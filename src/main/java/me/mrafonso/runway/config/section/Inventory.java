package me.mrafonso.runway.config.section;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class Inventory {
    private boolean title = true;
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