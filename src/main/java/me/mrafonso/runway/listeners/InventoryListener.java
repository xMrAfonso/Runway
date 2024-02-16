package me.mrafonso.runway.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.mrafonso.runway.config.ConfigManager;
import me.mrafonso.runway.util.ProcessHandler;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class InventoryListener extends AbstractListener {

    public InventoryListener(Plugin plugin, ProcessHandler processHandler, ConfigManager configManager) {
        super(plugin, processHandler, configManager, PacketType.Play.Server.OPEN_WINDOW,
                                                     PacketType.Play.Server.WINDOW_ITEMS);
    }

    @Override
    public void onPacketSending(PacketEvent e) {
        PacketContainer packet = e.getPacket();
        Player player = e.getPlayer();

        boolean titles = config.getOrDefault("listeners.inventory.titles", true);
        boolean items = config.getOrDefault("listeners.inventory.items", true);

        if (titles && packet.getType() == PacketType.Play.Server.OPEN_WINDOW) {
            packet.getChatComponents().modify(0, component -> handler.processComponent(component, player));

        } else if (items && packet.getType() == PacketType.Play.Server.WINDOW_ITEMS) {
            packet.getItemListModifier().modify(0, itemStacks -> {
                for (ItemStack itemStack : itemStacks) {
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    if (itemMeta != null) {
                        if (itemMeta.hasDisplayName()) itemMeta.displayName(handler.processComponent(itemMeta.displayName(), player));
                        if (itemMeta.hasLore()) itemMeta.lore(handler.processComponent(itemMeta.lore(), player));
                        itemStack.setItemMeta(itemMeta);
                    }
                }
                return itemStacks;
            });

        }
    }
}
