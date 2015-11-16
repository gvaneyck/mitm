package bots.shopheroes

class ShopAnalysis {

    static DataLoader dl = new DataLoader(
            'S:\\Unity Decompiler\\Shop Heroes\\shopheroes\\staticdata\\CAB-6fc6b3ebbc62e47018550ddbb2a35e17\\TextAsset\\staticdata.txt',
            'S:\\Unity Decompiler\\Shop Heroes\\shopheroes\\sharedassets0\\TextAsset\\texts_en.txt')

    public static void main(String[] args) {
        new ShopAnalysis().calcBestItem()
    }

    def dumpItems() {
        new File('out.csv').withWriter { out ->
            def header = "Name,Item Level,Price,Power,Crafting XP,Type,Rarity,Iron,Wood,Leather,Herbs,Steel,Hardwood,Fabric,Oil,Gems,Mana,Requirement 1,Requirement 2,Metalworking,Woodworking," +
                    "Textile Working,Alchemy,Magic,Weaponcrafting,Armorcrafting,Arts And Crafts,Jewelry,Rune Writing,Tinkering,L1,L1 Boost,L2,L2 Boost,L3,L3 Boost,L4,L4 Boost,L5,L5 Boost"
            out.write("${header}\n")
            dl.items.each { it ->
                if (it.power != 0) {
                    out.write("${getName(it.name + '_name')},${it.level},${it.price},${it.power},${it.xp},${getName(it.type)},${it.rare},")
                    out.write("${it.iron},${it.wood},${it.leather},${it.herb},${it.steel},${it.hardwood},${it.fabric},${it.oil},${it.gems},${it.mana},")
                    def req1 = (it.item1 == '' ? '---' : (it.i1c + ' ' + getQuality(it.i1q) + getName(it.item1 + '_name')))
                    def req2 = (it.item2 == '' ? '---' : (it.i2c + ' ' + getQuality(it.i2q) + getName(it.item2 + '_name')))
                    out.write("${req1},${req2},")
                    out.write("${it.metalworking},${it.woodworking},${it.textileworking},${it.alchemy},${it.channeling},")
                    out.write("${it.weaponcrafting},${it.armormaking},${it.craftsmanship},${it.jewelry},${it.enchanting},${it.tinkering},")
                    out.write("${it.l1},${getItemBonus(it.l1B)},")
                    out.write("${it.l2},${getItemBonus(it.l2B)},")
                    out.write("${it.l3},${getItemBonus(it.l3B)},")
                    out.write("${it.l4},${getItemBonus(it.l4B)},")
                    out.write("${it.l5},${getItemBonus(it.l5B)}\n")
                }
            }
        }
    }

    def dumpAchievements() {
        new File('out.csv').withWriter { out ->
            dl.achievements.each { it ->
                if (it.threshold != 0) {
                    def description = dictionary[it.family + '01_description'].replace('{0}', '' + it.threshold)
                    def reward = it.reward.replace('gems:gems,', 'Gems ').replace('gold:gold,', 'Gold ')
                    out.write("\"${description}\",\"${reward}\"\n")
                }
            }
        }
    }

    def dumpBuildings() {
        def buildingStats = [:]
        dl.improvements.each { x ->
            if (!buildingStats[x.family]) {
                buildingStats[x.family] = [:]
            }
            buildingStats[x.family][x.level] = '' + x.amount
        }

        new File('out.csv').withWriter { out ->
            buildingStats.each { family, levels ->
                out.write(getName(family + '01_name'))
                out.write(',')
            }
            out.write('\n')

            for (int i = 1; i <= 100; i++) {
                buildingStats.each { family, levels ->
                    out.write(levels[i] ?: '')
                    out.write(',')
                }
                out.write('\n')
            }
        }
    }

    def dumpQuests() {
        new File('out.csv').withWriter { out ->
            out.write("Quest,Reward,Time,L0,F0,E0,B0,L1,F1,E1,B1,L2,F2,E2,B2,L3,F3,E3,B3,L4,F4,E4,B4,L5,E5\n")
            dl.adventures.each {
                def hours = (int) (it.duration / 3600)
                def minutes = (int) (it.duration / 60) % 60
                def difficulty = getItem(it.reward).level + it.difficulty
                out.write("${getName(it.name + '_name')},${getName(it.reward + '_name')},${hours ? hours + 'H' : ''}${minutes ? minutes + 'M' : ''},")
                for (int i = 0; i < 5; i++) {
                    out.write("${it.q0},${it.ex1},${getQuestDifficulty(difficulty, i, false)},${getQuestDifficulty(difficulty, i, true)},")
                }
                out.write("${it.q5},${getQuestDifficulty(difficulty, 5, false)}\n")
            }
        }
    }

