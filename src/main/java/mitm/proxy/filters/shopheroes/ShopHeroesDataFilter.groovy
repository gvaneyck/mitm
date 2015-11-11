package mitm.proxy.filters.shopheroes;

import mitm.proxy.filters.DataFilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class ShopHeroesDataFilter extends DataFilter {
    boolean skipNext = false;

    public byte[] handle(String name, byte[] buffer, int bytesRead) throws IOException {
//        byte[] bufferCopy = Arrays.copyOf(buffer, bytesRead);
//
//        byte flags = bufferCopy[0];
//        boolean fin = (flags & 0x80) != 0;
//        boolean rsv1 = (flags & 0x40) != 0;
//        boolean rsv2 = (flags & 0x20) != 0;
//        boolean rsv3 = (flags & 0x10) != 0;
//        int opcode = flags & 0x0F;
//
//        boolean masked = (bufferCopy[1] & 0x80) != 0;
//        int length = bufferCopy[1] & 0x7F;
//
//        int offset = 2;
//        if (length == 126) {
//            length = ((bufferCopy[offset + 0] & 0xFF) << 8)
//                    + (bufferCopy[offset + 1] & 0xFF);
//            offset += 2;
//        } else if (length == 127) {
//            length = ((bufferCopy[offset + 0] & 0xFF) << 24)
//                    + ((bufferCopy[offset + 1] & 0xFF) << 16)
//                    + ((bufferCopy[offset + 2] & 0xFF) << 8)
//                    + (bufferCopy[offset + 3] & 0xFF);
//            offset += 4;
//        }
//
//        if (masked) {
//            for (int i = offset + 4; i < bufferCopy.length; i++) {
//                byte xor = bufferCopy[((i - offset - 4) % 4) + offset];
//                bufferCopy[i] = (byte) (bufferCopy[i] ^ xor);
//            }
//            offset += 4;
//        }
//
//        String jsonData = new String(bufferCopy, offset, bufferCopy.length - offset);
//        System.out.println(jsonData);
//
//        if (skipNext) {
//            skipNext = false
//            throw new IOException('Skipping message')
//        }
//        if (jsonData.contains('ReportError')) {
//            skipNext = true
//            throw new IOException('Skipping message')
//        }

        return null;
    }

    private void doCopy(InputStream is, OutputStream os) throws Exception {
        int oneByte;
        while ((oneByte = is.read()) != -1) {
            os.write(oneByte);
        }
        os.close();
        is.close();
    }

    private void dumpBytes(String name, byte[] buffer, boolean doAscii) {
        final StringBuffer stringBuffer = new StringBuffer();

        boolean inHex = false;
        for (int i = 0; i < buffer.length; i++) {
            final int value = (buffer[i] & 0xFF);

            if (doAscii && value >= ' ' && value <= '~') {
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

        System.out.print(name + ": ");
        System.out.println(stringBuffer.toString());    }
}
