package mitm.proxy.socket;

import org.apache.commons.io.IOUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

public class LocalKeyStore {

    public static final char[] keyStorePassword = "asdfasdf".toCharArray();
    private static final File keyStoreFile = new File("local.jks");

    private static KeyStore localKeyStore = null;

    public static KeyStore getLocalKeyStore() throws Exception {
        if (localKeyStore == null) {
            localKeyStore = KeyStore.getInstance("jks");
            if (keyStoreFile.exists()) {
                InputStream in = new FileInputStream(keyStoreFile);
                localKeyStore.load(in, keyStorePassword);
                in.close();
            } else {
                localKeyStore.load(null, null);
            }
        }
        return localKeyStore;
    }

    private static void saveLocalKeyStore() throws Exception {
        OutputStream out = new FileOutputStream(keyStoreFile);
        localKeyStore.store(out, keyStorePassword);
        out.close();
    }

    public static void forgeCert(String domain) throws Exception {
        // Turn domain into wildcard format, e.g. *.google.com
        int idx = domain.lastIndexOf('.');
        idx = domain.lastIndexOf('.', idx - 1);
        if (idx == -1) {
            throw new Exception("Domains less than 3 parts (e.g. google.com vs www.google.com) are not allowed");
        } else {
            domain = "*." + domain.substring(idx + 1);
        }

        // Load the key store and check for the cert
        KeyStore keyStore = getLocalKeyStore();
        X509Certificate cert = (X509Certificate)keyStore.getCertificate(domain);
        if (cert != null && cert.getNotAfter().before(new Date())) {
            return;
        }

        // Create and save the cert since it did not exist
        generateCert(domain);
    }

    private static void generateCert(String domain) throws Exception {
        File certsDir = new File("certs");
        if (!certsDir.exists()) {
            certsDir.mkdir();
        }

        String shortName = domain.substring(2);
        String[] command = new String[] {
                "openssl",
                "req",
                "-new",
                "-x509",
                "-newkey", "rsa:2048",
                "-nodes",
                "-out", "certs/" + shortName + ".crt",
                "-keyout", "certs/" + shortName + ".key",
                "-subj", "/C=LH/ST=LocalHost/L=LocalHost/O=MITM/OU=MITM/CN=" + domain
        };
        new ProcessBuilder(command).redirectErrorStream(true).start();

        // TODO: Wait for files to exist

        byte[] keyBytes = IOUtils.toByteArray(new FileInputStream("certs/" + shortName + ".key"));
        String key = new String(keyBytes)
                .replace("-----BEGIN PRIVATE KEY-----\n", "")
                .replace("\n-----END PRIVATE KEY-----\n", "");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(DatatypeConverter.parseBase64Binary(key));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(keySpec);

        byte[] certBytes = IOUtils.toByteArray(new FileInputStream("certs/" + shortName + ".crt"));
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate newCert = cf.generateCertificate(new ByteArrayInputStream(certBytes));

        KeyStore keyStore = getLocalKeyStore();
        keyStore.deleteEntry(domain);
        keyStore.setKeyEntry(domain, privateKey, keyStorePassword, new Certificate[] { newCert });
//        keyStore.setCertificateEntry(domain, newCert);
        saveLocalKeyStore();
    }
}
