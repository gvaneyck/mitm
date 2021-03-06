package bots.shopheroes

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class ShopHeroesBrain {
    def craftThrottle = System.currentTimeMillis() + 5000

    ShopHeroesBot client

    def sociability = [
            theor: 6,
            garreth: 4,
            minh: 7,
            melina: 7,
            karal: 6,
            clovis: 6,
            palash: 5,
            albert: 9,
            gauvin: 8,
            irena: 2,
            mila: 4,
            nya: 1,
            darthos: 6,
            oneira: 9,
            lancaster: 3,
            reina: 4,
            kurul: 3,
            fiora: 7,
            azula: 3,
            francesca: 5,
            louca: 6,
            kuroshobi: 1,
            alicia: 6,
            mojian: 3,
            edward: 4
    ]

    def energy
    def upgrade
    def fusion
    def inventory
    def slots
    def resources
    def visitors
    def trades

    public ShopHeroesBrain(client) {
        this.client = client
    }

    def canCompliment(customer, seed) {
        if (customer == null) {
            return false
        }
        return (((seed * 16807L % 2147483647L - 1) / 2.14748365E+09f) < sociability[customer] / 10)
    }

    def SignInEvent(data) {
        energy = data.user.energy
        upgrade = [ end: data.user.mUpgrade.end, uid: data.user.mUpgrade.uid ]
        fusion = data.user.fusion // TODO: fusion

        inventory = [:]
        data.user.inventory.each {
            inventory["${it.uid}|q"] = it.q
            inventory["${it.uid}|q1"] = it.q1
            inventory["${it.uid}|q2"] = it.q2
            inventory["${it.uid}|q3"] = it.q3
            inventory["${it.uid}|q4"] = it.q4
            inventory["${it.uid}|q5"] = it.q5
        }

        slots = data.user.slots.collect { [ available: !it.locked, expectedTime: 0 ] }
        data.user.slots.eachWithIndex { it, i ->
            SlotCraftEvent([ index: i, end: it.end, secondsCompletedIn: it.secondsCompletedIn ])
        }

        resources = data.user.resources.collectEntries { resData ->
            def binned = 0
            resData.bins.each { binned += it.quantity }
            return [ resData.uid, [ stored: resData.stored, binned: binned, max: data.modifier[resData.uid + '_storage'] ] ]
        }

        visitors = data.user.visitors.each { VisitorEnterEvent(it) }

        trades = data.user.trades // TODO: trades
    }

    def UpdateResourcesEvent(data) {
        def key = data.iterator().next().key
        def resource = key.substring(0, key.indexOf('_'))
        if (resource == 'herbs') {
            resource = 'herb'
        }

        resources[resource].binned = 0
        data.each { key2, value ->
            resources[resource].binned += value.quantity
        }
    }

    def ResourceChangeEvent(data) {
        def resource = data.uid
        resources[resource].stored = data.stored
        resources[resource].binned = 0
        data.bins.each {
            resources[resource].binned += it.quantity
        }
    }

    def StoreItemEvent(data) {
        def quality = data.inventoryItem.find { it.key.startsWith('q') }
        inventory["${data.inventoryItem.uid}|${quality.key}"] = quality.value
    }

    def SlotCraftEvent(data) {
        if (data.end == 0) {
            return
        }

        def slotId = data.index
        def thread = new Thread() {
            public void run() {
                slots[slotId].available = false
                slots[slotId].expectedTime = System.currentTimeMillis() + data.secondsCompletedIn * 1000 + 5000
                if (data.secondsCompletedIn > 0) {
                    sleep(data.secondsCompletedIn * 1000 + 100)
                }
                client.send([command: 'StoreItem', slot: slotId])
                craftThrottle = System.currentTimeMillis() + 1000
                slots[slotId].available = true
            }
        }
        thread.start()
    }

    def VisitorEnterEvent(data) {
        def dialogs = []
        if (data.type == 'buyer' && ['palash', 'garreth'].contains(data.uid)) {
            def item = null
            for (int i = 0; i < 5; i++) {
                def sellName = 'elvencoif:' + i
                def invName = "elvencoif|${i == 0 ? 'q' : 'q' + i}"
                if (inventory[invName] > 0) {
                    item = sellName
                    inventory[invName]--
                }
            }
            if (item) {
                dialogs << [ d: 'suggest', o: item ]
//                dialogs << [ d: 'surcharge', o: 20 ]
            }
        }
        if (canCompliment(data.uid, data.seed)) {
            dialogs << [ d: 'compliment' ]
        }
        if (data.type == 'seller') {
//            dialogs << [ d: 'buy' ]
        }
        if (!dialogs.find { it.d == 'buy' || it.d == 'suggest' }) {
            dialogs << [ d: 'refuse' ]
        }
        if (dialogs.find { it.d == 'suggest' }) {
            dialogs << [ d: 'sell' ]
        }
        client.send([ command: 'VisitorDialog', visitor: data.index, dialogs: dialogs ])
    }

    def ChatEvent(data) {
        println "${data.name.substring(0, data.name.indexOf('#'))}: ${data.text}"
    }

    def OutOfSyncEvent(data) {
        println 'OUT OF SYNC'
    }

    def rev() {
        checkResources()
        checkCrafting()
        checkSelling()
    }

    def checkSelling() {

    }

    def checkResources() {
        resources.each { res, data ->
            if (data.stored < data.max && data.binned > 0) {
                def toStore = (int)Math.min(data.binned, data.max - data.stored)
                data.binned -= toStore
                // Game server lies about binned resources in UpdateResourcesEvent, so don't assume we get them
                // It immediately responds with ResourceChangeEvent, so just have to wait a message
                //data.stored += toStore
                client.send([ command: 'StoreResource', resource: res, amount: toStore ])
            }
        }
    }

    def MAX_ITEMS = 6

    def recipes = [
            elvencoif: [ name: 'elvencoif', reqs: [ leather: 100, fabric: 40, mana: 8 ], itemReq: 'scarletcoif', itemReqQuality: 0, itemReqNum: 1 ],
            scarletcoif: [ name: 'scarletcoif', reqs: [ leather: 44, fabric: 16 ], itemReq: 'plumedhat', itemReqQuality: 1, itemReqNum: 2 ],
            plumedhat: [ name: 'plumedhat', reqs: [ leather: 17 ] ],

            whisperingwand: [ name: 'whisperingwand', reqs: [ wood: 75, hardwood: 32 ], itemReq: 'mutedcaster', itemReqQuality: 0, itemReqNum: 3 ],
            mutedcaster: [ name: 'mutedcaster', reqs: [ wood: 21 ], itemReq: 'walkingstick', itemReqQuality: 1, itemReqNum: 3 ],
            walkingstick: [ name: 'walkingstick', reqs: [ wood: 1 ] ],

            soldiersmark: [ name: 'soldiersmark', reqs: [ iron: 45, steel: 10 ], itemReq: 'dirk', itemReqQuality: 0, itemReqNum: 3 ],
            dirk: [ name: 'dirk', reqs: [ iron: 5 ], itemReq: 'knife', itemReqQuality: 0, itemReqNum: 1 ],
            knife: [ name: 'knife', reqs: [ iron: 1 ] ],

            sealofdeflection: [ name: 'sealofdeflection', reqs: [ iron: 20, steel: 6 ] ]
    ]

    def toCraft = [
            'elvencoif',
//            'soldiersmark',
//            'sealofdeflection',
    ]
    def craftQueue = []

    def getOwnedQuantity(item, quality) {
        def owned = 0
        for (int i = quality; i < 5; i++) {
            owned += inventory[getInventoryKey(item, i)]
        }
        return owned
    }

    def getInventoryKey(item, quality) {
        return "${item}|q${quality > 0 ? quality : ''}"
    }

    def removeItem(name, quality, amt) {
        for (int i = quality; i < 5; i++) {
            def itemName = getInventoryKey(name, i)
            def avail = Math.min(inventory[itemName], amt)
            inventory[itemName] -= avail
            amt -= avail
            if (amt == 0)
                break
        }
    }

    def synchronized checkCrafting() {
        if (System.currentTimeMillis() < craftThrottle) {
            return // Give time to retrieve all crafts from slots
        }

        def slotId = null
        slots.eachWithIndex { entry, i ->
            if (entry.available || (System.currentTimeMillis() > entry.expectedTime && entry.expectedTime != 0)) {
                slotId = i
            }
        }
        if (slotId == null) {
            return
        }

        fillCraftQueue()

        def aRes = new JsonSlurper().parseText(JsonOutput.toJson(resources))

        def craftItem = null
        craftQueue.each { item ->
            if (craftItem != null) {
                return
            }

            def recipe = recipes[item]
            if (recipe.itemReq && getOwnedQuantity(recipe.itemReq, recipe.itemReqQuality) < recipe.itemReqNum) {
                return
            }

            def canCraft = true
            recipe.reqs.each { res, amt ->
                if (aRes[res].stored < amt) {
                    canCraft = false
                }
                aRes[res].stored -= amt
            }

            if (canCraft) {
                craftItem = item
            }
        }

        if (craftItem) {
            craftThrottle = System.currentTimeMillis() + 5000
            craftQueue.remove(craftItem)

            slots[slotId].available = false
            slots[slotId].expectedTime = System.currentTimeMillis() + 5000

            def recipe = recipes[craftItem]
            recipe.reqs.each { res, amt ->
                resources[res].stored -= amt
            }

            if (recipe.itemReq) {
                removeItem(recipe.itemReq, recipe.itemReqQuality, recipe.itemReqNum)
            }
            client.send([command: 'CraftItem', object: craftItem, slot: slotId, useAuto: false, useGems: false])
        }
    }

    def fillCraftQueue() {
        toCraft.each { item ->
            def needed = MAX_ITEMS
            def quality = 6 // To make sure it's not found, always want 5 in queue
            def recipe = recipes[item]
            while (recipe) {
                needed -= getOwnedQuantity(recipe.name, quality)
                def inQueue = craftQueue.count(recipe.name)

                for (int i = inQueue; i < needed; i++) {
                    craftQueue << recipe.name
                }

                needed *= (recipe.itemReqNum ?: 1)
                quality = recipe.itemReqQuality
                recipe = recipes[recipe.itemReq]
            }
        }
    }
}
