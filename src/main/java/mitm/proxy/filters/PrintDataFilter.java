package mitm.proxy.filters;

import java.io.IOException;

public class PrintDataFilter extends DataFilter {
    public byte[] handle(String name, byte[] buffer, int bytesRead) throws IOException {
        System.out.println(name + ": " + parse(buffer, bytesRead));
        return null;
    }

    public static String parse(byte[] buffer, int bytesRead) {
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

        return stringBuffer.toString();
    }
}
