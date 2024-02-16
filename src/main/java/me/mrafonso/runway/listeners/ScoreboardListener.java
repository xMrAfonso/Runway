package me.mrafonso.runway.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ScoreboardListener extends AbstractListener {
    public ScoreboardListener(Plugin plugin, ProcessHandler processHandler, ConfigManager configManager) {
        super(plugin, processHandler, configManager, PacketType.Play.Server.SCOREBOARD_OBJECTIVE,
                                                     PacketType.Play.Server.SCOREBOARD_TEAM);
    }

    @Override
    public void onPacketSending(PacketEvent e) {
        if (!config.getOrDefault("listeners.scoreboards", false)) return;
        PacketContainer packet = e.getPacket();
        Player player = e.getPlayer();

        if (packet.getType() == PacketType.Play.Server.SCOREBOARD_OBJECTIVE) {
            e.getPacket().getChatComponents().modify(0, component -> handler.processComponent(component, player));
        } else if (packet.getType() == PacketType.Play.Server.SCOREBOARD_TEAM) {
            for (int i = 0; i < e.getPacket().getOptionalStructures().size(); i++) {
                e.getPacket().getOptionalStructures().modify(i, internalStructure -> {
                    if (internalStructure.isPresent()) {
                        for (int j = 0; j < internalStructure.get().getChatComponents().size(); j++) {
                            internalStructure.get().getChatComponents().write(j, handler.processComponent(internalStructure.get().getChatComponents().read(j), player));
                        }
                    }
                    return internalStructure;
                });
            }
        }
    }
}