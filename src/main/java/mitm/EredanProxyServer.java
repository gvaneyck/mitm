package mitm;

import mitm.proxy.InsecureProxyEngine;
import mitm.proxy.filters.eredan.EredanHTTPRequestFilter;
import mitm.proxy.filters.eredan.EredanHTTPResponseFilter;
import mitm.proxy.filters.eredan.EredanServerRequestFilter;
import mitm.proxy.filters.eredan.EredanServerResponseFilter;
import mitm.proxy.socket.MITMPlainSocketFactory;

public class EredanProxyServer {
    public static String httpHost = "www.eredan-arena.com";
    public static String httpIp = "195.60.188.44";
    public static String serverFakeHost = "www.ooooo.com";
    public static String serverIp = "195.60.188.25";

    public static void main(String[] args) {
        try {
            new InsecureProxyEngine(new MITMPlainSocketFactory(), new EredanHTTPRequestFilter(), new EredanHTTPResponseFilter(), httpHost, httpIp, 80, 0);
            new InsecureProxyEngine(new MITMPlainSocketFactory(), new EredanServerRequestFilter(), new EredanServerResponseFilter(), serverFakeHost, serverIp, 9339, 0);
            new InsecureProxyEngine(new MITMPlainSocketFactory(), null, null, serverFakeHost, serverIp, 443, 0);

        }
        catch (Exception e) {
            System.err.println("Could not initialize proxy:");
            e.printStackTrace();
            System.exit(2);
        }
    }
}
