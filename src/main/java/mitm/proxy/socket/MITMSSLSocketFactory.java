package mitm.proxy.socket;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public final class MITMSSLSocketFactory implements MITMSocketFactory {
    final ServerSocketFactory serverSocketFactory;
    final SocketFactory clientSocketFactory;
    final SSLContext sslContext;

    public MITMSSLSocketFactory() throws Exception {
        sslContext = SSLContext.getInstance("SSL");

        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(LocalKeyStore.getLocalKeyStore(), LocalKeyStore.keyStorePassword);

        sslContext.init(keyManagerFactory.getKeyManagers(), new TrustManager[]{new TrustEveryone()}, null);

        clientSocketFactory = sslContext.getSocketFactory();
        serverSocketFactory = sslContext.getServerSocketFactory();
    }

    public ServerSocket createServerSocket(String localHost, int localPort, int timeout) throws IOException {
        final SSLServerSocket socket = (SSLServerSocket)serverSocketFactory.createServerSocket(localPort, 50, InetAddress.getByName(localHost));
        socket.setSoTimeout(timeout);
        socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
        return socket;
    }

    public Socket createClientSocket(String remoteHost, int remotePort) throws IOException {
        final SSLSocket socket = (SSLSocket)clientSocketFactory.createSocket(remoteHost, remotePort);
        socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
        socket.startHandshake();
        return socket;
    }
}
