package app.aaps.core.main.extensions

import app.aaps.core.data.db.GlucoseUnit
import app.aaps.core.data.db.IDs
import app.aaps.core.data.db.TE
import app.aaps.core.data.pump.defs.PumpType

fun TE.Companion.asAnnouncement(error: String, pumpId: Long? = null, pumpType: PumpType? = null, pumpSerial: String? = null): TE =
    TE(
        timestamp = System.currentTimeMillis(),
        type = TE.Type.ANNOUNCEMENT,
        duration = 0, note = error,
        enteredBy = "AAPS",
        glucose = null,
        glucoseType = null,
        glucoseUnit = GlucoseUnit.MGDL,
        ids = IDs(
            pumpId = pumpId,
            pumpType = pumpType,
            pumpSerial = pumpSerial
        )
    )
