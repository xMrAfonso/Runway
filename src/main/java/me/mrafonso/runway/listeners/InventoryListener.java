package me.mrafonso.runway.listeners;

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;
import org.bukkit.entity.Player;

public class InventoryListener extends AbstractListener {

    public InventoryListener(ProcessHandler processHandler, ConfigManager configManager) {
        super(processHandler, configManager);
    }

    @Override
    public void onPacketPlaySend(PacketPlaySendEvent e) {
        PacketTypeCommon type = e.getPacketType();
        if (type != PacketType.Play.Server.OPEN_WINDOW &&
            type != PacketType.Play.Server.WINDOW_ITEMS) return;

        Player player = (Player) e.getPlayer();
        boolean titles = config.getOrDefault("listeners.inventory.titles", true);
        boolean items = config.getOrDefault("listeners.inventory.items", true);

        if (titles && type == PacketType.Play.Server.OPEN_WINDOW) {
            WrapperPlayServerOpenWindow packet = new WrapperPlayServerOpenWindow(e);
            packet.setTitle(handler.processComponent(packet.getTitle(), player));
        } else if (items && type == PacketType.Play.Server.WINDOW_ITEMS) {
            WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(e);
            packet.setItems(handler.processItems(packet.getItems(), player));
        }
    }
}
