package mitm.proxy;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class CrossDomainServerSocket {
    public static void generate(String host, int port) {
        try {
            final ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host));
            serverSocket.setSoTimeout(0);

            Thread t = new Thread() {
                public void run() {
                    while (true) {
                        try {
                            final Socket localSocket = serverSocket.accept();
                            InputStream in = localSocket.getInputStream();
                            OutputStream out = localSocket.getOutputStream();

                            in.read(new byte[23]);

                            out.write("<?xml version=\"1.0\"?><cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"*\" /></cross-domain-policy>".getBytes());
                            out.write(0);
                            out.flush();

                            localSocket.close();
                        } catch (Exception e) {

                        }
                    }
                }
            };
            t.start();
        } catch (Exception e) {
            System.err.println("Failed to start cross domain interceptor");
            e.printStackTrace();
        }
    }
}
