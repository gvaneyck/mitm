package com.gvaneyck.prismata.encoding;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PBKDF2 {
    public static String hash(String password, String salt, int iterations) {
        Mac mac;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(password.getBytes("UTF-8"), "HmacSHA256");
            mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        byte[] u = salt.getBytes();
        u = mac.doFinal(u);

        byte[] accum = u;
        byte[] accumNext = new byte[32];
        for (int i = 0; i < iterations; i++) {
            u = mac.doFinal(u);
            for (int j = 0; j < 32; j++) {
                accumNext[j] = (byte)(u[j] ^ accum[j]);
            }
            accum = accumNext;
        }

        return new String(Base64.encodeBytes(accum));
    }
}
