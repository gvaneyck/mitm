package mitm;

import mitm.proxy.InsecureProxyEngine;
import mitm.proxy.filters.tt.TapTitanHackerFilter;
import mitm.proxy.filters.tt.TapTitanTimeFilter;
import mitm.proxy.socket.MITMPlainSocketFactory;

public class TapTitansProxyServer {
    public static String host = "api.taptitansgame.com";
    public static String ip = "204.236.229.161";

    public static void main(String[] args) {
        try {
            new InsecureProxyEngine(new MITMPlainSocketFactory(), new TapTitanHackerFilter(), new TapTitanTimeFilter(), host, ip, 80, 0);
        }
        catch (Exception e) {
            System.err.println("Could not initialize proxy:");
            e.printStackTrace();
            System.exit(2);
        }
    }
}
