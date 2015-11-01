package mitm.proxy.filters;

import java.io.IOException;
import java.util.List;

public class FilteredPrintDataFilter extends DataFilter {

    private List<String> allowed;
    private List<String> taboo;

    public FilteredPrintDataFilter(List<String> allowed, List<String> taboo) {
        this.allowed = allowed;
        this.taboo = taboo;
    }

    public byte[] handle(String name, byte[] buffer, int bytesRead) throws IOException {
        final StringBuffer stringBuffer = new StringBuffer();

        boolean inHex = false;

        for (int i = 0; i < bytesRead; i++) {
            final int value = (buffer[i] & 0xFF);

            if (value >= ' ' && value <= '~') {
                if (inHex) {
                    stringBuffer.append(']');
                    inHex = false;
                }

                if (value == '\r') {
                    stringBuffer.append("\\r");
                }
                else if (value == '\n') {
                    stringBuffer.append("\\n");
                }
                else {
                    stringBuffer.append((char)value);
                }
            }
            else {
                if (!inHex) {
                    stringBuffer.append('[');
                    inHex = true;
                }

                stringBuffer.append(String.format("%02X", value));
            }
        }

        if (inHex) {
            stringBuffer.append("]");
        }

        String value = stringBuffer.toString();

        boolean allow = true;
        if (allowed != null) {
            allow = false;
            for (String word : allowed) {
                if (value.contains(word)) {
                    allow = true;
                    break;
                }
            }
        }

        if (taboo != null) {
            for (String word : taboo) {
                if (value.contains(word)) {
                    allow = false;
                    break;
                }
            }
        }

        if (allow) {
            System.out.print(name + ": ");
            System.out.println(value);
        }

        return null;
    }
}
