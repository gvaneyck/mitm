package mitm;

public class SpellstoneProxyServer {
    public static String host = "spellstone.synapse-games.com";
    public static String ip = "54.164.160.236";

    public static void main(String[] args) {
        try {
//            CrossDomainServerSocket.generate(host);
//            new SecureProxyEngine(new MITMSSLSocketFactory(), new PrintDataFilter(), new PrintDataFilter(), host, ip, 443, 0);
        }
        catch (Exception e) {
            System.err.println("Could not initialize proxy:");
            e.printStackTrace();
            System.exit(2);
        }
    }
}
