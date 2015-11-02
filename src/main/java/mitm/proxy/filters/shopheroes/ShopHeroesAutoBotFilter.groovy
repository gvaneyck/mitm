package mitm.proxy.filters.shopheroes

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import mitm.ShopHeroesProxyServer
import mitm.proxy.filters.DataFilter
import mitm.proxy.filters.PrintDataFilter

class ShopHeroesAutoBotFilter extends DataFilter {

    def resources = [
            iron: [ stored: 0, max: 0 ],
            wood: [ stored: 240, max: 240 ],
            leather: [ stored: 300, max: 300 ],
            herb: [ stored: 300, max: 300 ],
            steel: [ stored: 0, max: 0 ],
            hardwood: [ stored: 0, max: 0 ],
            fabric: [ stored: 75, max: 75 ],
            oil: [ stored: 75, max: 75 ],
            gems: [ stored: 0, max: 0 ],
            mana: [ stored: 50, max: 50 ]
    ]

    public byte[] handle(String name, byte[] buffer, int bytesRead) throws IOException {
        System.out.println(name + ": " + PrintDataFilter.parse(buffer, bytesRead))

        String dataString = new String(buffer, 0, bytesRead)
        if (dataString.contains('{')) {
            dataString = dataString.substring(dataString.indexOf('{'))
        }

        def jsonData
        try {
            jsonData = new JsonSlurper().parseText(dataString)
        } catch (Exception e) {
            return null
        }

        if (dataString.contains('OutOfSyncEvent')) {
            throw new IOException()
        }

        jsonData.events?.each { event ->
            final def data = event.data
            if (ShopHeroesProxyServer.autoRecraft && event.event == 'SlotCraftEvent') {
                def thread = new Thread() {
                    public void run() {
                        sleep(data.end - System.currentTimeMillis() + 100)

                        def storeMsg = JsonOutput.toJson([command: 'StoreItem', slot: data.index])
                        remote.write(0x81)
                        remote.write(storeMsg.getBytes().length)
                        remote.write(storeMsg.getBytes())
                        println storeMsg

                        autoCraft()
                    }
                }
                thread.start()
            }
            if (event.event == 'UpdateResourcesEvent') {
                //{"wood_6naz3jxde7b9":{"id":"wood_6naz3jxde7b9","quantity":1,"begin":1446325200949.792,"end":1446325216400.4358,"secondsNextIn":14.739435791015625}}}]}
                def binEntry = data.iterator().next()
                def binId = binEntry.key
                def binType = binId.substring(0, binId.indexOf('_'))
                def binQuantity = binEntry.value.quantity
                resources[binType][binId] = binQuantity

                if (resources[binType].stored < resources[binType].max) {
                    storeResource(binType, binId, binQuantity)
                }
            }
            if (event.event == 'ResourceChangeEvent') {
                //{"uid":"herb","stored":204,"bins":[{"id":"herbs_bwx5m6cbrcnmi","quantity":26,"begin":1446448309296.6147,"end":1446448324747.2585,"secondsNextIn":3.382258544921875},{"id":"herbs_dnqi7jzudte29","quantity":19,"begin":1446448307785.3271,"end":1446448323235.971,"secondsNextIn":1.870970947265625},{"id":"herbs_rls8cc0ltyb9","quantity":0,"begin":1446448313035.19,"end":1446448328485.8337,"secondsNextIn":7.120833740234375}]}}]}
                def type = data.uid
                resources[type].stored = data.stored

                def triggered = false
                data.bins.each {
                    resources[type][it.id] = it.quantity
                    if (!triggered && resources[type].stored < resources[type].max && it.quantity > 0) {
                        storeResource(type, it.id, it.quantity)
                        triggered = true
                    }
                }
            }
        }

        return null
    }

    def autoCraft() {
        if (ShopHeroesProxyServer.autoHarvest) {
            def craftMsg = JsonOutput.toJson([command: 'CraftItem', object: data.uid, slot: data.index, useAuto: false, useGems: false])
            remote.write(0x81)
            remote.write(craftMsg.getBytes().length)
            remote.write(craftMsg.getBytes())
            println craftMsg
        }
    }

    def storeResource(type, id, quantity) {
        def harvestMessage = JsonOutput.toJson([command: 'StoreResource', resource: type, moduleId: id, amount: quantity])
        remote.write(0x81)
        remote.write(harvestMessage.getBytes().length)
        remote.write(harvestMessage.getBytes())
        println harvestMessage
    }
}
