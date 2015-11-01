package bots

import groovy.json.JsonOutput
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

def cookie
def dictionary = [:]
def cardDetails = [:]

def getHttp = {
    def http = new HTTPBuilder("http://www.eredan-arena.com/")
//    http.setProxy('localhost', 8888, 'http')
    return http
}

def login = { user, pass ->
    def http = getHttp()
    http.request(Method.POST) {
        requestContentType = ContentType.URLENC
        uri.path = '/'
        body = [
                pseudo: user,
                pass: pass,
                x: 0,
                y: 0
        ]

        response.success = { resp ->
            cookie = resp.getHeaders('Set-Cookie')[0].value
        }
    }
}

def doRequest = { postBody ->
    sleep(1000)

    postBody.version = 'web'
    postBody.device = 'COMPUTER_WIN_DEVICE'
    postBody.params = JsonOutput.toJson(postBody.params)

    def http = getHttp()
    http.request(Method.POST) {
        requestContentType = ContentType.URLENC

        uri.path = '/gateway.php'
        uri.query = [ lang: 'us' ]

        headers.'Cookie' = cookie
        headers.'Accept' = 'application/json'

        body = postBody

        response.success = { resp, json ->
            return json
        }
    }
}

def initData = {
    def result = doRequest(params: [], action: 'Load_get_datas')
    dictionary = result.textes
    cardDetails = result.cartes.collectEntries { key, value -> [ key, [ name: value.id_nom, next: value.evolution ] ] }
    return result
}

def getLoot = {
    def result = doRequest(params: [], action: 'Coffres_getCoffre')
    return result
}

def convertToXP = {
    def result = doRequest(params: [], action: 'BoutiqueGateway_boosterConvertirXP')
    return result
}

def takeCardOrSell = {
    def result = doRequest(params: [], action: 'BoutiqueGateway_boosterRecupererCarte')
    return result
}

def buyBooster = {
    def result = doRequest(params: ['1'], action: 'BoutiqueGateway_boosterOuvrir')
    return result
}

def result
def user = 'jabe'
def pass = 'x'


login(user, pass)
result = initData()
def lastRegen = result.player.energyInfos.pe_regen.toLong() * 1000
def energy = result.player.energyInfos.pe.toInteger()

//result = buyBooster()
//println dictionary[cardDetails[result.id_carte].name]
//System.exit(0)

while (true) {
    try {
        def timeToWait = lastRegen + 30 * 60 * 1000 - System.currentTimeMillis() + 10000
        if (energy == 0 && timeToWait > 0) {
            if (timeToWait < 1700000) {
                println "Sleeping ${timeToWait / 1000}s"
            }
            sleep(timeToWait)
        }

        login(user, pass)

        result = getLoot()
        lastRegen = result.energy_infos.pe_regen.toLong() * 1000
        energy = result.energy_infos.pe.toInteger()

        def reward = result.coffre_infos.reward
        print "${reward.type} ${reward.value}"

        if (reward.type == 'card' || reward.type == 'booster') {
            print ' ' + dictionary[cardDetails[reward.boosterInfos.id_carte].name]

            if (reward.boosterInfos.status == 2 && cardDetails[reward.boosterInfos.id_carte].next) {
                // Convert to XP
                convertToXP()
                print ' convert to XP'
            } else {
                // New card or max level
                takeCardOrSell()
                if (reward.boosterInfos.status == 2) {
                    print ' sold'
                } else {
                    print ' evolved/received'
                }
            }
        }
        println ''
    }
    catch (Exception e) {
        takeCardOrSell()
        initData()
    }
}
