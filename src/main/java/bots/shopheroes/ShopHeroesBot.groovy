package bots.shopheroes

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import mitm.proxy.socket.TrustEveryone
import org.java_websocket.client.DefaultSSLWebSocketClientFactory
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_17
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.java_websocket.handshake.ServerHandshake

import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import java.security.SecureRandom

class ShopHeroesBot extends WebSocketClient {
    def brain

    def userId
    def token

    def slurper = new JsonSlurper()

    public static void main(String[] args) {
        def (userId, token) = new File('login.txt').readLines()[0].split('=')*.trim()
        new ShopHeroesBot(userId, token)
    }

    public ShopHeroesBot(userId, token) {
        super(new URI('wss://shopheroes-1.cloudcade.com:443'), new Draft_17())
        this.userId = userId
        this.token = token

        brain = new ShopHeroesBrain(this)

        trustAllHosts()
        this.connect()
    }

    public void trustAllHosts() {
        // Install the all-trusting trust manager
        try {
            def sc = SSLContext.getInstance("TLS");
            def trustManagers = new X509TrustManager[1]
            trustManagers[0] = new TrustEveryone()
            sc.init(null, trustManagers, new SecureRandom())
            this.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sc))
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    void onOpen(ServerHandshake handshakedata) {
        def auth = [
                command: 'Authorize',
                authType: 'kongregate',
                version: 'v1.0.54381',
                os: 'Windows 7 Service Pack 1 (6.1.7601) 64bit',
                device: 'AMD Phenom(tm) II X4 965 Processor (16384 MB)',
                platform: 'web',
                userId: userId,
                gameAuthToken: token
        ]
        send(auth)
    }

    void onMessage(String message) {
        def data = slurper.parseText(message)

        data.events.each {
            try {
//                println "${it.event}: ${it.data}"
                if (brain.metaClass.getMetaMethod(it.event)) {
                    brain."${it.event}"(it.data)
                }
            } catch (Exception e) {
                e.printStackTrace()
            }
        }

        brain.rev()
    }

    void onClose(int code, String reason, boolean remote) {

    }

    void onError(Exception ex) {

    }

    public synchronized void send(Map data) {
        try {
            def jsonData = JsonOutput.toJson(data)
            println jsonData
            super.send(jsonData)
            sleep(100)
        } catch (WebsocketNotConnectedException e) {
            this.closeBlocking()
            new ShopHeroesBot(userId, token)
        }
    }
}
