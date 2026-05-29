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

    override fun handlePacket(e: PacketPlaySendEvent) {
        if (e.packetType != PacketType.Play.Server.SHOW_DIALOG) return

        val settings = configHandler.get<Settings>()
        if (!settings.listeners.dialogs.enable) return

        val player = e.getPlayer<Player>()
        val packet = WrapperPlayServerShowDialog(e)
        packet.dialog = processDialog(packet.dialog, player, settings.requiresPrefix(settings.listeners.dialogs))
    }

    private fun processDialog(dialog: Dialog, player: Player?): Dialog {
        val settings = configHandler.get<Settings>()
        return processDialog(dialog, player, settings.requiresPrefix(settings.listeners.dialogs))
    }

    private fun processDialog(dialog: Dialog, player: Player?, requirePrefix: Boolean): Dialog {
        return when (dialog) {
            is NoticeDialog -> NoticeDialog(
                processCommon(dialog.common, player, requirePrefix),
                processButton(dialog.action, player, requirePrefix)
            )
            is ConfirmationDialog -> ConfirmationDialog(
                processCommon(dialog.common, player, requirePrefix),
                processButton(dialog.yesButton, player, requirePrefix),
                processButton(dialog.noButton, player, requirePrefix)
            )
            is MultiActionDialog -> MultiActionDialog(
                processCommon(dialog.common, player, requirePrefix),
                dialog.actions.map { processButton(it, player, requirePrefix) },
                dialog.exitAction?.let { processButton(it, player, requirePrefix) },
                dialog.columns
            )
            is ServerLinksDialog -> ServerLinksDialog(
                processCommon(dialog.common, player, requirePrefix),
                dialog.exitAction?.let { processButton(it, player, requirePrefix) },
                dialog.columns,
                dialog.buttonWidth
            )
            is DialogListDialog -> DialogListDialog(
                processCommon(dialog.common, player, requirePrefix),
                dialog.dialogs,
                dialog.exitAction?.let { processButton(it, player, requirePrefix) },
                dialog.columns,
                dialog.buttonWidth
            )
            else -> dialog
        }
    }

    private fun processCommon(common: CommonDialogData, player: Player?, requirePrefix: Boolean): CommonDialogData {
        return CommonDialogData(
            processComponent(common.title, player, requirePrefix),
            common.externalTitle?.let { processComponent(it, player, requirePrefix) },
            common.isCanCloseWithEscape,
            common.isPause,
            common.afterAction,
            common.body.map { processBody(it, player, requirePrefix) },
            common.inputs.map { processInput(it, player, requirePrefix) }
        )
    }

    private fun processBody(body: DialogBody, player: Player?, requirePrefix: Boolean): DialogBody {
        return when (body) {
            is PlainMessageDialogBody -> PlainMessageDialogBody(processPlainMessage(body.message, player, requirePrefix))
            is ItemDialogBody -> ItemDialogBody(
                handler.processItem(body.item, player, requirePrefix),
                body.description?.let { processPlainMessage(it, player, requirePrefix) },
                body.isShowDecorations,
                body.isShowTooltip,
                body.width,
                body.height
            )
            else -> body
        }
    }

    private fun processInput(input: Input, player: Player?, requirePrefix: Boolean): Input {
        return Input(input.key, processInputControl(input.control, player, requirePrefix))
    }

    private fun processInputControl(control: InputControl, player: Player?, requirePrefix: Boolean): InputControl {
        return when (control) {
            is TextInputControl -> TextInputControl(
                control.width,
                processComponent(control.label, player, requirePrefix),
                control.isLabelVisible,
                processPlainText(control.initial, player, requirePrefix),
                control.maxLength,
                control.multiline
            )
            is BooleanInputControl -> BooleanInputControl(
                processComponent(control.label, player, requirePrefix),
                control.isInitial,
                control.onTrue,
                control.onFalse
            )
            is SingleOptionInputControl -> SingleOptionInputControl(
                control.width,
                control.options.map { option ->
                    SingleOptionInputControl.Entry(
                        option.id,
                        option.display?.let { processComponent(it, player, requirePrefix) },
                        option.isInitial
                    )
                },
                processComponent(control.label, player, requirePrefix),
                control.isLabelVisible
            )
            is NumberRangeInputControl -> NumberRangeInputControl(
                control.width,
                processComponent(control.label, player, requirePrefix),
                control.labelFormat,
                control.rangeInfo
            )
            else -> control
        }
    }

    private fun processButton(button: ActionButton, player: Player?, requirePrefix: Boolean): ActionButton {
        return ActionButton(processButtonData(button.button, player, requirePrefix), button.action)
    }

    private fun processButtonData(button: CommonButtonData, player: Player?, requirePrefix: Boolean): CommonButtonData {
        return CommonButtonData(
            processComponent(button.label, player, requirePrefix),
            button.tooltip?.let { processComponent(it, player, requirePrefix) },
            button.width
        )
    }

    private fun processPlainMessage(message: PlainMessage, player: Player?, requirePrefix: Boolean): PlainMessage {
        return PlainMessage(processComponent(message.contents, player, requirePrefix), message.width)
    }

    private fun processComponent(component: Component, player: Player?, requirePrefix: Boolean): Component {
        return handler.processComponent(component, player, requirePrefix) ?: component
    }

    private fun processPlainText(text: String, player: Player?, requirePrefix: Boolean): String {
        return handler.processComponent(text, player, requirePrefix)?.let { plainText.serialize(it) } ?: text
    }
}
