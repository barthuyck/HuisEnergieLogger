package be.huyck.huisenergielogger.modellen

import java.time.LocalDateTime

data class RegistratieGegevens (
    var registratiedatum: LocalDateTime,
    var meterwaardeElekImport: Double,
    var meterwaardeElekExport: Double,
    var meterwaardeGas: Double,
    var meterwaardeWater: Double,
    var meterwaardePv1: Double,
    var meterwaardePv2: Double,
    var verschilElekImport: Double,
    var verschilElekExport: Double,
    var verschilGas: Double,
    var verschilWater: Double,
    var verschilPv1: Double,
    var verschilPv2: Double,
    var firebaseid: String
)

