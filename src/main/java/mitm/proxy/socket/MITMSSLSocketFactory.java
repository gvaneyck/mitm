package mitm.proxy.socket;

import mitm.proxy.socket.cert.SignCert;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public final class MITMSSLSocketFactory implements MITMSocketFactory {
    final ServerSocketFactory m_serverSocketFactory;
    final SocketFactory m_clientSocketFactory;
    final SSLContext m_sslContext;

    final String keyStoreFile = "dummy.jks";
    final char[] keyStorePassword = "asdfasdf".toCharArray();
    final String keyStoreType = "jks";
    final String keyAlias = "mykey";

    public MITMSSLSocketFactory() throws IOException, GeneralSecurityException {
        m_sslContext = SSLContext.getInstance("SSL");

        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

        final KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(new FileInputStream(keyStoreFile), keyStorePassword);
        keyManagerFactory.init(keyStore, keyStorePassword);

        m_sslContext.init(keyManagerFactory.getKeyManagers(), new TrustManager[] { new TrustEveryone() }, null);

        m_clientSocketFactory = m_sslContext.getSocketFactory();
        m_serverSocketFactory = m_sslContext.getServerSocketFactory();
    }


    public MITMSSLSocketFactory(Certificate javaCert) throws Exception {
        m_sslContext = SSLContext.getInstance("SSL");

        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

        // Load PK
        final KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(new FileInputStream(keyStoreFile), keyStorePassword);
        PrivateKey pk = (PrivateKey)keyStore.getKey(keyAlias, keyStorePassword);

        iaik.x509.X509Certificate newCert = SignCert.forgeCert(keyStore, keyStorePassword, keyAlias, javaCert);

        KeyStore newKS = KeyStore.getInstance(keyStoreType);
        newKS.load(null, null);
        newKS.setKeyEntry(keyAlias, pk, keyStorePassword, new Certificate[] { newCert });

        keyManagerFactory.init(newKS, keyStorePassword);

        m_sslContext.init(keyManagerFactory.getKeyManagers(), new TrustManager[] { new TrustEveryone() }, null);

        m_clientSocketFactory = m_sslContext.getSocketFactory();
        m_serverSocketFactory = m_sslContext.getServerSocketFactory();
    }

    public final ServerSocket createServerSocket(String localHost, int localPort, int timeout) throws IOException {
        final SSLServerSocket socket = (SSLServerSocket)m_serverSocketFactory.createServerSocket(localPort, 50, InetAddress.getByName(localHost));

        socket.setSoTimeout(timeout);

        socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());

        return socket;
    }

    public final Socket createClientSocket(String remoteHost, int remotePort) throws IOException {
        final SSLSocket socket = (SSLSocket)m_clientSocketFactory.createSocket(remoteHost, remotePort);

        socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());

        socket.startHandshake();

        return socket;
    }

    private static class TrustEveryone implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain, String authenticationType) {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authenticationType) {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
