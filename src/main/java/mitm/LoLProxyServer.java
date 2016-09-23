package mitm;

import mitm.proxy.SecureProxyEngine;
import mitm.proxy.filters.PrintDataFilter;

public class LoLProxyServer {

    public static String host = "lq.na2.lol.riotgames.com";
    public static String ip = "104.16.122.50";

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
