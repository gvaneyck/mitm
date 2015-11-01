package mitm;

import mitm.proxy.CrossDomainServerSocket;
import mitm.proxy.SecureProxyEngine;
import mitm.proxy.filters.shopheroes.ShopHeroesAutoBotFilter;
import mitm.proxy.filters.shopheroes.ShopHeroesDataFilter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ShopHeroesProxyServer {
    public static String host = "shopheroes-1.cloudcade.com";
    public static String ip = "104.197.86.226";

    public static boolean autoRecraft = false;
    public static boolean autoHarvest = false;

    public static void main(String[] args) {
        try {
            CrossDomainServerSocket.generate(host, 843);
            new SecureProxyEngine(host, ip, 443, 0, new ShopHeroesDataFilter(), new ShopHeroesAutoBotFilter());
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = in.readLine();
                if (line.equals("craft")) {
                    autoRecraft = !autoRecraft;
                    System.out.println("Auto recraft is now " + autoRecraft);
                } else if (line.equals("harvest")) {
                    autoHarvest = !autoHarvest;
                    System.out.println("Auto harvest is now " + autoHarvest);
                }
            }
        }
        catch (Exception e) {
            System.err.println("Could not initialize proxy:");
            e.printStackTrace();
            System.exit(2);
        }
    }
}
