package be.huyck.huisenergielogger.modellen

import java.time.LocalDateTime

data class RegistratieGegevens (
    var registratiedatum: LocalDateTime,
    var meterwaarde_el: Double,
    var meterwaarde_ga: Double,
    var meterwaarde_wa: Double,
    var meterwaarde_pv: Double,
    var verschil_el: Double,
    var verschil_ga: Double,
    var verschil_wa: Double,
    var verschil_pv: Double,
    var firebaseid: String
)