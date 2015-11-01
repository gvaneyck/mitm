package com.gvaneyck.prismata;

import com.gvaneyck.prismata.encoding.TypedObject;

import java.util.ArrayList;
import java.util.List;

public class ReplayGrabber {
    public static PrismataClient client = null;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("java -jar ReplayGrabber.jar <username> <password> [replay-ID ...]");
            System.out.println("Replay ID is optional and multiple can be specified.");
            System.out.println("Default behavior is to get all of YOUR replays.");
            return;
        }

        try {
            client = new PrismataClient();
            client.connect();
            client.login(args[0], args[1]);

            List<String> replayHashes = new ArrayList<String>();
            if (args.length > 2) {
                for (int i = 2; i < args.length; i++) {
                    replayHashes.add(args[i]);
                }
            }
            else {
                // Get a single replay and the count of replays
                client.sendNormalData(new Object[] { "ListFirstReplays", 7 });
                Object[] packet = client.getNormalPacket("ReplayList");

                int numReplays = (Integer)packet[1];

                Object[] replays = (Object[])packet[2];
                for (Object o : replays) {
                    TypedObject to = (TypedObject)o;
                    replayHashes.add(to.getString("hash"));
                }

                // Get the rest of the replays
                for (int i = 7; i < numReplays; i += 7) {
                    int replaysToGet = Math.min(numReplays - i, 7);
                    client.sendNormalData(new Object[] { "ListReplays", i, replaysToGet });

                    packet = client.getNormalPacket("ReplayList");

                    replays = (Object[])packet[2];
                    for (Object o : replays) {
                        TypedObject to = (TypedObject)o;
                        replayHashes.add(to.getString("hash"));
                    }
                }
            }

            TypedObject replayData = new TypedObject();
            for (String hash : replayHashes) {
                replayData.put(hash, getReplay(hash));
            }
            System.out.println(replayData.toPrettyString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static TypedObject getReplay(String hash) throws Exception {
        client.sendNormalData(new Object[] { "MenuReplay", hash });
        Object[] packet = client.getNormalPacket("BeginMenuReplay");
        TypedObject result = (TypedObject)packet[1];

        // Need to quit the game or the server complains...
        client.sendNormalData(new Object[] { "QuitGame" });
        client.getNormalPacket("QuitGame");

        return result;
    }
}
