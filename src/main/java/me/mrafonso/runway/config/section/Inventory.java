package me.mrafonso.runway.config.section;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class Inventory {
    @Comment("Whether to parse inventory titles. (default: true)")
    private boolean title = true;
    @Comment("Whether to parse inventory items, including the item name and lore. (default: true)")
    private boolean items = true;

    public boolean title() {
        return title;
    }

    public boolean items() {
        return items;
    }

    public void title(boolean title) {
        this.title = title;
    }

    public void items(boolean items) {
        this.items = items;
    }
}