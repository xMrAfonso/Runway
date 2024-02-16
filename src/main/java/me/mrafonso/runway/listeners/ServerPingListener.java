package me.mrafonso.runway.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ServerPingListener extends AbstractListener {
    public ServerPingListener(Plugin plugin, ProcessHandler processHandler, ConfigManager configManager) {
        super(plugin, processHandler, configManager, PacketType.Status.Server.SERVER_INFO);
    }

    @Override
    public void onPacketSending(PacketEvent e) {
        if (!config.getOrDefault("listeners.motd", true)) return;

        PacketContainer packet = e.getPacket();
        Player player = e.getPlayer();

        WrappedServerPing ping = packet.getServerPings().read(0);
        ping.setMotD(handler.processComponent(ping.getMotD(), player));
        packet.getServerPings().write(0, ping);
        e.setPacket(packet);
    }
}