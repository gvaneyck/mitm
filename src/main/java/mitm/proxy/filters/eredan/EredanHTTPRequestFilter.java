package mitm.proxy.filters.eredan;

import mitm.proxy.filters.DataFilter;
import mitm.proxy.filters.PrintDataFilter;

import java.io.IOException;

public class EredanHTTPRequestFilter extends DataFilter {
    DataFilter printer = new PrintDataFilter();

    public byte[] handle(String name, byte[] buffer, int bytesRead) throws IOException {
//        printer.handle(name, buffer, bytesRead);
        String data = new String(buffer, 0, bytesRead);

        return null;
    }
}
