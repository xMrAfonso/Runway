package me.mrafonso.runway.listener.packet

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent
import com.github.retrooper.packetevents.protocol.dialog.CommonDialogData
import com.github.retrooper.packetevents.protocol.dialog.ConfirmationDialog
import com.github.retrooper.packetevents.protocol.dialog.Dialog
import com.github.retrooper.packetevents.protocol.dialog.DialogListDialog
import com.github.retrooper.packetevents.protocol.dialog.MultiActionDialog
import com.github.retrooper.packetevents.protocol.dialog.NoticeDialog
import com.github.retrooper.packetevents.protocol.dialog.ServerLinksDialog
import com.github.retrooper.packetevents.protocol.dialog.body.DialogBody
import com.github.retrooper.packetevents.protocol.dialog.body.ItemDialogBody
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessage
import com.github.retrooper.packetevents.protocol.dialog.body.PlainMessageDialogBody
import com.github.retrooper.packetevents.protocol.dialog.button.ActionButton
import com.github.retrooper.packetevents.protocol.dialog.button.CommonButtonData
import com.github.retrooper.packetevents.protocol.dialog.input.BooleanInputControl
import com.github.retrooper.packetevents.protocol.dialog.input.Input
import com.github.retrooper.packetevents.protocol.dialog.input.InputControl
import com.github.retrooper.packetevents.protocol.dialog.input.NumberRangeInputControl
import com.github.retrooper.packetevents.protocol.dialog.input.SingleOptionInputControl
import com.github.retrooper.packetevents.protocol.dialog.input.TextInputControl
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerShowDialog
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.processing.ProcessHandler
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player

class DialogPacketListener(processHandler: ProcessHandler, configHandler: ConfigHandler) :
    AbstractPacketListener(processHandler, configHandler) {

    private val plainText = PlainTextComponentSerializer.plainText()

    override fun onPacketPlaySend(e: PacketPlaySendEvent) {
        super.onPacketPlaySend(e)
        if (e.packetType != PacketType.Play.Server.SHOW_DIALOG) return

        val settings = configHandler.get<Settings>()
        if (!settings.listeners.dialogs) return

        val player = e.getPlayer<Player>()
        val packet = WrapperPlayServerShowDialog(e)
        packet.dialog = processDialog(packet.dialog, player)
    }

    private fun processDialog(dialog: Dialog, player: Player?): Dialog {
        return when (dialog) {
            is NoticeDialog -> NoticeDialog(
                processCommon(dialog.common, player),
                processButton(dialog.action, player)
            )
            is ConfirmationDialog -> ConfirmationDialog(
                processCommon(dialog.common, player),
                processButton(dialog.yesButton, player),
                processButton(dialog.noButton, player)
            )
            is MultiActionDialog -> MultiActionDialog(
                processCommon(dialog.common, player),
                dialog.actions.map { processButton(it, player) },
                dialog.exitAction?.let { processButton(it, player) },
                dialog.columns
            )
            is ServerLinksDialog -> ServerLinksDialog(
                processCommon(dialog.common, player),
                dialog.exitAction?.let { processButton(it, player) },
                dialog.columns,
                dialog.buttonWidth
            )
            is DialogListDialog -> DialogListDialog(
                processCommon(dialog.common, player),
                dialog.dialogs,
                dialog.exitAction?.let { processButton(it, player) },
                dialog.columns,
                dialog.buttonWidth
            )
            else -> dialog
        }
    }

    private fun processCommon(common: CommonDialogData, player: Player?): CommonDialogData {
        return CommonDialogData(
            processComponent(common.title, player),
            common.externalTitle?.let { processComponent(it, player) },
            common.isCanCloseWithEscape,
            common.isPause,
            common.afterAction,
            common.body.map { processBody(it, player) },
            common.inputs.map { processInput(it, player) }
        )
    }

    private fun processBody(body: DialogBody, player: Player?): DialogBody {
        return when (body) {
            is PlainMessageDialogBody -> PlainMessageDialogBody(processPlainMessage(body.message, player))
            is ItemDialogBody -> ItemDialogBody(
                handler.processItem(body.item, player),
                body.description?.let { processPlainMessage(it, player) },
                body.isShowDecorations,
                body.isShowTooltip,
                body.width,
                body.height
            )
            else -> body
        }
    }

    private fun processInput(input: Input, player: Player?): Input {
        return Input(input.key, processInputControl(input.control, player))
    }

    private fun processInputControl(control: InputControl, player: Player?): InputControl {
        return when (control) {
            is TextInputControl -> TextInputControl(
                control.width,
                processComponent(control.label, player),
                control.isLabelVisible,
                processPlainText(control.initial, player),
                control.maxLength,
                control.multiline
            )
            is BooleanInputControl -> BooleanInputControl(
                processComponent(control.label, player),
                control.isInitial,
                control.onTrue,
                control.onFalse
            )
            is SingleOptionInputControl -> SingleOptionInputControl(
                control.width,
                control.options.map { option ->
                    SingleOptionInputControl.Entry(
                        option.id,
                        option.display?.let { processComponent(it, player) },
                        option.isInitial
                    )
                },
                processComponent(control.label, player),
                control.isLabelVisible
            )
            is NumberRangeInputControl -> NumberRangeInputControl(
                control.width,
                processComponent(control.label, player),
                control.labelFormat,
                control.rangeInfo
            )
            else -> control
        }
    }

    private fun processButton(button: ActionButton, player: Player?): ActionButton {
        return ActionButton(processButtonData(button.button, player), button.action)
    }

    private fun processButtonData(button: CommonButtonData, player: Player?): CommonButtonData {
        return CommonButtonData(
            processComponent(button.label, player),
            button.tooltip?.let { processComponent(it, player) },
            button.width
        )
    }

    private fun processPlainMessage(message: PlainMessage, player: Player?): PlainMessage {
        return PlainMessage(processComponent(message.contents, player), message.width)
    }

    private fun processComponent(component: Component, player: Player?): Component {
        return handler.processComponent(component, player) ?: component
    }

    private fun processPlainText(text: String, player: Player?): String {
        return handler.processComponent(text, player)?.let { plainText.serialize(it) } ?: text
    }
}
