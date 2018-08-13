package org.abimon.openSpiralViewer

import ColourFX
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.images.FontMap

enum class DanganFont(val fontMap: FontMap, val image: Image, val missingChar: Char? = null) {
    DR1_MAIN(
            FontMap { DanganFont::class.java.classLoader.getResourceAsStream("font/dr1/main.dat") }!!,
            DanganFont::class.java.classLoader.getResourceAsStream("font/dr1/main.png").use(::Image),
            '※'
    );

    val fontImage = FontImage(image)

    operator fun component1(): FontMap = fontMap
    operator fun component2(): FontImage = fontImage
    operator fun component3(): Char? = missingChar
//    operator fun component4(): Map<Int, Image> {
//
//    }

    open class FontImage(val sourceImage: Image) {
        private val width: Int = sourceImage.width.toInt()
        private val height: Int = sourceImage.height.toInt()
        private val images: MutableMap<Int, Image> = HashMap()

        operator fun get(colourCode: Int): Image? {
            if (colourCode in images)
                return images[colourCode]

            val hex = GameData.HEX_CODES[DR1]?.get(colourCode) ?: return null
            val img = this[ColourFX.web("#$hex")]
            images[colourCode] = img

            return img
        }

        operator fun get(colourFX: ColourFX): Image {
            val output = WritableImage(width, height)

            val reader = sourceImage.pixelReader
            val writer = output.pixelWriter

            val r = (colourFX.red * 255).toInt()
            val g = (colourFX.green * 255).toInt()
            val b = (colourFX.blue * 255).toInt()

            for (x in 0 until width) {
                for (y in 0 until height) {
                    val argb = reader.getArgb(x, y)
                    val a = (argb shr 24) and 0xFF
                    writer.setArgb(x, y, (a shl 24) or (r shl 16) or (g shl 8) or b)
                }
            }

            return output
        }
    }
}