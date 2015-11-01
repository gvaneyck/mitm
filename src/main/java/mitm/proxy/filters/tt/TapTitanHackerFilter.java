package mitm.proxy.filters.tt;

import mitm.proxy.filters.DataFilter;
import mitm.proxy.filters.PrintDataFilter;

import java.io.IOException;

public class TapTitanHackerFilter extends DataFilter {
    PrintDataFilter printer = new PrintDataFilter();

    public byte[] handle(String name, byte[] buffer, int bytesRead) throws IOException {
        printer.handle(name, buffer, bytesRead);

        String data = new String(buffer, 0, bytesRead);
        if (data.startsWith("POST /mark_hacker") || data.startsWith("POST /upload_stats")) {
            throw new IOException();
        }

        return null;
    }
}
