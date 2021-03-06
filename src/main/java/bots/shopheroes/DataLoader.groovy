package bots.shopheroes

//this.(.*) = reader.R(.*)\(\);
//$1: r$2(),
class DataLoader {

    def staticDataLocation
    def dictionaryLocation

    def bytes = []
    def pos = 0

    def achievements
    def adventures
    def city
    def constants
    def investCurves
    def craftCurves
    def goldCurves
    def questCurves
    def repairCurves
    def resourceT1Curves
    def resourceT2Curves
    def resourceT3Curves
    def healCurves
    def customers
    def dailyGoals
    def fameLevels
    def gemPackages
    def heroLevels
    def improvements
    def items
    def itemLevels
    def storeLayouts
    def modules
    def powers
    def quests
    def rouletteLevels
    def rouletteStreaks
    def shopkeeperCustomizations
    def shopLevels
    def craftSlotLevels
    def questSlotLevels
    def employeeSlotLevels
    def tradeSlotLevels
    def starterPackages
    def temporaryPowers
    def tutorials
    def workers
    def workerLevels
    def dictionary

    public DataLoader(staticDataLocation, dictionaryLocation) {
        this.staticDataLocation = staticDataLocation
        this.dictionaryLocation = dictionaryLocation
        run()
    }

    def run() {
        bytes = new File(staticDataLocation).bytes
        achievements = loadAchievements()
        adventures = loadAdventures()
        city = loadCity()
        constants = loadConstants()
        investCurves = loadCurves()
        craftCurves = loadCurves()
        goldCurves = loadCurves()
        questCurves = loadCurves()
        repairCurves = loadCurves()
        resourceT1Curves = loadCurves()
        resourceT2Curves = loadCurves()
        resourceT3Curves = loadCurves()
        healCurves = loadCurves()
        customers = loadCustomers()
        dailyGoals = loadDailyGoals()
        fameLevels = loadFameLevels()
        gemPackages = loadGemPackages()
        heroLevels = loadHeroLevels()
        improvements = loadImprovements()
        items = loadItems()
        itemLevels = loadItemLevels()
        storeLayouts = loadStoreLayouts()
        modules = loadModules()
        powers = loadPowers()
        quests = loadQuests()
        rouletteLevels = loadRouletteLevels()
        rouletteStreaks = loadRouletteStreaks()
        shopkeeperCustomizations = loadShopkeeperCustomizations()
        shopLevels = loadShopLevels()
        craftSlotLevels = loadCraftSlotLevels()
        questSlotLevels = loadQuestSlotLevels()
        employeeSlotLevels = loadEmployeeSlotLevels()
        tradeSlotLevels = loadTradeSlotLevels()
        starterPackages = loadStarterPackages()
        temporaryPowers = loadTemporaryPowers()
        tutorials = loadTutorials()
        workers = loadWorkers()
        workerLevels = loadWorkerLevels()
        dictionary = loadDictionary()
    }

    def loadDictionary() {
        pos = 0
        bytes = new File(dictionaryLocation).bytes

        def result = [:]
        try {
            while (true) {
                def count = readUInt16()
                for (int i = 0; i < count; i++) {
                    def key = readString()
                    def val = readString()
                    result[key] = val
                }
            }
        } catch (Exception e) {

        }

        return result
    }

