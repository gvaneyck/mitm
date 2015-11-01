package mitm.proxy.filters.tt;

import mitm.proxy.filters.DataFilter;
import mitm.proxy.filters.PrintDataFilter;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class TapTitanTimeFilter extends DataFilter {
    PrintDataFilter printer = new PrintDataFilter();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Long lastTime = null;

    public byte[] handle(String name, byte[] buffer, int bytesRead) throws IOException {
        printer.handle(name, buffer, bytesRead);
        String data = new String(buffer, 0, bytesRead);

//        if (data.contains("\"relics_reward\":")) {
//            int idx = data.indexOf("Content-Length") + 16;
//            int idx2 = data.indexOf("\r", idx);
//            data = data.substring(0, idx) + (Integer.parseInt(data.substring(idx, idx2)) + 18) + data.substring(idx2);
//            data = data.replace("\"diamonds_reward\": 0", "\"diamonds_reward\": 1000000000");
//            data = data.replace("\"relics_reward\": 0", "\"relics_reward\": 1000000000");
//        }
        return data.getBytes();
    }
}
