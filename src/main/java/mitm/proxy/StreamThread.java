// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/

package mitm.proxy;

import mitm.proxy.filters.DataFilter;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

public class StreamThread implements Runnable {
    private final static int BUFFER_SIZE = 65536;

    private final String name;
    private final InputStream in;
    private final OutputStream out;
    private final DataFilter filter;

    public StreamThread(String name, InputStream in, OutputStream out, DataFilter filter) {
        this.name = name;
        this.in = in;
        this.out = out;
        this.filter = filter;

        new Thread(this, "Filter thread for " + name).start();
    }

    public void run() {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                final int bytesRead = in.read(buffer, 0, BUFFER_SIZE);

                if (bytesRead == -1) {
                    break;
                }

                try {
                    byte[] newBytes = null;
                    if (filter != null) {
                        newBytes = filter.handle(name, buffer, bytesRead);
                    }

                    if (newBytes != null) {
                        out.write(newBytes);
                    } else {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                catch (Exception e) {
                    int i = 0;
                    // Drop messages if we get an exception from filter
                }
            }
        }
        catch (SocketException e) {
            // Be silent about SocketExceptions.
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }

        try {
            out.close();
        }
        catch (Exception e) {
        }
        try {
            in.close();
        }
        catch (Exception e) {
        }

//        System.out.println(name + " closed");
    }
}
