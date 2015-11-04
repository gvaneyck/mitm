package mitm.proxy.filters.shopheroes

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import mitm.ShopHeroesProxyServer
import mitm.proxy.filters.DataFilter
import mitm.proxy.filters.PrintDataFilter

class ShopHeroesAutoBotFilter extends DataFilter {

    def resources = [
            iron: [ stored: 180, max: 180 ],
            wood: [ stored: 300, max: 300 ],
            leather: [ stored: 300, max: 300 ],
            herbs: [ stored: 300, max: 300 ],
            steel: [ stored: 75, max: 75 ],
            hardwood: [ stored: 75, max: 75 ],
            fabric: [ stored: 80, max: 80 ],
            oil: [ stored: 75, max: 75 ],
            gems: [ stored: 0, max: 0 ],
            mana: [ stored: 50, max: 50 ]
    ]

    def initialCrafts = [
            'plumedhat': 5,
            'potionofspeed': 1,
            'crowstick': 3
    ]

    def craftQueue = []
    def craftOptions = [
            [
                    uid: 'plumedhat',
                    reqs: [ leather: 17 ],
                    waitTime: 0,
                    nextItem: [
                            uid: 'plumedhat',
                            reqs: [ leather: 17 ],
                            waitTime: 114000,
                            nextItem: [
                                    uid: 'scarletcoif',
                                    reqs: [ leather: 44, fabric: 16 ]
                            ]
                    ]
            ],
            [
                    uid: 'potionofspeed',
                    reqs: [ herbs: 8 ],
                    waitTime: 24000,
                    nextItem: [
                            uid: 'potionofstrength',
                            reqs: [ herbs: 45, oil: 10 ]
                    ]
            ],
            [
                    uid: 'crowstick',
                    reqs: [ wood: 4 ],
                    waitTime: 13000,
                    nextItem: [
                            uid: 'healingrod',
                            reqs: [ wood: 30, hardwood: 5 ]
                    ]
            ]
    ]
    def slots = [
            0: false,
            1: false,
            2: false,
            3: false,
            4: false
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
            throw new IOException('Dropping out of sync event')
        }

        jsonData.events?.each { event ->
            final def data = event.data
            if (ShopHeroesProxyServer.autoCraft && event.event == 'SlotCraftEvent') {
                def slotId = data.index
                def thread = new Thread() {
                    public void run() {
                        slots[slotId] = true

                        sleep(data.end - System.currentTimeMillis() + 100)

                        def storeMsg = JsonOutput.toJson([command: 'StoreItem', slot: data.index])
                        remote.write(0x81)
                        remote.write(storeMsg.getBytes().length)
                        remote.write(storeMsg.getBytes())
                        println storeMsg

                        sleep(1000) // Give the server a second to process before we try to use the slot

                        slots[slotId] = false

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
                if (type == 'herb') {
                    type = 'herbs'
                }
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

    public synchronized void autoCraft() {
        if (ShopHeroesProxyServer.autoCraft) {
            def slotId = slots.find { it.value == false }?.key
            if (slotId == null) {
                return // No slots
            }

            def reserved = resources.collectEntries { [ (it.key): 0 ] }
            def craftItem = null
            craftQueue.each { item ->
                if (craftItem) {
                    return
                }

                def valid = true
                if (item.minTime > System.currentTimeMillis()) {
                    valid = false
                }
                item.reqs.each { mat, amt ->
                    if (resources[mat].stored - reserved[mat] < amt) {
                        valid = false
                    }
                    reserved[mat] += amt
                }

                if (valid) {
                    craftItem = item
                }
            }

            if (craftItem) {
                craftQueue.remove(craftItem)
            }

            craftOptions.each { item ->
                if (craftItem) {
                    return
                }

                def valid = true
                item.reqs.each { mat, amt ->
                    if (resources[mat].stored - reserved[mat] < amt) {
                        valid = false
                    }
                }

                if (valid) {
                    craftItem = item
                }
            }

            if (craftItem) {
                if (initialCrafts.containsKey(craftItem.uid) && initialCrafts[craftItem.uid] > 0) {
                    if (craftItem.nextItem) {
                        def item = cloneMap(craftItem.nextItem)
                        craftQueue << item
                    }
                    initialCrafts[craftItem.uid]--
                    autoCraft()
                    return
                }

                craftItem.reqs.each { mat, amt ->
                    resources[mat].stored -= amt
                }
                if (craftItem.nextItem) {
                    def item = cloneMap(craftItem.nextItem)
                    item.minTime = System.currentTimeMillis() + craftItem.waitTime + 5000 // 5s buffer
                    craftQueue << item
                }
                doCraft(craftItem.uid, slotId)
            }
        }
    }

    def doCraft(item, slotId) {
        slots[slotId] = true

        def craftMsg = JsonOutput.toJson([command: 'CraftItem', object: item, slot: slotId, useAuto: false, useGems: false])
        remote.write(0x81)
        remote.write(craftMsg.getBytes().length)
        remote.write(craftMsg.getBytes())
        println craftMsg

        sleep(1000) // Give the server a second to process before we try to use resources again
    }

    def storeResource(type, id, quantity) {
        if (ShopHeroesProxyServer.autoHarvest) {
            if (type == 'herbs') {
                type = 'herb'
            }
            def harvestMessage = JsonOutput.toJson([command: 'StoreResource', resource: type, moduleId: id, amount: quantity])
            remote.write(0x81)
            remote.write(harvestMessage.getBytes().length)
            remote.write(harvestMessage.getBytes())
            println harvestMessage

            autoCraft()
        }
    }

    def cloneMap(map) {
        return new JsonSlurper().parseText(JsonOutput.toJson(map))
    }
}