    def dumpBoosts() {
        def buildingStats = [:]
        dl.improvements.each { x ->
            if (!buildingStats[x.family]) {
                buildingStats[x.family] = [:]
            }

            buildingStats[x.family][x.level] = [formatBoost(x.collective), formatBoost(x.individual)]
        }

        new File('out.csv').withWriter { out ->
            buildingStats.each { family, levels ->
                out.write(getName(family + '01_name'))
                out.write(',,')
            }
            out.write('\n')

            for (int i = 1; i <= 40; i++) {
                buildingStats.each { family, levels ->
                    out.write(levels[i][0] ?: '')
                    out.write(',')
                    out.write(levels[i][1] ?: '')
                    out.write(',')
                }
                out.write('\n')
            }
        }
    }

    def dumpWorkers() {
        new File('out.csv').withWriter { out ->
            out.write("Name,Nice Name,Points/Lvl,Metalworking,Woodworking,Textile Working,Alchemy,Magic,Weaponcrafting,Armorcrafting,Arts And Crafts,Jewelry,Rune Writing,Tinkering,Mastery\n")
            dl.workers.each {
                out.write("${getName(it.name + '_name')},${getName(it.name + '_nicename')},${it.pointsPerLevel},")
                out.write("${it.metalworking},${it.woodworking},${it.textileworking},${it.alchemy},${it.channeling},${it.weaponcrafting},")
                out.write("${it.armormaking},${it.craftsmanship},${it.jewelry},${it.enchanting},${it.tinkering},${it.mastery}\n")
            }
        }
    }

    def dumpRouletteLevels() {
        def getILvl = { desc, lvl ->
            return lvl + desc.substring(4).toInteger()
        }
        new File('out.csv').withWriter { out ->
            out.write("Player Level,Spin Cost,Rewards\n")
            dl.rouletteLevels.each { x ->
                def total = 0
                x.each { total += (it.key.startsWith('w') ? it.value : 0) }
                out.write("${x.level},${x.priceSpin},")
                out.write("${x.w1 / total},${x.gold / 2} Gold,${x.w2 / total},${x.gold} Gold,${x.w3 / total},${x.gold * 2} Gold,")
                out.write("${x.w4 / total},~${getILvl(x.roulette4, x.item)} Item,${x.w5 / total},~${getILvl(x.roulette5, x.item)} Item,${x.w6 / total},~${getILvl(x.roulette6, x.item)} Item,")
                out.write("${x.w7 / total},-Quest Mat,${x.w8 / total},+Quest Mat,")
                out.write("${x.w9 / total},${getName(x.key + '_name')},${x.w10 / total},Spin,${x.w11 / total},${x.gems} Gems,${x.w12 / total},Blueprint Fragment\n")
            }
        }
    }

    def dumpModules() {
        def moduleStats = [:]
        dl.modules.each { x ->
            if (x.appealScore > 0) {
                return
            }
            if (!moduleStats[x.family]) {
                moduleStats[x.family] = [:]
            }

            def info = []
            if (x.metalworking) info << x.metalworking + ' Metalworking'
            if (x.woodworking) info << x.woodworking + ' Woodworking'
            if (x.textileworking) info << x.textileworking + ' Textile Working'
            if (x.alchemy) info << x.alchemy + ' Alchemy'
            if (x.channeling) info << x.channeling + ' Magic'
            if (x.weaponcrafting) info << x.weaponcrafting + ' Weaponcrafting'
            if (x.armormaking) info << x.armormaking + ' Armorcrafting'
            if (x.craftsmanship) info << x.craftsmanship + ' Arts And Crafts'
            if (x.jewelry) info << x.jewelry + ' Jewelry'
            if (x.enchanting) info << x.enchanting + ' Rune Writing'
            if (x.tinkering) info << x.tinkering + ' Tinkering'
            if (x.energyCapacity) info << x.fusionMax + ' Energy Capacity'
            if (x.capacity) info << x.capacity + ' Capacity'
            if (x.resStorage) info << x.resStorage + ' Storage'
            if (x.energyBonus) info << x.energyBonus + ' Energy'
            if (x.storage) info << x.storage + ' Storage'
            if (x.fusionMax) info << x.fusionMax + ' Slots'

            moduleStats[x.family][x.level] = [ x.cost, formatSeconds(x.time), info.join(',') ]
        }

        new File('out.csv').withWriter { out ->
            out.write('Level,')
            moduleStats.each { family, levels ->
                out.write(family)
                out.write(',,,')
            }
            out.write('\n')

            for (int i = 1; i <= 15; i++) {
                out.write(i + ',')
                moduleStats.each { family, levels ->
                    if (levels.containsKey(i)) {
                        out.write(levels[i][0].toString())
                        out.write(',')
                        out.write(levels[i][1].toString())
                        out.write(',"')
                        out.write(levels[i][2] ?: '')
                        out.write('",')
                    }
                }
                out.write('\n')
            }
        }
    }

