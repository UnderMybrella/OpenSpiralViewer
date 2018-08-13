package org.abimon.openSpiralViewer

import ColourFX
import javafx.scene.image.Image

class DanganScene {
    companion object {
        val TEXT_BACKDROP = DanganScene::class.java.classLoader.getResourceAsStream("ui/dr1/text_backdrop.png").use(::Image)

        val TEXT_OTHERS_DAY = DanganScene::class.java.classLoader.getResourceAsStream("ui/dr1/text_others_day.png").use(::Image)
        val TEXT_OTHERS_NIGHT = DanganScene::class.java.classLoader.getResourceAsStream("ui/dr1/text_others_night.png").use(::Image)
        val TEXT_OTHERS_UNK = DanganScene::class.java.classLoader.getResourceAsStream("ui/dr1/text_others_unk.png").use(::Image)

        val TEXT_PROTAG_DAY_THINK = DanganScene::class.java.classLoader.getResourceAsStream("ui/dr1/text_protagonist_think_day.png").use(::Image)
        val TEXT_PROTAG_NIGHT_THINK = DanganScene::class.java.classLoader.getResourceAsStream("ui/dr1/text_protagonist_think_night.png").use(::Image)
        val TEXT_PROTAG_UNK_THINK = DanganScene::class.java.classLoader.getResourceAsStream("ui/dr1/text_protagonist_think_unk.png").use(::Image)

        val TEXT_PROTAG_DAY_SPEAK = DanganScene::class.java.classLoader.getResourceAsStream("ui/dr1/text_protagonist_speak_day.png").use(::Image)
        val TEXT_PROTAG_NIGHT_SPEAK = DanganScene::class.java.classLoader.getResourceAsStream("ui/dr1/text_protagonist_speak_night.png").use(::Image)
        val TEXT_PROTAG_UNK_SPEAK = DanganScene::class.java.classLoader.getResourceAsStream("ui/dr1/text_protagonist_speak_unk.png").use(::Image)
    }

    var gameVisible: Boolean = false
    var colour: ColourFX = ColourFX.TRANSPARENT

    var speakerIsProtag: Boolean = false
    var speaker: String? = null
    var text: String? = null

    var timeOfDay: TimeOfDay = TimeOfDay.UNK
    var font: DanganFont = DanganFont.DR1_MAIN

    fun reset() {
        gameVisible = false
        colour = ColourFX.TRANSPARENT

        speakerIsProtag = false
        speaker = null
        text = null

        timeOfDay = TimeOfDay.UNK
        font = DanganFont.DR1_MAIN
    }
}