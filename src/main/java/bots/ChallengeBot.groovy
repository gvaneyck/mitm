package bots

import groovy.json.JsonOutput
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

def getHttp = {
    def http = new HTTPBuilder("http://www.eredan-arena.com/")
    http.setProxy('localhost', 8888, 'http')
    return http
}

def login = { user, pass ->
    def cookie
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
    return cookie
}

def doRequest = { cookie, postBody ->
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

def getPlayerInfo = { cookie ->
    def result = doRequest(cookie, [ params: [], action: 'Load_get_datas' ])

    def player = [ cookie: cookie ]
    player.id = result.player.id
    player.token = result.player.token
    player.xp = result.player.xpInfos.xp
    player.xpNext = result.player.xpInfos.xp_max
    player.lastEnergy = result.player.energyInfos.pe_regen
    player.energy = result.player.energyInfos.pe
    return player
}

def challenge = { p1, p2 ->
    def result = doRequest(p1.cookie, [ params: [ p2.id ], action: 'Player_lancerDefi' ])
    doRequest(p2.cookie, [ params: [ p1.id ], action: 'Player_accepterDefi' ])

    return result.idDefi
}

def jabe = getPlayerInfo(login('a', 'b'))
def jabe2 = getPlayerInfo(login('c', 'd'))
def challengeId = challenge(jabe, jabe2)

def temp = 2
