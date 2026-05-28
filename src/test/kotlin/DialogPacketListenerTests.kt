package me.mrafonso.runway

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.dialog.CommonDialogData
import com.github.retrooper.packetevents.protocol.dialog.Dialog
import com.github.retrooper.packetevents.protocol.dialog.DialogAction
import com.github.retrooper.packetevents.protocol.dialog.MultiActionDialog
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessage
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessageDialogBody
import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton
import com.github.retrooper.packetevents.protocol.dialog.button.CommonButtonData
import com.github.retrooper.packetevents.protocol.dialog.input.BooleanInputControl
import com.github.retrooper.packetevents.protocol.dialog.input.Input
import com.github.retrooper.packetevents.protocol.dialog.input.NumberRangeInputControl
import com.github.retrooper.packetevents.protocol.dialog.input.SingleOptionInputControl
import com.github.retrooper.packetevents.protocol.dialog.input.TextInputControl
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import me.mrafonso.runway.integration.HookHandler
import me.mrafonso.runway.listener.packet.DialogPacketListener
import me.mrafonso.runway.processing.ProcessHandler
import me.mrafonso.runway.resolver.ResolverHandler
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock

class DialogPacketListenerTests : StringSpec({
    lateinit var server: ServerMock
    lateinit var plugin: Runway
    lateinit var listener: DialogPacketListener

    val plainText = PlainTextComponentSerializer.plainText()

    beforeEach {
        server = MockBukkit.mock()

        System.setProperty("bstats.relocatecheck", "false")
        System.setProperty("runway.testmode", "true")

        plugin = MockBukkit.load(Runway::class.java)

        val hookHandler = HookHandler()
        hookHandler.init(plugin)
        val resolverHandler = ResolverHandler(plugin, hookHandler, plugin.configHandler)
        resolverHandler.reloadAll()
        listener = DialogPacketListener(ProcessHandler(plugin.configHandler, resolverHandler), plugin.configHandler)
    }

    afterEach {
        if (PacketEvents.getAPI() != null) {
            PacketEvents.getAPI().terminate()
        }
        server.pluginManager.disablePlugin(plugin)
        MockBukkit.unmock()
    }

    fun processDialog(dialog: Dialog): Dialog {
        val method = DialogPacketListener::class.java.getDeclaredMethod(
            "processDialog",
            Dialog::class.java,
            Player::class.java
        )
        method.isAccessible = true
        return method.invoke(listener, dialog, null) as Dialog
    }

    fun text(component: Component): String = plainText.serialize(component)

    fun dialog(
        title: Component = Component.text("Title"),
        externalTitle: Component? = null,
        body: List<PlainMessageDialogBody> = emptyList(),
        inputs: List<Input> = emptyList(),
        actions: List<ActionButton> = emptyList(),
        exitAction: ActionButton? = null
    ): MultiActionDialog {
        return MultiActionDialog(
            CommonDialogData(
                title,
                externalTitle,
                true,
                false,
                DialogAction.CLOSE,
                body,
                inputs
            ),
            actions,
            exitAction,
            1
        )
    }

    "dialog listener processes common title and body text" {
        val processed = processDialog(
            dialog(
                title = Component.text("$<green>Dialog Title"),
                externalTitle = Component.text("$<yellow>External Title"),
                body = listOf(
                    PlainMessageDialogBody(PlainMessage(Component.text("$<uppercase>body text</uppercase>"), 180))
                )
            )
        ) as MultiActionDialog

        val body = processed.common.body.single() as PlainMessageDialogBody

        text(processed.common.title) shouldBe "Dialog Title"
        text(processed.common.externalTitle!!) shouldBe "External Title"
        text(body.message.contents) shouldBe "BODY TEXT"
    }

    "dialog listener processes action and exit button text" {
        val processed = processDialog(
            dialog(
                actions = listOf(
                    ActionButton(
                        CommonButtonData(
                            Component.text("$<green>Launch"),
                            Component.text("$<yellow>Launch Tooltip"),
                            120
                        ),
                        null
                    )
                ),
                exitAction = ActionButton(CommonButtonData(Component.text("$<red>Exit"), null, 80), null)
            )
        ) as MultiActionDialog

        text(processed.actions.single().button.label) shouldBe "Launch"
        text(processed.actions.single().button.tooltip!!) shouldBe "Launch Tooltip"
        text(processed.exitAction!!.button.label) shouldBe "Exit"
    }

    "dialog listener processes text input labels and initial values" {
        val processed = processDialog(
            dialog(
                inputs = listOf(
                    Input(
                        "name",
                        TextInputControl(
                            200,
                            Component.text("$<blue>Name Label"),
                            true,
                            "$<uppercase>pilot</uppercase>",
                            32,
                            null
                        )
                    )
                )
            )
        ) as MultiActionDialog

        val textInput = processed.common.inputs.single().control as TextInputControl

        text(textInput.label) shouldBe "Name Label"
        textInput.initial shouldBe "PILOT"
    }

    "dialog listener processes boolean and number range input labels" {
        val processed = processDialog(
            dialog(
                inputs = listOf(
                    Input(
                        "confirm",
                        BooleanInputControl(Component.text("$<red>Confirm Label"), true, "yes", "no")
                    ),
                    Input(
                        "amount",
                        NumberRangeInputControl(
                            200,
                            Component.text("$<yellow>Amount Label"),
                            "options.generic_value",
                            NumberRangeInputControl.RangeInfo(1f, 10f, 3f, 1f)
                        )
                    )
                )
            )
        ) as MultiActionDialog

        val booleanInput = processed.common.inputs[0].control as BooleanInputControl
        val rangeInput = processed.common.inputs[1].control as NumberRangeInputControl

        text(booleanInput.label) shouldBe "Confirm Label"
        text(rangeInput.label) shouldBe "Amount Label"
    }

    "dialog listener processes single option input labels and displays" {
        val processed = processDialog(
            dialog(
                inputs = listOf(
                    Input(
                        "choice",
                        SingleOptionInputControl(
                            200,
                            listOf(
                                SingleOptionInputControl.Entry(
                                    "alpha",
                                    Component.text("$<smallcaps>Alpha</smallcaps>"),
                                    true
                                )
                            ),
                            Component.text("$<green>Choice Label"),
                            true
                        )
                    )
                )
            )
        ) as MultiActionDialog

        val optionInput = processed.common.inputs.single().control as SingleOptionInputControl

        text(optionInput.label) shouldBe "Choice Label"
        text(optionInput.options.single().display!!) shouldBe "\u1D00\u029F\u1D18\u029C\u1D00"
    }
})
