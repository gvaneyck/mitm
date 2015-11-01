package mitm;

import mitm.proxy.InsecureProxyEngine;
import mitm.proxy.filters.DataFilter;
import mitm.proxy.filters.FilteredPrintDataFilter;
import mitm.proxy.filters.PrintDataFilter;
import mitm.proxy.socket.MITMPlainSocketFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * A Java MITM proxy server for Prismata.  Based on code by Srinivas Inguva
 *
 * @author Gabriel Van Eyck
 */

// -Djava.protocol.handler.pkgs=com.sun.net.ssl.internal.www.protocol -Djavax.net.debug=ssl
// FW2BT-EEG7V-BRUHG-XMWTC
public class PrismataProxyServer {
    public static boolean debugFlag = false;

    public static String host = "ec2-54-211-113-4.compute-1.amazonaws.com";
    public static String ip = "54.211.113.4";

    public static void main(String[] args) {
        List<String> taboo = new ArrayList<String>();
        taboo.add("EchoRequest");
        taboo.add("EchoReply");

        DataFilter basicFilter = new PrintDataFilter();
        DataFilter filteredFilter = new FilteredPrintDataFilter(null, taboo);

        try {
            initXMLServerSocket(host, 11619);

//            new SecureProxyEngine(new MITMSSLSocketFactory(), basicFilter, basicFilter, host, ip, 11620, 0);
            new InsecureProxyEngine(new MITMPlainSocketFactory(), filteredFilter, filteredFilter, host, ip, 11618, 0);
        }
        catch (Exception e) {
            System.err.println("Could not initialize proxy:");
            e.printStackTrace();
            System.exit(2);
        }
    }

    public static void initXMLServerSocket(String host, int port) throws Exception {
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
                    }
                    catch (Exception e) {

                    }
                }
            }
        };
        t.start();
    }
}
