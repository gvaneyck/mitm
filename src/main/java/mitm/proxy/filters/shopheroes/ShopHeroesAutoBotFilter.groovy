package mitm.proxy.filters.shopheroes

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import mitm.ShopHeroesProxyServer
import mitm.proxy.filters.DataFilter
import mitm.proxy.filters.PrintDataFilter

class ShopHeroesAutoBotFilter extends DataFilter {

    def resources = [
            iron:     [ max: 3 * 60 ],
            wood:     [ max: 3 * 100 ],
            leather:  [ max: 3 * 100 ],
            herbs:    [ max: 3 * 100 ],
            steel:    [ max: 3 * 20 ],
            hardwood: [ max: 2 * 30 ],
            fabric:   [ max: 3 * 30 ],
            oil:      [ max: 3 * 30 ],
            gems:     [ max: 0 * 5 ],
            mana:     [ max: 2 * 25 ]
    ]

    def craftQueue = []
    def recipes = [
            elvencoif: [
                    [ uid: 'plumedhat', reqs: [ leather: 17 ], waitTime: 0 ],
                    [ uid: 'plumedhat', reqs: [ leather: 17 ], waitTime: 0 ],
                    [ uid: 'plumedhat', reqs: [ leather: 17 ], waitTime: 0 ],
                    [ uid: 'plumedhat', reqs: [ leather: 17 ], waitTime: 114000 ],
                    [ uid: 'scarletcoif', reqs: [ leather: 44, fabric: 16 ], waitTime: 0 ],
                    [ uid: 'scarletcoif', reqs: [ leather: 44, fabric: 16 ], waitTime: 645000 ],
                    [ uid: 'elvencoif', reqs: [ leather: 100, fabric: 50, mana: 8 ] ],
            ],
//            scarletcoif: [
//                    [ uid: 'plumedhat', reqs: [ leather: 17 ], waitTime: 0 ],
//                    [ uid: 'plumedhat', reqs: [ leather: 17 ], waitTime: 114000 ],
//                    [ uid: 'scarletcoif', reqs: [ leather: 44, fabric: 16 ] ]
//            ],
            potionofstrength: [
                    [ uid: 'potionofspeed', reqs: [ herbs: 8 ], waitTime: 24000 ],
                    [ uid: 'potionofstrength', reqs: [ herbs: 45, oil: 10 ] ]
            ],
//            sealofdeflection: [
//                    [ uid: 'sealofdeflection', reqs: [ iron: 20, steel: 6 ] ]
//            ],
    ]

    def slots = [
            0: false,
            1: false,
            2: false,
            3: false,
            4: false
    ]

    public ShopHeroesAutoBotFilter() {
        resources.each { mat, value ->
            value.stored = value.max
        }
    }

    def last = ''

    public byte[] handle(String name, byte[] buffer, int bytesRead) throws IOException {
        System.out.println(name + ": " + PrintDataFilter.parse(buffer, bytesRead))

        String dataString = new String(buffer, 0, bytesRead)
        if (dataString.contains('{') || dataString.contains('}')) {
            if (dataString.charAt(0) == 0xFFFD) {
                last = ''
                if (dataString.charAt(1) == 0x02) {
                    dataString = dataString.substring(2)
                } else {
                    dataString = dataString.substring(dataString.indexOf(buffer[4]))
                }
            }
            dataString = last + dataString
        }

        def jsonData
        try {
            jsonData = new JsonSlurper().parseText(dataString)
        } catch (Exception e) {
            last = dataString
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
            if (event.event == 'GetCitiesEvent') {
                event.data.candidates.each {
                    println "${it.name} - ${(int)(it.avg / 1000000)}M avg, ${(int)(it.avg * it.pop / 1000000)}M total"
                }
            }
        }

        if (ShopHeroesProxyServer.spin) {
            ShopHeroesProxyServer.spin = false
            def spinMsg = '{"command":"SpinRoulette","useGems":false}'
            remote.write(0x81)
            remote.write(spinMsg.getBytes().length)
            remote.write(spinMsg.getBytes())
            println spinMsg
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
            def craftInfo = null
            craftQueue.each { info ->
                if (craftInfo) {
                    return
                }

                def valid = true
                if (info.minTime > System.currentTimeMillis()) {
                    valid = false
                }

                recipes[info.recipe][info.step].reqs.each { mat, amt ->
                    if (resources[mat].stored - reserved[mat] < amt) {
                        valid = false
                    }
                    reserved[mat] += amt
                }

                if (valid) {
                    craftInfo = info
                }
            }

            if (craftInfo) {
                craftQueue.remove(craftInfo)
            }

            recipes.each { recipe, steps ->
                if (craftInfo) {
                    return
                }

                def valid = true
                steps[0].reqs.each { mat, amt ->
                    if (resources[mat].stored - reserved[mat] < amt) {
                        valid = false
                    }
                }

                if (valid) {
                    craftInfo = [ recipe: recipe, step: 0 ]
                }
            }

            if (craftInfo) {
                def recipe = recipes[craftInfo.recipe]
                def step = recipe[craftInfo.step]

                craftInfo.reqs.each { mat, amt ->
                    resources[mat].stored -= amt
                }
                if (craftInfo.step < recipe.size() - 1) {
                    craftInfo.step++
                    craftInfo.minTime = System.currentTimeMillis() + (step.waitTime ?: 0) + 3000 // 3s buffer
                    craftQueue << craftInfo
                }
                doCraft(step.uid, slotId)
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
