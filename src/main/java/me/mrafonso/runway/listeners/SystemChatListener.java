package me.mrafonso.runway.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import me.mrafonso.runway.config.Config;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;

public class SystemChatListener extends AbstractListener {
    public SystemChatListener(Plugin plugin, ProcessHandler processHandler, ConfigManager configManager) {
        super(plugin, processHandler, configManager, PacketType.Play.Server.SYSTEM_CHAT);
    }

    @Override
    public void onPacketSending(PacketEvent e) {
        if (!configManager.config().listeners().systemMessages()) return;

        e.getPacket().getSpecificModifier(Component.class).modify(0, component -> handler.processComponent(component, e.getPlayer()));
    }
}