    def formatSeconds(time) {
        if (!time) return ''
        def hours = (int)(time / 3600)
        def minutes = (int)(time / 60) % 60
        def seconds = (int)time % 60

        return "${hours ? hours + 'H' : ''}${minutes ? minutes + 'M' : ''}${seconds ? seconds + 'S' : ''}"
    }

    def formatBoost(data) {
        def boost = data.split(',').collect {
            if (it.startsWith('boost.')) {
                it = it.substring(6)
            } else if (it.startsWith('xp.')) {
                it = it.substring(3)
            }

            def amt
            if (it.contains('-')) {
                def idx = it.indexOf('-')
                amt = it.substring(idx + 1).toDouble()
                if (amt < 1) {
                    amt = String.format('-%.1f%%', amt * 100)
                } else {
                    amt = String.format('-%d', (int)amt)
                }
                it = it.substring(0, idx)
            } else if (it.contains('+')) {
                def idx = it.indexOf('+')
                amt = it.substring(idx + 1).toDouble()
                if (amt < 1) {
                    amt = String.format('+%.1f%%', amt * 100)
                } else {
                    amt = String.format('+%d', (int)amt)
                }
                it = it.substring(0, idx)
            }
            return it + ' ' + amt
        }
        return "${boost.join(' ')}"
    }

    def getQuality(quality) {
        switch (quality) {
            case 0: return ''
            case 1: return 'Good '
            case 2: return 'Great '
            case 3: return 'Flawless '
            case 4: return 'Epic '
            case 5: return 'Legendary '
        }
    }

    def getItem(name) {
        return dl.items.find { it.name == name }
    }

    def getConstant(name) {
        return dl.constants.find { it.key == name }?.value
    }

    def getQuestDifficulty(difficulty, level, boss) {
        def mult = (boss ? getConstant("boss${level + 1}Mult") : getConstant("explore${level}Mult") ?: 1)
        def value = Math.pow(difficulty, getConstant('questDifficultyPower')) * 100 * mult

        def roundFactor
        if (value > 1000000)
            roundFactor = 50000
        else if (value > 100000)
            roundFactor = 5000
        else if (value > 10000)
            roundFactor = 500
        else if (value > 1000)
            roundFactor = 50
        else if (value > 50)
            roundFactor = 5
        else
            roundFactor = 2

        value = Math.round((int)((value + 1) / roundFactor)) * roundFactor
        return value
    }

    def getItemBonus(bonus) {
        if (bonus == null) {
            return ''
        } else if (bonus.startsWith('price*')) {
            return 'Gold +' + (int)((bonus.substring(6).toDouble() - 1) * 100) + '%'
        } else if (bonus.startsWith('quality=')) {
            return 'Base Quality ' + getQuality(bonus.substring(8).toInteger()).trim()
        } else if (bonus.startsWith('time*')) {
            return 'Time -' + (int)((1 - bonus.substring(5).toDouble()) * 100) + '%'
        } else if (bonus.contains('-')) {
            def (mat, amt) = bonus.split('-')
            return '-' + amt + ' ' + (getName(mat) ?: getName(mat + '_name'))
        } else if (bonus.startsWith('power=')) {
            def (skill, quality) = bonus.substring(6).split(',')
            return 'Skill ' + getName(skill + '_name') + ' at ' + getQuality(quality.toInteger()).trim()
        } else {
            return 'Unlocks ' + getName(bonus + '_name')
        }
    }

    def getName(item) {
        if (!dl.dictionary.get(item)) {
            return null
        }
        return dl.dictionary.get(item).split(' ')*.capitalize().join(' ')
    }