    def loadAchievements() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    name: readString(),
                    uid: readString(),
                    parent: readString(),
                    family: readString(),
                    level: readByte(),
                    hidden: (readByte() == 0),
                    unlock: readString(),
                    threshold: readInt32(),
                    reward: readString(),
                    hash: readString()
            ]
        }
        return result
    }

    def loadAdventures() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    name: readString(),
                    uid: readString(),
                    parent: readString(),
                    difficulty: readInt32(),
                    duration: readInt32(),
                    lootGems: readInt32(),
                    reward: readString(),
                    q0: readString(),
                    q1: readString(),
                    q2: readString(),
                    q3: readString(),
                    q4: readString(),
                    q5: readString(),
                    ex1: readInt32(),
                    ex2: readInt32(),
                    ex3: readInt32(),
                    ex4: readInt32(),
                    ex5: readInt32(),
                    chest: readString(),
                    hash: readString()
            ]
        }
        return result
    }

    def loadCity() {
        def result = [
                name: readString(),
                w: readUInt16(),
                h: readUInt16(),
                buildings: [],
                improvements: []
        ]
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result.buildings << [
                    name: readString(),
                    x: readInt16(),
                    y: readInt16(),
                    w: readInt16(),
                    h: readInt16()
            ]
        }
        count = readUInt16()
        for (int i = 0; i < count; i++) {
            result.improvements << [
                    name: readString(),
                    show: (readByte() != 0),
                    x: readInt16(),
                    y: readInt16(),
                    w: readInt16(),
                    h: readInt16()
            ]
        }
        return result
    }

    def loadConstants() {
        def result = [
                startingGold: readInt32(),
                startingGems: readInt32(),
                startingModules: readString(),
                startingWorkers: readString(),
                startingInventory: readString(),
                defaultStorageCapacity: readInt32(),
                defaultEnergyCapacity: readInt32(),
                maxSlots: readInt32(),
                maxVisitors: readInt32(),
                maxEmployees: readInt32(),
                maxQuests: readInt32(),
                maxTrades: readInt32(),
                numItemLevels: readInt32(),
                maximumWorkerLevel: readInt32(),
                energyDiscountLevelRatio: readSingle(),
                energySurchargeLevelRatio: readSingle(),
                energySuggestLevelRatio: readSingle(),
                energyBargainLevelRatio: readSingle(),
                energyComplimentRefillRatio: readSingle(),
                energyIntelligenceMultiplier: readSingle(),
                energyPatienceMultiplier: readSingle(),
                energyProfBMult: readSingle(),
                energyProfCMult: readSingle(),
                energyProfDMult: readSingle(),
                energyMaxWaitCost: readSingle(),
                energyRefillCost: readInt32(),
                quality0Multiplier: readSingle(),
                quality1Multiplier: readSingle(),
                quality2Multiplier: readSingle(),
                quality3Multiplier: readSingle(),
                quality4Multiplier: readSingle(),
                quality5Multiplier: readSingle(),
                maximumAdventureFloors: readInt32(),
                companion1Level: readInt32(),
                companion2Level: readInt32(),
                companion3Level: readInt32(),
                companion4Level: readInt32(),
                companion5Level: readInt32(),
                companion0LootProb: readSingle(),
                companion1LootProb: readSingle(),
                companion2LootProb: readSingle(),
                companion3LootProb: readSingle(),
                companion4LootProb: readSingle(),
                companion5LootProb: readSingle(),
                companions1Loots: readInt32(),
                companions2Loots: readInt32(),
                companions3Loots: readInt32(),
                companions4Loots: readInt32(),
                companions5Loots: readInt32(),
                companions6Loots: readInt32(),
                minCustomerItemLevelDelta: readInt32(),
                maxCustomerItemLevelDelta: readInt32(),
                minCustomerSuggestLevelDelta: readInt32(),
                maxCustomerSuggestLevelDelta: readInt32(),
                maximumFameLevel: readInt32(),
                maximumHeroLevel: readInt32(),
                visitorItemInStockProbability: readSingle(),
                visitorItemCraftableProbability: readSingle(),
                surchargeRatio: readSingle(),
                discountRatio: readSingle(),
                visitorPercentageToBeSeller: readSingle(),
                sellerBaseRatio: readSingle(),
                sellerBargainRatio: readSingle(),
                sellerBargainMax: readSingle(),
                findCityLowerRange: readSingle(),
                findCityUpperRange: readInt32(),
                findCityMaximumLowerRangeValue: readInt32(),
                findCityMinimumUpperRangeValue: readInt32(),
                cityInactivityDays: readInt32(),
                rouletteLoseStreakThreshold: readInt32(),
                rouletteLoseStreakPassedThreshold: readInt32(),
                rouletteDailyReset: readInt32(),
                rouletteConsecutivePriceIncrease: readInt32(),
                tradeMaxGold: readInt32(),
                tradeMaxGems: readInt32(),
                tradeMaxQuantityOffer: readInt32(),
                tradeMaxQuantityRequest: readInt32(),
                tradeTaxGems: readSingle(),
                tradeTaxGold: readSingle(),
                tradeGemsMinLevel: readInt32(),
                tradeMinGems: readInt32(),
                tradeCancelDelayCooldown: readInt32(),
                tradeCancelCooldown: readInt32(),
                tradeItemMaxMultiplier: readInt32(),
                tradeSiMaxMultiplier: readInt32(),
                equipmentSetBonus: readSingle(),
                equipmentAdequacyThreshold: readSingle(),
                weaponProficiencyMultiplierA: readSingle(),
                weaponProficiencyMultiplierB: readSingle(),
                weaponProficiencyMultiplierC: readSingle(),
                weaponProficiencyMultiplierD: readSingle(),
                minimumCraftTimeSeconds: readInt32(),
                minimumQuestTimeSeconds: readInt32(),
                minimumInvestment: readSingle(),
                quality1pBase: readSingle(),
                quality1pInc: readSingle(),
                quality2pBase: readSingle(),
                quality2pInc: readSingle(),
                quality3pBase: readSingle(),
                quality3pInc: readSingle(),
                quality4pBase: readSingle(),
                quality4pInc: readSingle(),
                quality5pBase: readSingle(),
                quality5pInc: readSingle(),
                quality0EquipMultiplier: readSingle(),
                quality1EquipMultiplier: readSingle(),
                quality2EquipMultiplier: readSingle(),
                quality3EquipMultiplier: readSingle(),
                quality4EquipMultiplier: readSingle(),
                quality5EquipMultiplier: readSingle(),
                questEvenSurvivalRate: readSingle(),
                questSurvivalRateBonusPerCompanion: readSingle(),
                questDifficultyPower: readSingle(),
                adventureGoldMin: readSingle(),
                adventureGoldMax: readSingle(),
                questXpFactor: readSingle(),
                adventureXpFactor: readSingle(),
                itemAdequacyLevelDeltaValue: readSingle(),
                itemAdequacyProficiencyValue: readSingle(),
                itemAdequacyToBreakChancePower: readSingle(),
                chestWoodContentLevelFloor: readInt32(),
                chestLeatherContentLevelFloor: readInt32(),
                chestIronContentLevelFloor: readInt32(),
                chestGoldContentLevelFloor: readInt32(),
                chestMagicContentLevelFloor: readInt32(),
                minimumBreakChance: readSingle(),
                zeroBreakChanceThreshold: readSingle(),
                breakChanceQuality1Mult: readSingle(),
                breakChanceQuality2Mult: readSingle(),
                breakChanceQuality3Mult: readSingle(),
                breakChanceQuality4Mult: readSingle(),
                breakChanceQuality5Mult: readSingle(),
                heroValueMultiplier: readSingle(),
                questRewardXpMultiplier: readSingle(),
                questRewardXpPower: readSingle(),
                fusionFailprob2: readSingle(),
                fusionFailprob3: readSingle(),
                fusionFailprob4: readSingle(),
                fusionFailprob5: readSingle(),
                fusionQuality1Weight: readInt32(),
                fusionQuality2Weight: readInt32(),
                fusionQuality3Weight: readInt32(),
                fusionQuality4Weight: readInt32(),
                fusionQuality5Weight: readInt32(),
                fusionQuality1Power: readInt32(),
                fusionQuality2Power: readInt32(),
                fusionQuality3Power: readInt32(),
                fusionQuality4Power: readInt32(),
                fusionQuality5Power: readInt32(),
                brokenItemBasePercentage: readSingle(),
                brokenItemLevelDifferenceMultiplier: readSingle(),
                brokenItemQualityMultiplier: readSingle(),
                workerRespecCostPerDisciplines: readInt32(),
                workerSkillReset: readInt32(),
                medalsDecayThreshold: readInt32(),
                borrowHeroMax: readInt32(),
                borrowHeroReset: readInt32(),
                workerResetSkillsFactor: readSingle(),
                boostDurationSeconds: readInt32(),
                cityUnlockInvestStep: readInt32(),
                cityUnlockInvestTotal: readInt32(),
                hireLevelPower: readSingle(),
                difficultyXpMultiplierPerLevel: readSingle(),
                cityJoinCooldown: readInt32(),
                autoCraftPriceOnce: readInt32(),
                questHealAllDiscount: readSingle(),
                questRepairAllDiscount: readSingle(),
                explore1Mult: readSingle(),
                explore2Mult: readSingle(),
                explore3Mult: readSingle(),
                explore4Mult: readSingle(),
                explore5Mult: readSingle(),
                boss1Mult: readSingle(),
                boss2Mult: readSingle(),
                boss3Mult: readSingle(),
                boss4Mult: readSingle(),
                boss5Mult: readSingle(),
                precraftSlot1Price: readInt32(),
                precraftSlot2Price: readInt32(),
                precraftSlot3Price: readInt32(),
                precraftSlot4Price: readInt32(),
                precraftSlot5Price: readInt32(),
                precraftSlot6Price: readInt32(),
                precraftSlot7Price: readInt32(),
                precraftSlot8Price: readInt32(),
                precraftSlot9Price: readInt32(),
                blueprintCostLevelMult: readSingle(),
                blueprintCostLevelPow: readSingle(),
                reportsMax: readInt32(),
                reportsRegenRate: readInt32()
        ]
        return result
    }

    def loadCurves() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    value: readInt64(),
                    target: readInt64(),
                    unit: readDouble()
            ]
        }
        return result
    }

    def loadCustomers() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    name: readString(),
                    uid: readString(),
                    level: readInt16(),
                    flag: readInt32(),
                    recruit: readInt16(),
                    gender: readString(),
                    classtype: readString(),
                    multiplier: readSingle(),
                    power: readInt32(),
                    intelligence: readByte(),
                    sociability: readByte(),
                    patience: readByte(),
                    ws: readString(),
                    wa: readString(),
                    wd: readString(),
                    wp: readString(),
                    wm: readString(),
                    wt: readString(),
                    wb: readString(),
                    wg: readString(),
                    ah: readString(),
                    am: readString(),
                    al: readString(),
                    hh: readString(),
                    hl: readString(),
                    gh: readString(),
                    gl: readString(),
                    bh: readString(),
                    bl: readString(),
                    s: readString(),
                    uh: readString(),
                    up: readString(),
                    us: readString(),
                    uw: readString(),
                    xl: readString(),
                    xp: readString(),
                    xr: readString(),
                    weapon: readString(),
                    armor: readString(),
                    body: readString(),
                    usable: readString(),
                    jewel: readString(),
                    passByLvl: readByte(),
                    l1: readByte(),
                    l1P: readString(),
                    l2: readByte(),
                    l2P: readString(),
                    l3: readByte(),
                    l3P: readString(),
                    miniAsset: readString(),
                    hdAsset: readString(),
                    hash: readString(),
            ]
        }
        return result
    }

    def loadDailyGoals() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    name: readString(),
                    uid: readString(),
                    type: readString(),
                    description: readString(),
            ]
        }
        return result
    }

    def loadFameLevels() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    xp: readInt32(),
                    investment: readInt32(),
                    gems: readInt32(),
            ]
        }
        return result
    }

    def loadGemPackages() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    name: readString(),
                    uid: readString(),
                    priority: readInt32(),
                    info: readInt32(),
                    gems: readInt32(),
                    kreds: readInt32(),
                    days: readInt32(),
                    usd: readString(),
                    hash: readString(),
            ]
        }
        return result
    }

    def loadHeroLevels() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    xp: readInt32(),
                    power: readInt32(),
            ]
        }
        return result
    }

    def loadImprovements() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            def improvement = [
                    name: readString(),
                    uid: readString(),
                    family: readString(),
                    level: readInt32(),
                    amount: readInt64(),
                    parent: readString(),
                    population: readInt16(),
                    tavern: readByte(),
                    resource: readString(),
                    resourceRate: readInt16(),
                    worker: readString(),
                    workerMaxLevel: readByte(),
                    hero: readString(),
                    heroMaxLevel: readByte(),
                    adventure: readString(),
                    adventureMaxBoss: readByte(),
                    collective: readString(),
                    individual: readString(),
                    collectives: [],
                    individuals: []
            ]
            def count2 = readUInt16()
            for (int j = 0; j < count2; j++) {
                improvement.collectives << [
                        modifies: readString(),
                        val: readSingle(),
                        op: readString(),
                        type: readInt32(),
                ]
            }
            count2 = readUInt16()
            for (int j = 0; j < count2; j++) {
                improvement.individuals << [
                        modifies: readString(),
                        val: readSingle(),
                        op: readString(),
                        type: readInt32(),
                ]
            }
            improvement.hash = readString()
            result << improvement
        }
        return result
    }

    def loadItems() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    name: readString(),
                    uid: readString(),
                    level: readInt16(),
                    price: readInt32(),
                    power: readInt32(),
                    xp: readInt32(),
                    type: readString(),
                    rare: readUInt16(),
                    iron: readUInt16(),
                    wood: readUInt16(),
                    leather: readUInt16(),
                    herb: readUInt16(),
                    steel: readUInt16(),
                    hardwood: readUInt16(),
                    fabric: readUInt16(),
                    oil: readUInt16(),
                    gems: readUInt16(),
                    mana: readUInt16(),
                    item1: readString(),
                    i1t: readString(),
                    i1q: readByte(),
                    i1c: readByte(),
                    item2: readString(),
                    i2t: readString(),
                    i2q: readByte(),
                    i2c: readByte(),
                    metalworking: readInt32(),
                    woodworking: readInt32(),
                    textileworking: readInt32(),
                    alchemy: readInt32(),
                    channeling: readInt32(),
                    weaponcrafting: readInt32(),
                    armormaking: readInt32(),
                    craftsmanship: readInt32(),
                    jewelry: readInt32(),
                    enchanting: readInt32(),
                    tinkering: readInt32(),
                    l1: readUInt16(),
                    l2: readUInt16(),
                    l3: readUInt16(),
                    l4: readUInt16(),
                    l5: readUInt16(),
                    l1B: readString(),
                    l2B: readString(),
                    l3B: readString(),
                    l4B: readString(),
                    l5B: readString(),
                    hash: readString(),
            ]
        }
        return result
    }

    def loadItemLevels() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    value: readInt32(),
                    xp: readInt32(),
                    fusionTime: readInt32(),
            ]
        }
        return result
    }

    def loadStoreLayouts() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            // Discard data
            readString()
            for (int j = 0; j < 6; j++) {
                def outerCount = readUInt16()
                for (int k = 0; k < outerCount; k++) {
                    def innerCount = readUInt16()
                    for (int l = 0; l < innerCount; l++) {
                        readByte()
                    }
                }
            }
        }
        return result
    }

    def loadModules() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    name: readString(),
                    uid: readString(),
                    family: readString(),
                    type: readString(),
                    level: readByte(),
                    levelRequirement: readByte(),
                    cost: readInt32(),
                    costGold: readInt32(),
                    time: readInt32(),
                    anchor: readString(),
                    width: readByte(),
                    height: readByte(),
                    maximum: readUInt16(),
                    metalworking: readUInt16(),
                    woodworking: readUInt16(),
                    textileworking: readUInt16(),
                    alchemy: readUInt16(),
                    channeling: readUInt16(),
                    weaponcrafting: readUInt16(),
                    armormaking: readUInt16(),
                    craftsmanship: readUInt16(),
                    jewelry: readUInt16(),
                    enchanting: readUInt16(),
                    tinkering: readUInt16(),
                    appealScore: readInt32(),
                    capacity: readInt32(),
                    resStorage: readInt32(),
                    energyCapacity: readInt32(),
                    energyBonus: readInt32(),
                    storage: readInt32(),
                    energyItemtype: readString(),
                    resource: readString(),
                    fusionMax: readInt32(),
                    fusionSlots: readInt32(),
                    hash: readString(),
            ]
        }
        return result
    }

    def loadPowers() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    name: readString(),
                    uid: readString(),
                    target: readString(),
                    condition: readString(),
                    modifies: readString(),
                    hash: readString(),
            ]
        }
        return result
    }

    def loadQuests() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    name: readString(),
                    uid: readString(),
                    index: readByte(),
                    difficulty: readUInt16(),
                    level: readUInt16(),
                    duration: readInt64(),
                    hero: readString(),
                    equipment0: readString(),
                    q0: readByte(),
                    companion1: readString(),
                    equipment1: readString(),
                    q1: readByte(),
                    companion2: readString(),
                    equipment2: readString(),
                    q2: readByte(),
                    companion3: readString(),
                    equipment3: readString(),
                    q3: readByte(),
                    companion4: readString(),
                    equipment4: readString(),
                    q4: readByte(),
                    companion5: readString(),
                    equipment5: readString(),
                    q5: readByte(),
                    reward: readString(),
            ]
        }
        return result
    }

    def loadRouletteLevels() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    level: readInt64(),
                    priceSpin: readInt64(),
                    gold: readInt64(),
                    gems: readInt64(),
                    key: readString(),
                    item: readInt64(),
                    roulette1: readString(),
                    w1: readInt64(),
                    roulette2: readString(),
                    w2: readInt64(),
                    roulette3: readString(),
                    w3: readInt64(),
                    roulette4: readString(),
                    w4: readInt64(),
                    roulette5: readString(),
                    w5: readInt64(),
                    roulette6: readString(),
                    w6: readInt64(),
                    roulette7: readString(),
                    w7: readInt64(),
                    roulette8: readString(),
                    w8: readInt64(),
                    roulette9: readString(),
                    w9: readInt64(),
                    roulette10: readString(),
                    w10: readInt64(),
                    roulette11: readString(),
                    w11: readInt64(),
                    roulette12: readString(),
                    w12: readInt64(),
            ]
        }
        return result
    }

    def loadRouletteStreaks() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    streak: readInt64(),
                    type: readString(),
                    effect: readString(),
            ]
        }
        return result
    }

    def loadShopkeeperCustomizations() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    name: readString(),
                    uid: readString(),
                    gender: readString(),
                    family: readString(),
                    cost: readInt32(),
                    level: readInt32(),
                    assetName: readString(),
                    fullHead: (readByte() != 0),
                    hash: readString(),
            ]
        }
        return result
    }

    def loadShopLevels() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    cost: readInt64(),
                    gems: readInt32(),
                    maximumVisitors: readByte(),
                    maximumFurniture: readByte(),
            ]
        }
        return result
    }

    def loadCraftSlotLevels() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    gold: readInt32(),
                    level: readByte(),
                    gems: readInt32(),
            ]
        }
        return result
    }

    def loadQuestSlotLevels() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    gold: readInt32(),
                    level: readByte(),
                    gems: readInt32(),
            ]
        }
        return result
    }

    def loadEmployeeSlotLevels() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    gold: readInt32(),
                    level: readByte(),
                    gems: readInt32(),
            ]
        }
        return result
    }

    def loadTradeSlotLevels() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    gold: readInt32(),
                    level: readByte(),
                    gems: readInt32(),
            ]
        }
        return result
    }

    def loadStarterPackages() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    name: readString(),
                    uid: readString(),
                    priority: readInt32(),
                    usd: readString(),
                    flag: readInt32(),
                    expiry: readInt32(),
                    starts: readString(),
                    expiresFrom: readString(),
                    gems: readInt32(),
                    kreds: readInt32(),
                    days: readInt32(),
                    gold: readInt32(),
                    items: readString(),
                    slots: readString(),
                    blueprints: readString(),
                    modules: readString(),
                    customizations: readString(),
                    hash: readString(),
            ]
        }
        return result
    }

    def loadTemporaryPowers() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    name: readString(),
                    uid: readString(),
                    key: readString(),
                    duration: readInt64(),
                    power: readString(),
                    cost: readInt32(),
                    hash: readString(),
            ]
        }
        return result
    }

    def loadTutorials() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    name: readString(),
                    uid: readString(),
                    level: readInt64(),
                    location: readString(),
                    index: readByte(),
                    order: readByte(),
                    ignoreObjective: readString(),
                    hash: readString(),
            ]
        }
        return result
    }

    def loadWorkers() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    name: readString(),
                    uid: readString(),
                    flag: readInt32(),
                    metalworking: readUInt16(),
                    woodworking: readUInt16(),
                    textileworking: readUInt16(),
                    alchemy: readUInt16(),
                    channeling: readUInt16(),
                    weaponcrafting: readUInt16(),
                    armormaking: readUInt16(),
                    craftsmanship: readUInt16(),
                    jewelry: readUInt16(),
                    enchanting: readUInt16(),
                    tinkering: readUInt16(),
                    mastery: readUInt16(),
                    startingRecipes: readString(),
                    xpScale: readSingle(),
                    payBase: readInt32(),
                    payScale: readSingle(),
                    pointsPerLevel: readUInt16(),
                    hash: readString(),
            ]
        }
        return result
    }

    def loadWorkerLevels() {
        def result = []
        def count = readUInt16()
        for (int i = 0; i < count; i++) {
            result << [
                    xp: readInt32(),
            ]
        }
        return result
    }

    def readString() {
        def len = read7BitEncodedInt()
        if (len < 0) {
            throw new Exception("" + len)
        }

        if (len == 0) {
            return ''
        }

        def result = new String(bytes, pos, len)
        pos += len
        return result
    }

    def read7BitEncodedInt() {
        int ret = 0
        int shift = 0

        int len
        for (len = 0; len < 5; len++) {
            byte b = bytes[pos + len] & 0xFF

            ret = ret | ((b & 0x7f) << shift);
            shift += 7;
            if ((b & 0x80) == 0)
                break;
        }

        pos += len + 1

        if (len < 5) {
            return ret
        } else {
            throw new Exception()
        }
    }

    def readUInt16() {
        def result = ((bytes[pos + 1] & 0xFF) << 8) | (bytes[pos] & 0xFF)
        pos += 2
        return result
    }

    def readInt16() {
        return readUInt16()
    }

    def readInt32() {
        def result = ((bytes[pos + 3] & 0xFF) << 24) | ((bytes[pos + 2] & 0xFF) << 16) | ((bytes[pos + 1] & 0xFF) << 8) | (bytes[pos] & 0xFF)
        pos += 4
        return result
    }

    def readInt64() {
        def result = ((bytes[pos + 7] & 0x00FF) << 56) | ((bytes[pos + 6] & 0x00FF) << 48) | ((bytes[pos + 5] & 0x00FF) << 40) | ((bytes[pos + 4] & 0x00FF) << 32) |
                ((bytes[pos + 3] & 0x00FF) << 24) | ((bytes[pos + 2] & 0x00FF) << 16) | ((bytes[pos + 1] & 0x00FF) << 8) | (bytes[pos] & 0x00FF)
        pos += 8
        return result
    }

    def readByte() {
        def result = bytes[pos] & 0xFF
        pos++
        return result
    }

    def readSingle() {
        return Float.intBitsToFloat(readInt32())
    }

    def readDouble() {
        return Double.longBitsToDouble(readInt64())
    }
}
