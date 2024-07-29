package be.huyck.huisenergielogger.modellen

data class JsonGegevens (
    var metertijd: String, // "22/07/2024, 23:59"
    var metertijdiso: String, //"2024-07-22T23:59:04.567426"
    var gasmeter: String, // 63.357,
    var watermeter: String, // 1097.1265,
    var elpvHmeter: String, // 10523.61,
    var elpvGmeter: String, // 1364.85,
    var elNETmeter: String, // -707.166,
    var elNETimport: String, // 183.89,
    var elNETexport: String, // 891.056
)
