/*
Copyright 2007

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of Stanford University nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

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
