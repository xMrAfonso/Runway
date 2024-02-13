package me.mrafonso.runway.config.section;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class PlaceholderHook {
    private boolean placeholderapi = false;
    private boolean miniplaceholders = false;

    public boolean placeholderAPI() {
        return placeholderapi;
    }

    public void placeholderAPI(boolean placeholderAPI) {
        this.placeholderapi = placeholderAPI;
    }

    public boolean miniPlaceholders() {
        return miniplaceholders;
    }

    public void miniPlaceholders(boolean miniPlaceholders) {
        this.miniplaceholders = miniPlaceholders;
    }
}
