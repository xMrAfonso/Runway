package me.mrafonso.runway.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import me.mrafonso.runway.config.Config;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class ItemListener extends AbstractListener {

    public ItemListener(Plugin plugin, ProcessHandler processHandler, ConfigManager configManager) {
        super(plugin, processHandler, configManager, PacketType.Play.Server.SET_SLOT);
    }

    @Override
    public void onPacketSending(PacketEvent e) {
        if (!configManager.config().listeners().items()) return;

        Player player = e.getPlayer();

        e.getPacket().getItemModifier().modify(0, itemStack -> {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                if (itemMeta.hasDisplayName()) itemMeta.displayName(handler.processComponent(itemMeta.displayName(), player));
                if (itemMeta.hasLore()) itemMeta.lore(handler.processComponent(itemMeta.lore(), player));
                itemStack.setItemMeta(itemMeta);
            }
            return itemStack;
        });
    }
}
