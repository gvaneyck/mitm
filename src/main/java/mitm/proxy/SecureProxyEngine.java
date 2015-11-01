package mitm.proxy;

import mitm.proxy.filters.DataFilter;
import mitm.proxy.socket.MITMSSLSocketFactory;

import javax.net.ssl.SSLSocket;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.cert.X509Certificate;

public class SecureProxyEngine implements Runnable {

    private String interceptedHost;
    private String remoteIp;
    private int port;
    private int timeout;

    private DataFilter requestFilter;
    private DataFilter responseFilter;

    private MITMSSLSocketFactory sslSocketFactory;
    private ServerSocket proxySocket;

    public SecureProxyEngine(String interceptedHost, String remoteIp, int port, int timeout, DataFilter requestFilter, DataFilter responseFilter) throws Exception {
        this.interceptedHost = interceptedHost;
        this.remoteIp = remoteIp;
        this.port = port;
        this.timeout = timeout;
        this.requestFilter = requestFilter;
        this.responseFilter = responseFilter;

        initProxySocket();

        new Thread(this, "SecureProxyEngine " + interceptedHost + " " + port).start();
    }

    private void initProxySocket() throws Exception {
        // Create a client connection to grab their cert
        sslSocketFactory = new MITMSSLSocketFactory();
        SSLSocket tempSocket = (SSLSocket) sslSocketFactory.createClientSocket(remoteIp, port);
        X509Certificate javaCert = (X509Certificate) tempSocket.getSession().getPeerCertificates()[0];
        tempSocket.close();

        // Rebuild the socket factory w/ the required cert
        sslSocketFactory = new MITMSSLSocketFactory(javaCert);

        //MITMSSLSocketFactory tempFactory = new MITMSSLSocketFactory(javaCert, true);
        proxySocket = sslSocketFactory.createServerSocket(interceptedHost, port, timeout);
    }

    public void run() {
        int i = 0;
        while (true) {
            try {
                i++;

                final Socket localSocket = proxySocket.accept();

                System.out.println(port + " " + i + " Accepted a new socket");

                InputStream in = localSocket.getInputStream();
                OutputStream out = localSocket.getOutputStream();

                final Socket remoteSocket = sslSocketFactory.createClientSocket(remoteIp, port);

                requestFilter.local = out;
                requestFilter.remote = remoteSocket.getOutputStream();
                responseFilter.local = out;
                responseFilter.remote = remoteSocket.getOutputStream();

                new StreamThread(port + " " + i + " Local", in, remoteSocket.getOutputStream(), requestFilter);
                new StreamThread(port + " " + i + " Remote", remoteSocket.getInputStream(), out, responseFilter);
            }
            catch (InterruptedIOException e) {
                System.err.println("Listen time out");
                break;
            }
            catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

}
