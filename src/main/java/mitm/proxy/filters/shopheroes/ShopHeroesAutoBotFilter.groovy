package mitm.proxy.filters.shopheroes

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import mitm.ShopHeroesProxyServer
import mitm.proxy.filters.DataFilter
import mitm.proxy.filters.PrintDataFilter

class ShopHeroesAutoBotFilter extends DataFilter {
    public byte[] handle(String name, byte[] buffer, int bytesRead) throws IOException {
        System.out.println(name + ": " + PrintDataFilter.parse(buffer, bytesRead))

        String dataString = new String(buffer, 0, bytesRead)
        if (dataString.contains('{')) {
            dataString = dataString.substring(dataString.indexOf('{'))
        }

        def jsonData = null
        try {
            jsonData = new JsonSlurper().parseText(dataString)
        } catch (Exception e) {
            return null
        }

        if (dataString.contains('OutOfSyncEvent')) {
            throw new IOException()
        }

        jsonData.events?.each { event ->
            if (ShopHeroesProxyServer.autoRecraft && event.event == 'SlotCraftEvent') {
                final def data = event.data
                def thread = new Thread() {
                    public void run() {
                        sleep(data.end - System.currentTimeMillis() + 100);

                        def storeMsg = JsonOutput.toJson([command: 'StoreItem', slot: data.index])
                        remote.write(0x81)
                        remote.write(storeMsg.getBytes().length)
                        remote.write(storeMsg.getBytes())
                        println storeMsg

                        def craftMsg = JsonOutput.toJson([command: 'CraftItem', object: data.uid, slot: data.index, useAuto: false, useGems: false])
                        remote.write(0x81)
                        remote.write(craftMsg.getBytes().length)
                        remote.write(craftMsg.getBytes())
                        println craftMsg
                    }
                }
                thread.start()
            }
            if (ShopHeroesProxyServer.autoHarvest && event.event == 'UpdateResourcesEvent') {
                def binEntry = event.data.iterator().next()
                def binId = binEntry.key
                def binData = binEntry.value
                def harvestMessage = JsonOutput.toJson([command: 'StoreResource', resource: binId.substring(0, binId.indexOf('_')), moduleId: binId, amount: binData.quantity])
                remote.write(0x81)
                remote.write(harvestMessage.getBytes().length)
                remote.write(harvestMessage.getBytes())
                println harvestMessage
                //{"isLoop":true,"events":[{"event":"UpdateResourcesEvent","data":{"wood_6naz3jxde7b9":{"id":"wood_6naz3jxde7b9","quantity":1,"begin":1446325200949.792,"end":1446325216400.4358,"secondsNextIn":14.739435791015625}}}]}
            }
        }

        return null
    }
}
