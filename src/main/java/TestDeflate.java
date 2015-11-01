import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;

public class TestDeflate {
    public static void main(String[] args) {
        try {
            String s = "H4sIAAAAAAAAANVYy46jOBT9F9aM5BcGZzdSb6bV8wWtCDngUFbxEpi0qkv172MbzCNJJZCe6ppRbcpg7rm2z7n3OK9e14rmry8x9HaYBAHw7YP4IMrkydt9f/XUSy3ihKeN8Hb6bcoLnvX/PvE2rnP+IlJvd+R5K/RbkXHVxrI85jITrZ3W1lXZVo0evHoyjcuq8HYeQaHn66G3C30LoZ+NE33vJJpWVqV+CHFIcUQwgvpxm3eZflbLhivhvfneIa+S5xHdhEMhoIwB5ntZXkmTNPS9Y9Uk4u9O5bLOX2xSCa95IpVJUa/xxHPRNXoHzPrLQ5920shaxeJ4FGpIPRX9wz4zygK7hGFFlNFhRQgi34TshI2o0+SJkifef/cd+GD/5k+gmzAjtsQEDtOseMC8Bon2b3vfa8QP3qR/pmm/BzlvW7E8FgyHgBCO5zLMW55KFDCGKAmmU0mq4sCV4qUyJ6ND5rJVstSvCAw0Nk/OsAhxyZMRy846O/8IsgBFYELKqlwUBsSeq2abVCZhvSSeK9HYVRu66RnVj9LQeyR3KU+CdxpyhgExgzpFRJEjjQs5bTUL4aAMk/AQLGt4OiPYkIPS51oqQ6z9eAyjRMzB//8Upf+ixxQFwQZ2E0rm7CY0GMkIRnbriJf0hgtF4fWQDKA5JAPYcRKHk4jPAeFaNbloEN9Wk+G4FtOM4wVveJea5ZyLCeCrYmIucbZNTGVVFlWjLuSEg1+TE2DwhpxIsEZOJodPk5MjX7BOToBOe/pT5je6U0joY91pg5QCtGhOurg5JpJJSvhub9qgJBhQPIfUY1ffMUXva2lDb/oANWFIr6oJOTWhbWoS+VGW4kJM6OHeRAAika5/+H0xUYrXiAnd7U1737lBZOMQnUlV1y6sG2rldL3AWsWT51g1PWvrU20BzZw2qQY92sSmT+ywbkWXVnq7vvKD2dNpTlxXreyZ8YdxILIQ1pf1UcfvUt48F7xE9ozHnM4+tlGLKhXxUWZP/Z6db5MdT8nGSZFauiQqefrGO2OCe4CPN8QfVG9ISMMH3fCGegOd/XXq12VuWA+BtyvOsndvMgwIoYVj0GO3i3RmiK9aBvQLsGC5WD12iw1mpe4qLPjEYsfA1VrntgwFG214JwqRS3VZ7ui9cmdKiSt3+LLcIULeL3f2NjHXsQl2We7oZ1px1/nIKi0jxuZ+7OctLUfgIS2TLfzW9m3ZygkcWzmcbDF5h98zWSG0AZYBsPTi7i6KMB1BdcRz0NX+wTV1iLZLKhPnaqLR71FTdM+Iz8V0acRJFN2412JGVogp+kwf/oE/FIXh5Ks2yQn9yzdM9N+6YaLfcsNEW4gNr90wo1s3zDXEtjncNcXG/819JHj7BxBKbvA3FQAA";
            byte[] sBytes = DatatypeConverter.parseBase64Binary(s);

            String test = "123";
            InputStream in = new ByteArrayInputStream(test.getBytes());
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            OutputStream out = new DeflaterOutputStream(result);
            doCopy(in, out);

            for (int i = 0; i < sBytes.length; i++) {
                for (int j = 1; i + j <= sBytes.length; j++) {
                    try {
                        in = new GZIPInputStream(new ByteArrayInputStream(sBytes, i, j));
                        result = new ByteArrayOutputStream();
                        doCopy(in, result);
                        System.out.println(i + " " + j + " " + new String(result.toByteArray()));
                    } catch (Exception e) {
                    }
                }
            }

//            byte[] bytes = result.toByteArray();
//            in = new InflaterInputStream(new ByteArrayInputStream(sBytes));
//            result = new ByteArrayOutputStream();
//            doCopy(in, result);
//
//            String s1 = new String(result.toByteArray());
//            String s2 = "";
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void doCopy(InputStream is, OutputStream os) throws Exception {
        int oneByte;
        while ((oneByte = is.read()) != -1) {
            os.write(oneByte);
        }
        os.close();
        is.close();
    }
}
