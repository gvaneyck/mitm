package mitm.proxy.socket;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class TrustEveryone implements X509TrustManager {

    public void checkClientTrusted(X509Certificate[] chain, String authenticationType) {
    }

    public void checkServerTrusted(X509Certificate[] chain, String authenticationType) {
    }

    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}
