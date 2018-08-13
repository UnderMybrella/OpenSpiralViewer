package org.abimon.openSpiralViewer

import ColourFX
import javafx.application.Application
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import javafx.scene.web.WebView
import javafx.stage.Stage
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import org.abimon.osl.OSL
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.scripting.Lin
import org.abimon.spiral.core.objects.scripting.lin.*
import org.abimon.spiral.core.utils.DataHandler
import java.io.File
import java.io.FileInputStream
import java.text.DecimalFormat
import kotlinx.coroutines.experimental.javafx.JavaFx as UI
import kotlinx.coroutines.experimental.launch as launchCoroutine

class OpenSpiralViewer : Application() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val jsonParser = OSL.JsonParser()

            DataHandler.stringToMap = { string -> jsonParser.parse(string) }
            DataHandler.streamToMap = { stream -> jsonParser.parse(String(stream.readBytes())) }

            Application.launch(OpenSpiralViewer::class.java, *args)
        }


    }

    val root = GridPane()
    val scene: Scene = Scene(root)

    val canvas: Canvas = Canvas(960.0, 560.0).apply {
        //        widthProperty().addListener(InvalidationListener {
//            graphics.fill = ColourFX.BLANCHEDALMOND
//            graphics.fillRect(0.0, 0.0, widthProperty().value, heightProperty().get())
//        })
//
//        heightProperty().addListener(InvalidationListener {
//            graphics.fill = ColourFX.BLANCHEDALMOND
//            graphics.fillRect(0.0, 0.0, widthProperty().value, heightProperty().get())
//        })
//
//        widthProperty().addListener { observable, oldValue, newValue -> println() }
    }

    val graphics = canvas.graphicsContext2D

    val webView = WebView()

    val scriptNodes = ListView<LinScript>().apply {
        this.setCellFactory {
            object : ListCell<LinScript>() {
                override fun updateItem(item: LinScript?, empty: Boolean) {
                    super.updateItem(item, empty)

                    if (empty || item == null) {
                        text = null
                    } else {
                        text = "0x${item.opCode.toString(16)}"
                    }
                }
            }
        }

        this.selectionModel.selectedItemProperty().addListener { observable, oldValue, newValue ->
            launchCoroutine {
                recalculate(selectionModel.selectedIndex)
                withContext(UI) {
                    redraw()
                }

                scriptIndex = selectionModel.selectedIndex
            }

            opCodeView.content = null

            var text: String? = null

            when (newValue) {
                is TextCountEntry -> text = "There are ${newValue.lines} of text in this file"
                is TextEntry -> text = newValue.text
                is SpriteEntry -> {
                    val view = FileInputStream(File("/Users/undermybrella/Workspace/KSPIRAL/shinkiro/dr1_data/Dr1/data/all/cg/bustup_00_01.png")).use { stream ->
                        val imageView = ImageView(Image(stream))

//                        imageView.fitWidth = opCodePane.width
                        imageView.fitHeight = opCodeView.height
                        imageView.isPreserveRatio = true

                        return@use imageView
                    }

                    println(view.image?.width)
                    println(opCodeView.width)
                    println(view.boundsInLocal.maxX)

                    opCodeView.content = view
                    opCodeView.hvalue = (view.boundsInLocal.maxX / 2) / view.boundsInLocal.maxX
                }
                is ScreenFadeEntry -> {
                    text = buildString {
                        append("Fade")

                        if (newValue.fadeIn)
                            append(" in from ")
                        else
                            append(" out to ")

                        when (newValue.colour) {
                            0 -> append("black (special)")
                            1 -> append("black")
                            2 -> append("white")
                            3 -> append("red")
                            else -> append("{${newValue.colour}}")
                        }

                        append(" for ${DecimalFormat("#.##").format(newValue.frameDuration / 60.0)} seconds")
                    }
                }
            }

            text = text?.replace("\n", "<br/>")

            if (text != null) {
                val split = LinTextParser.splitUp(text!!)

                var open = false

                text = ""

                split.forEach { param ->
                    when (param) {
                        is String -> text += param
                        is Int -> {
                            if (param > 0) {
                                if (open)
                                    text += "</a>"
                                text += "<a style=\"color: #${GameData.HEX_CODES[DR1]?.get(param) ?: "FFFFFF"}\">"
                                open = true
                            } else {
                                text += "</a>"
                                open = false
                            }
                        }
                    }
                }

                webView.engine.loadContent("<p style=\"color: white; background-color: black\">$text</p>")
                opCodeView.content = webView
            }
        }
    }

    val gameDataScrollPane = ScrollPane()
    val gameDataTabPane = TabPane().apply { gameDataScrollPane.content = this }

    val sceneDataTable = TableView<Pair<String, Any>>().apply {
        this.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
        this.isEditable = true

        val keyColumn = TableColumn<Pair<String, Any>, String>("Key")
        val valueColumn = TableColumn<Pair<String, Any>, Any>("Value")

        keyColumn.setCellValueFactory { columnFeatures -> ReadOnlyObjectWrapper(columnFeatures.value.first) }
        valueColumn.setCellValueFactory { columnFeatures -> ReadOnlyObjectWrapper(columnFeatures.value.second) }

        valueColumn.setCellFactory { column -> DanganSceneCell() }

        valueColumn.setOnEditCommit { event ->
            val key = event.rowValue.first

            when (key) {
                "text" -> {
                    danganScene.text = event.newValue as? String
                    event.tableView.items[event.tablePosition.row] = key to event.newValue
                }
            }

            redraw()
        }

        this.columns.setAll(keyColumn, valueColumn)

        val tab = Tab("Scene Data", this)
        tab.isClosable = false
        gameDataTabPane.tabs.add(tab)
    }

    val opCodeView = ScrollPane()

    var linFile: Lin? = null
    var scriptIndex = 0
    var danganScene = DanganScene()

    lateinit var primaryStage: Stage

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     *
     *
     * NOTE: This method is called on the JavaFX Application Thread.
     *
     *
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set. The primary stage will be embedded in
     * the browser if the application was launched as an applet.
     * Applications may create other stages, if needed, but they will not be
     * primary stages and will not be embedded in the browser.
     */
    override fun start(primaryStage: Stage) {
        this.primaryStage = primaryStage

        root.hgap = 8.0
        root.vgap = 8.0
        root.padding = Insets(5.0)

        for (i in 0 until 2) {
            val column = ColumnConstraints()
            column.hgrow = Priority.ALWAYS
            root.columnConstraints.add(column)
        }

//        val label = Label("Hey you fuck")
//
//        GridPane.setHalignment(label, HPos.CENTER)
//
//        root.add(label, 0, 0)

//        val lbl = Label("Name:")
//        val field = TextField()
//        val view = ListView<String>()
//        val okBtn = Button("OK")
//        val closeBtn = Button("Close")
//
//        view.selectionModel.selectionModeProperty().set(SelectionMode.MULTIPLE)
//
//        view.items.addAll(arrayOf("Danganronpa: Trigger Happy Havoc", "Danganronpa 2: Goodbye Despair", "Danganronpa V3: Killing Harmony"))
//
//        field.setOnKeyPressed { event ->
//            if (event.code == KeyCode.ENTER) {
//                val text = field.text
//                view.items.add(text)
//                field.text = ""
//            }
//        }
//
//        okBtn.setOnAction { event ->
//            println(view.selectionModel.selectedItems)
//        }
//
//        closeBtn.setOnAction { event ->
//            Platform.exit()
//        }
//
//        GridPane.setHalignment(okBtn, HPos.RIGHT)

//        root.add(lbl, 0, 0)

        root.add(canvas, 0, 0)
        root.add(scriptNodes, 1, 0)
        root.add(gameDataScrollPane, 0, 1)
        root.add(opCodeView, 1, 1)

        gameDataScrollPane.prefViewportWidth = canvas.width
        gameDataScrollPane.prefViewportHeight = canvas.height / 2

        sceneDataTable.prefWidth = gameDataScrollPane.prefViewportWidth

        opCodeView.prefViewportWidth = canvas.width / 2
        opCodeView.prefViewportHeight = canvas.height / 2

        //root.isGridLinesVisible = true
        primaryStage.scene = scene
        primaryStage.show()

        launchCoroutine(UI) {
            redraw()

            withContext(CommonPool) {
                linFile = Lin(DR1) { FileInputStream(File("/Users/undermybrella/Workspace/KSPIRAL/shinkiro_raw/dr1_data_us/Dr1/data/us/script/e00_001_000.lin")) }
            }

            scriptNodes.items.clear()
            linFile?.entries?.let(scriptNodes.items::addAll)
        }

        primaryStage.widthProperty().addListener { observable, oldValue, newValue -> resize(oldValue.toDouble(), newValue.toDouble(), primaryStage.height, primaryStage.height) }
        primaryStage.heightProperty().addListener { observable, oldValue, newValue -> resize(primaryStage.width, primaryStage.width, oldValue.toDouble(), newValue.toDouble()) }
    }

    fun recalculate(newIndex: Int) {
        val start: Int

        if (newIndex > scriptIndex) { //We can just go from where we are!
            start = scriptIndex + 1
        } else {
            start = 0
            danganScene.reset()
        }

        if (linFile == null)
            return

        danganScene.apply {
            for (i in start until (newIndex + 1)) {
                val entry = linFile!!.entries[i]

                when (entry) {
                    is TextEntry -> this.text = entry.text
                    is ScreenFadeEntry -> {
                        if (entry.fadeIn) {
                            colour = ColourFX.TRANSPARENT
                            gameVisible = true
                        } else {
                            when (entry.colour) {
                                0 -> colour = ColourFX.BLACK
                                1 -> colour = ColourFX.BLACK
                                2 -> colour = ColourFX.WHITE
                                3 -> colour = ColourFX.RED
                            }

                            gameVisible = false
                        }
                    }
                }
            }
        }

        launchCoroutine(UI) {
            sceneDataTable.items.clear()
            sceneDataTable.items.addAll(
                    "gameVisible" to danganScene.gameVisible,
                    "colour" to danganScene.colour,
                    "text" to (danganScene.text ?: "")
            )
        }
    }

    fun redraw() {
        val width = canvas.widthProperty().value
        val height = canvas.heightProperty().value

        graphics.clearRect(0.0, 0.0, width, height)

        danganScene.apply {
            if (gameVisible) {
                graphics.fill = ColourFX.BLACK
                graphics.fillRect(0.0, 0.0, width, height)

                if (text != null) {
                    val textBoxImage: Image = when (timeOfDay) {
                        TimeOfDay.DAY -> if (speakerIsProtag) DanganScene.TEXT_PROTAG_DAY_SPEAK else DanganScene.TEXT_OTHERS_DAY
                        TimeOfDay.NIGHT -> if (speakerIsProtag) DanganScene.TEXT_PROTAG_NIGHT_SPEAK else DanganScene.TEXT_OTHERS_NIGHT
                        TimeOfDay.UNK -> if (speakerIsProtag) DanganScene.TEXT_PROTAG_UNK_SPEAK else DanganScene.TEXT_OTHERS_UNK
                    }

                    graphics.drawImage(textBoxImage, 0.0, 0.0, width, height)

                    val split = LinTextParser.splitUp(text!!)

                    var xPosition = 40.0
                    var yPosition = 440.0

                    graphics.stroke = ColourFX.WHITE
                    graphics.fill = ColourFX.WHITE
                    graphics.font = Font.font(28.0)

                    val (fontMap, fontImage, missingChar) = danganScene.font
                    val missingGlyph = missingChar?.let(fontMap::glyphFor)
                    val lineHeight = fontMap.glyphs.maxBy { glyph -> glyph.height }?.height ?: 0

                    var colourCode = 0

                    split.forEach { param ->
                        when (param) {
                            is String -> {
                                if (param.isBlank())
                                    return@forEach

                                param.forEach charLoop@{ char ->
                                    if (char == '\n') { //New line
                                        xPosition = 40.0
                                        yPosition += lineHeight

                                        return@charLoop
                                    }

                                    val glyph = fontMap.glyphFor(char) ?: missingGlyph ?: return@charLoop

                                    graphics.drawImage(fontImage[colourCode] ?: fontImage.sourceImage, glyph.x.toDouble(), glyph.y.toDouble(), glyph.width.toDouble(), glyph.height.toDouble(), xPosition, yPosition, glyph.width.toDouble(), glyph.height.toDouble())

                                    xPosition += glyph.width
                                }
                            }
                            is Int -> colourCode = param
                        }
                    }
                }
            }

            //Draw foreground colour
            graphics.fill = colour
            graphics.fillRect(0.0, 0.0, width, height)
        }
    }

    fun resize(oldWidth: Double, newWidth: Double, oldHeight: Double, newHeight: Double) {
        val widthPercent = newWidth / oldWidth
        val heightPercent = newHeight / oldHeight

        canvas.width *= widthPercent
        canvas.height *= heightPercent

        gameDataScrollPane.prefViewportWidth = canvas.width
        gameDataScrollPane.prefViewportHeight = canvas.height / 2

        sceneDataTable.prefWidth = gameDataScrollPane.prefViewportWidth

        opCodeView.prefViewportWidth = canvas.width / 2
        opCodeView.prefViewportHeight = canvas.height / 2

        redraw()
    }
}