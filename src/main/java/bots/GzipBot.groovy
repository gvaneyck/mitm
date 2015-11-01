package bots

import groovy.json.JsonOutput

import java.util.zip.GZIPInputStream

System.in.withReader {
    while (true) {
        try { println JsonOutput.prettyPrint(new GZIPInputStream(new ByteArrayInputStream(it.readLine().decodeBase64())).text) } catch (Exception e) { }
    }
}