    def calcBestItem() {
        // Get base data
        def itemStats = [:]
        dl.items.each { item ->
            def skip = false
            // Skip 0 power items (keys, chests, quest rewards)
            if (item.power.toInteger() == 0) {
                skip = true
            }
            // Skip chest blueprints
            if (item.rare.toInteger() != 0 && item.level >= 10) {
                skip = true
            }
            // Skip certain materials
//            [ "leather", "herb" ].each { type ->
//                if (item[type].toInteger() != 0) {
//                    skip = true
//                }
//            }

            if (skip) {
                return
            }

            def reqs = [:]
            [ "iron", "wood", "leather", "herb", "steel", "hardwood", "fabric", "oil", "gems", "mana" ].each { type ->
                if (item[type].toInteger() != 0) {
                    reqs[type] = item[type].toInteger()
                }
            }
            if (item.item1 != '') {
                reqs[item.item1] = item.i1c.toInteger()
            }
            if (item.item2 != '') {
                reqs[item.item2] = item.i2c.toInteger()
            }

            def skills = [:]
            [ "metalworking", "woodworking", "textileworking", "alchemy", "channeling", "weaponcrafting", "armormaking", "craftsmanship", "jewelry", "enchanting", "tinkering" ].each { type ->
                if (item[type].toInteger() != 0) {
                    skills[type] = item[type].toInteger()
                }
            }

            itemStats[item.name] = [
                    name: item.name,
                    price: item.price.toDouble(),
                    power: item.power.toInteger(),
                    speed: 1,
                    quality: 0,
                    reqs: reqs,
                    skills: skills
            ]
        }

        // Apply level ups
        dl.items.each { theItem ->
            def item = itemStats[theItem.name]
            if (item != null) {
                ["l1B", "l2B", "l3B", "l4B", "l5B"].each { level ->
                    def bonus = theItem[level]
                    if (bonus.startsWith('price*')) {
                        item.price *= bonus.substring(6).toDouble()
                    } else if (bonus.startsWith('quality=')) {
                        item.quality = bonus.substring(8).toInteger()
                    } else if (bonus.startsWith('time*')) {
                        item.speed *= bonus.substring(5).toDouble()
                    } else if (bonus.contains('-')) {
                        def (mat, amt) = bonus.split('-')
                        item.reqs[mat] -= amt.toInteger()
                    } else {
                        // Recipe unlock or item skill
                    }
                }
            }
        }


        // Add in sub crafts
        itemStats = itemStats.sort { it.value.power }
        itemStats.each { name, item ->
            def newReqs = [:]
            def newSkills = [:]
            item.reqs.each { mat, amt ->
                if (itemStats.containsKey(mat)) {
                    addReqs(newReqs, itemStats[mat].reqs, amt)
                    addReqs(newSkills, itemStats[mat].skills, amt)
                }
            }
            addReqs(item.reqs, newReqs, 1)
            addReqs(item.skills, newSkills, 1)
        }

        // Determine best items
        def craftCap = 1000
        def myRates = [
                iron: 180,
                wood: 180,
                leather: 180,
                herb: 180,
                steel: 66,
                hardwood: 66,
                fabric: 66,
                oil: 66,
                gems: 13,
                mana: 13
        ]

    //        myRates.iron += 70
    //        myRates.wood += 70
    //        myRates.leather += 70
    //        myRates.herb += 70
    //
    //        myRates.steel += 25
    //        myRates.hardwood += 25
    //        myRates.fabric += 25
    //        myRates.oil += 25
    //
    //        myRates.gems += 5
    //        myRates.mana += 5


        //def bestMat = 'ironcarapace'
        def maxMat = 11
        def allowedMats = []
        def bannedMats = []
        def allowed = true
        dl.items.findAll { it.power == 0 }.each {
    //            if (allowed && it.name == bestMat) {
            if (allowed && allowedMats.size() == maxMat - 1) {
                allowed = false
                allowedMats << it.name
            } else if (allowed) {
                allowedMats << it.name
            } else {
                bannedMats << it.name
            }
        }

        def itemRates = [:]
        itemStats.each { name, item ->
            // Check for banned mats
            if (item.reqs.find { bannedMats.contains(it.key) }) {
                return
            }

            def craftTime = 0
            item.skills.each { skill, amt ->
                craftTime += amt / craftCap
            }
            craftTime *= item.speed

            def resourceTime = 0
            item.reqs.each { mat, amt ->
                if (myRates.containsKey(mat)) {
                    resourceTime += amt / (myRates[mat] * 3 / 60) // 3 bins, rates are per hour
                }
            }

            def realTime = Math.max(craftTime, resourceTime)

            def realPrice = item.price
            if (item.quality == 1) {
                realPrice *= 1.25
            } else if (item.quality == 2) {
                realPrice *= 2
            } else if (item.quality != 0) {
                throw new Exception('unexpected quality for ' + item.name)
            }

            itemRates[name] = realPrice / realTime // Gold per minute
        }
        itemRates = itemRates.sort { -it.value }
        itemRates = itemRates.collect { rate -> "${rate.value} ${rate.key} ${getItem(rate.key).level }" }

        def temp = 0
    }

    def addReqs(reqSrc, newReqs, amt) {
        newReqs.each { mat, val ->
            if (reqSrc.containsKey(mat)) {
                reqSrc[mat] += val * amt
            } else {
                reqSrc[mat] = val * amt
            }
        }
    }

}
