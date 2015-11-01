package mitm.proxy.filters.eredan;

import mitm.proxy.filters.DataFilter;
import mitm.proxy.filters.PrintDataFilter;

import java.io.IOException;

public class EredanServerResponseFilter extends DataFilter {
    DataFilter printer = new PrintDataFilter();

    public byte[] handle(String name, byte[] buffer, int bytesRead) throws IOException {
        printer.handle(name, buffer, bytesRead);
//        String data = new String(buffer, 0, bytesRead);
//        if (data.contains("compressed_data")) {
//            int idx1 = data.indexOf("compressed_data") + 18;
//            int idx2 = data.indexOf("\"", idx1);
//            String compressedData = data.substring(idx1, idx2);
//
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            Inflater decompresser = new Inflater(true);
//            InflaterOutputStream inflaterOutputStream = new InflaterOutputStream(stream, decompresser);
//            inflaterOutputStream.write(compressedData.getBytes());
//            inflaterOutputStream.close();
//            String result = new String(stream.toByteArray());
//            String foo = "Bar";
//        }

        return null;
    }
}
