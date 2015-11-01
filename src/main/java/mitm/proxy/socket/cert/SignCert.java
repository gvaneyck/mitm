package mitm.proxy.socket.cert;

import iaik.asn1.structures.AlgorithmID;
import iaik.x509.X509Certificate;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

public class SignCert {
    public static X509Certificate forgeCert(KeyStore caKS, char[] caKSPass, String caAlias, Certificate baseCert) throws Exception {
        java.security.Security.addProvider(new iaik.security.provider.IAIK());

        CertificateFactory cf = CertificateFactory.getInstance("X.509", "IAIK");

        PrivateKey pk = (PrivateKey)caKS.getKey(caAlias, caKSPass);
        if (pk == null) {
            System.out.println("no private key!");
        }
        Certificate tmp = caKS.getCertificate(caAlias);
        //Use IAIK's cert-factory, so we can easily use the X509CertificateGenerator!
        X509Certificate caCert = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(tmp.getEncoded()));

        X509Certificate cert = new X509Certificate(baseCert.getEncoded());
        cert.setPublicKey(caCert.getPublicKey());
        cert.setIssuerDN(caCert.getSubjectDN());
        cert.sign(AlgorithmID.sha256WithRSAEncryption, pk);

        return cert;
    }
}
