package mitm.proxy.filters;

import java.io.IOException;
import java.io.OutputStream;

public abstract class DataFilter {
    public OutputStream remote;
    public OutputStream local;

    abstract public byte[] handle(String name, byte[] buffer, int bytesRead) throws IOException;
}
