package mitm.proxy.filters.eredan;

import mitm.proxy.filters.DataFilter;

import java.io.IOException;

public class EredanHTTPResponseFilter extends DataFilter {

    public byte[] handle(String name, byte[] buffer, int bytesRead) throws IOException {
        String data = new String(buffer, 0, bytesRead);
        if (data.contains("SmartFoxClient")) {
            int idx3 = data.indexOf("<port>");
            int idx4 = data.indexOf("</port>");
            String port = data.substring(idx3 + 6, idx4);

            int idx1 = data.indexOf("<ip>");
            int idx2 = data.indexOf("</ip>");
            String serverIp = data.substring(idx1 + 4, idx2);

            if (!serverIp.equals("195.60.188.25")) {
                System.out.println("Unexpected IP");
            }

            String fakeServer = "www.ooooo.com";
            data = data.substring(0, idx1 + 4) + fakeServer + data.substring(idx2);
            System.out.println("Attempted to load config for " + serverIp + ":" + port + ", redirected to " + fakeServer);

            return data.getBytes();
        }

        return null;
    }
}
