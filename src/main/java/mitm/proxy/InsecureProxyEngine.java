package mitm.proxy;

import mitm.proxy.filters.DataFilter;
import mitm.proxy.socket.MITMPlainSocketFactory;

import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class InsecureProxyEngine implements Runnable {

    public static final String ACCEPT_TIMEOUT_MESSAGE = "Listen time out";

    private String remoteIp;
    private int port;

    private ServerSocket proxySocket;

    private DataFilter requestFilter;
    private DataFilter responseFilter;

    public InsecureProxyEngine(MITMPlainSocketFactory plainSocketFactory, DataFilter requestFilter, DataFilter responseFilter, String interceptedHost, String remoteIp, int port, int timeout) throws Exception {
        this.requestFilter = requestFilter;
        this.responseFilter = responseFilter;
        this.remoteIp = remoteIp;
        this.port = port;

        proxySocket = plainSocketFactory.createServerSocket(interceptedHost, port, timeout);

        new Thread(this, "InsecureProxyEngine " + interceptedHost + " " + port).start();
    }

    public void run() {
        int i = 0;
        while (true) {
            try {
                i++;

                final Socket localSocket = proxySocket.accept();
                InputStream in = localSocket.getInputStream();
                OutputStream out = localSocket.getOutputStream();

//                System.out.println(port + " " + i + " Accepted a new socket");

                final Socket remoteSocket = new MITMPlainSocketFactory().createClientSocket(remoteIp, port);

                new StreamThread(port + " " + i + " Local", in, remoteSocket.getOutputStream(), requestFilter);
                new StreamThread(port + " " + i + " Remote", remoteSocket.getInputStream(), out, responseFilter);
            }
            catch (InterruptedIOException e) {
                System.err.println(ACCEPT_TIMEOUT_MESSAGE);
                break;
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
