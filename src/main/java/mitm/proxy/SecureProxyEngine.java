package mitm.proxy;

import mitm.proxy.filters.DataFilter;
import mitm.proxy.socket.LocalKeyStore;
import mitm.proxy.socket.MITMSSLSocketFactory;

import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SecureProxyEngine implements Runnable {

    private String remoteIp;
    private int port;

    private DataFilter requestFilter;
    private DataFilter responseFilter;

    private MITMSSLSocketFactory sslSocketFactory;
    private ServerSocket proxySocket;

    public SecureProxyEngine(String interceptedHost, String remoteIp, int port, int timeout, DataFilter requestFilter, DataFilter responseFilter) throws Exception {
        this.remoteIp = remoteIp;
        this.port = port;

        this.requestFilter = requestFilter;
        this.responseFilter = responseFilter;

        LocalKeyStore.forgeCert(interceptedHost);
        sslSocketFactory = new MITMSSLSocketFactory();
        proxySocket = sslSocketFactory.createServerSocket(interceptedHost, port, timeout);

        new Thread(this, "SecureProxyEngine " + interceptedHost + " " + port).start();
    }

    public void run() {
        int connectionCount = 0;
        while (true) {
            try {
                connectionCount++;

                final Socket localSocket = proxySocket.accept();

                System.out.println(port + " " + connectionCount + " Accepted a new socket");

                InputStream in = localSocket.getInputStream();
                OutputStream out = localSocket.getOutputStream();

                final Socket remoteSocket = sslSocketFactory.createClientSocket(remoteIp, port);

                requestFilter.local = out;
                requestFilter.remote = remoteSocket.getOutputStream();
                responseFilter.local = out;
                responseFilter.remote = remoteSocket.getOutputStream();

                new StreamThread(port + " " + connectionCount + " Local", in, remoteSocket.getOutputStream(), requestFilter);
                new StreamThread(port + " " + connectionCount + " Remote", remoteSocket.getInputStream(), out, responseFilter);
            } catch (InterruptedIOException e) {
                System.err.println("Listen time out");
                break;
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
