package mitm;

import mitm.proxy.SecureProxyEngine;
import mitm.proxy.filters.PrintDataFilter;

public class TestProxyServer {
    public static String host = "www.google.com";
    public static String ip = "216.58.219.46";

    public static void main(String[] args) {
        try {
            new SecureProxyEngine(host, ip, 443, 0, new PrintDataFilter(), new PrintDataFilter());
        }
        catch (Exception e) {
            System.err.println("Could not initialize proxy:");
            e.printStackTrace();
            System.exit(2);
        }
    }
}
