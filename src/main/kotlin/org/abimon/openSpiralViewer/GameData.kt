package org.abimon.openSpiralViewer

import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.game.hpa.HopesPeakKillingGame

object GameData {
    val HEX_CODES: Map<HopesPeakKillingGame, Map<Int, String>> by lazy {
        mapOf(
                DR1 to mapOf(
                        0 to "FFFFFF",
                        1 to "B766F4",
                        2 to "5C1598",
                        3 to "DEAB00",
                        4 to "54E1FF",
                        23 to "52FF13"
                ),
                DR2 to mapOf(
                        0 to "FFFFFF",
                        1 to "B766F4",
                        2 to "5C1598",
                        3 to "DEAB00",
                        4 to "54E1FF",
                        5 to "383838",
                        6 to "52FF13",
                        10 to "FE0008",
                        11 to "1A51E8",
                        33 to "FF6A6E",
                        34 to "6DCAFF",
                        45 to "252525",
                        47 to "3F3F3F",
                        48 to "585858",
                        61 to "FF9900"
                )
        )
    }
}