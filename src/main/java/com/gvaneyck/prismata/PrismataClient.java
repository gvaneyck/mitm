package com.gvaneyck.prismata;

import com.gvaneyck.prismata.encoding.AMF3Decoder;
import com.gvaneyck.prismata.encoding.AMF3Encoder;
import com.gvaneyck.prismata.encoding.PBKDF2;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PrismataClient {
    private static char[] passphrase = "changeit".toCharArray();
    private static String server = "ec2-54-211-113-4.compute-1.amazonaws.com";
    private static String version = "3044";

    private AMF3Encoder aec = new AMF3Encoder();

    private Socket normalSocket;
    private DataOutputStream normalOut;
    private AMF3PacketReader normalPacketReader;

    private SSLSocket secureSocket;
    private DataOutputStream secureOut;
    private AMF3PacketReader securePacketReader;

    public void connect() {
        try {
            // Basic connection
            normalSocket = new Socket(server, 11618);
            normalOut = new DataOutputStream(normalSocket.getOutputStream());
            normalPacketReader = new AMF3PacketReader(normalSocket.getInputStream());

            // SSL Connection
            openSSLSocket(server, 11620);
            secureOut = new DataOutputStream(secureSocket.getOutputStream());
            securePacketReader = new AMF3PacketReader(secureSocket.getInputStream());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void login(String user, String password) {
        try {
            Object[] packet;

            // Wait for session ID
            packet = getNormalPacket("Connected");
            String sessionID = (String)packet[1];

            sendSecureData(new Object[] { "LookupSalt", user });
            String salt = (String)getSecurePacket("PasswordSalt")[1];

            sendSecureData(new Object[] { "LoginPassword", user, PBKDF2.hash(password, salt, 1000), sessionID });
            String signedSessionID = (String)getSecurePacket("SignedSessionID")[1];

            // boolean args are "killOtherConnectionsOnLogIn" and "quietLogin"
            sendNormalData(new Object[] { "LogIn", signedSessionID, true, true, version });
            getNormalPacket("LoggedIn");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendNormalData(Object[] data) throws IOException {
        sendData(data, normalOut);
    }

    public void sendSecureData(Object[] data) throws IOException {
        sendData(data, secureOut);
    }

    private void sendData(Object[] data, DataOutputStream out) throws IOException {
        byte[] bytes = aec.encode(data);
        out.writeInt(bytes.length);
        out.write(bytes);
        out.flush();
    }

    public Object[] getNormalPacket() {
        return getNormalPacket(null);
    }

    public Object[] getNormalPacket(String type) {
        return normalPacketReader.getPacket(type);
    }

    public Object[] getSecurePacket() {
        return getSecurePacket(null);
    }

    public Object[] getSecurePacket(String type) {
        return securePacketReader.getPacket(type);
    }

    private void openSSLSocket(String host, int port) throws Exception {
        SavingTrustManager tm = openSocketWithCert(host, port);

        // Try to handshake the socket
        try {
            secureSocket.startHandshake();
            // Success first try!
            return;
        }
        catch (SSLException e) {
            secureSocket.close();
        }

        // If we failed to handshake, save the certificate we got and try again
        KeyStore keyStore = getKeyStore();

        // Add certificate
        X509Certificate[] chain = tm.chain;
        if (chain == null) {
            throw new Exception("Failed to obtain server certificate chain");
        }

        X509Certificate cert = chain[0];
        String alias = "prismata-1";
        keyStore.setCertificateEntry(alias, cert);

        // Save certificate
        OutputStream out = new FileOutputStream("prismata.cert");
        keyStore.store(out, passphrase);
        out.close();

        // Retry connection
        openSocketWithCert(host, port);
        secureSocket.startHandshake();
    }

    private SavingTrustManager openSocketWithCert(String host, int port) throws Exception {
        KeyStore keyStore = getKeyStore();

        // Set up the socket factory with the KeyStore
        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        X509TrustManager defaultTrustManager = (X509TrustManager)tmf.getTrustManagers()[0];
        SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
        context.init(null, new TrustManager[] { tm }, null);
        SSLSocketFactory factory = context.getSocketFactory();

        secureSocket = (SSLSocket)factory.createSocket(host, port);

        return tm;
    }

    private KeyStore getKeyStore() throws Exception {
        File cert = new File("prismata.cert");
        if (!cert.exists()) {
            cert = new File(System.getProperty("java.home") + "/lib/security/cacerts");
        }

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(new FileInputStream(cert), passphrase);

        return keyStore;
    }
}

class AMF3PacketReader {
    private InputStream in;
    private AMF3Decoder adc = new AMF3Decoder();

    private volatile List<Object> packets = Collections.synchronizedList(new LinkedList<Object>());

    public AMF3PacketReader(InputStream stream) {
        this.in = new BufferedInputStream(stream, 16384);

        Thread curThread = new Thread() {
            public void run() {
                try {
                    int pos = 0;
                    byte[] buffer = new byte[32768];
                    while (true) {
                        int val = in.read();
                        if (val == -1) {
                            System.out.println("DCed");
                            // HACK WARNING, need to gracefully handle getting disconnected
                            System.exit(0);
                            break;
                        }

                        buffer[pos] = (byte)val;
                        pos++;

                        try {
                            Object packet = adc.decode(buffer, pos);
                            packets.add(packet);
                            pos = 0;
                        }
                        catch (Exception e) {
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        curThread.setName("PacketReader");
        curThread.setDaemon(true);
        curThread.start();
    }

    public Object[] getPacket() {
        return getPacket(null);
    }

    public Object[] getPacket(String type) {
        Object packet = null;
        while (packet == null) {
            while (packets.isEmpty()) {
                sleep(10);
            }

            packet = packets.remove(0);
            if (packet instanceof Integer) {
                packet = null;
            }

            if (packet != null) {
                Object[] temp = (Object[])packet;
                if (type != null && !temp[0].equals(type)) {
                    packet = null;
                }
            }
        }
        return (Object[])packet;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        }
        catch (Exception e) {
        }
    }
}
