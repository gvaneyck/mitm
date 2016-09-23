package mitm;

import mitm.proxy.SecureProxyEngine;
import mitm.proxy.filters.PrintDataFilter;

public class BattlebornProxyServer {
    public static String host = "hydra.services.gearboxsoftware.com";
    public static String ip = "52.202.135.75";

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
