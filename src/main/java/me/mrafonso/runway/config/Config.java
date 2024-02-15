package me.mrafonso.runway.config;

import me.mrafonso.runway.config.section.Listeners;
import me.mrafonso.runway.config.section.Messages;
import me.mrafonso.runway.config.section.PlaceholderHook;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.Collections;
import java.util.Map;

@ConfigSerializable
public class Config {

    @Comment("Is the prefix [mm] required for packets to be parsed by Runway? (default: true)")
    private boolean requirePrefix = true;
    @Comment("Whether PlaceholderAPI or MiniPlaceholders should be hooked into Runway. Both can be used at the same time.")
    private PlaceholderHook placeholderHook = new PlaceholderHook();
    @Comment("Whether to disable italics in names and lores of items. (default: true)")
    private boolean disableItalics = true;
    @Comment("Whether to ignore legacy colors in packets by replacing the legacy color code with &. \n " +
             "If this is set to false, Runway will log a warning and set this to true.")
    private boolean ignoreLegacy = false;
    @Comment("Listeners for Runway to hook into.")
    private Listeners listeners = new Listeners();
    @Comment("Custom placeholders to be used in Runway. These are static and will not change.")
    private Map<String, String> customPlaceholders = Collections.singletonMap("server_name", "RunwayMC");
    @Comment("Messages to be used in Runway.")
    private Messages messages = new Messages();

    public boolean requirePrefix() {
        return requirePrefix;
    }

    public PlaceholderHook placeholderHook() {
        return placeholderHook;
    }

    public boolean disableItalics() {
        return disableItalics;
    }

    public Listeners listeners() {
        return listeners;
    }

    public Map<String, String> customPlaceholders() {
        return customPlaceholders;
    }

    public Messages messages() {
        return messages;
    }

    public boolean ignoreLegacy() {
        return ignoreLegacy;
    }

    public void requirePrefix(boolean requirePrefix) {
        this.requirePrefix = requirePrefix;
    }

    public void placeholderHook(PlaceholderHook placeholderHook) {
        this.placeholderHook = placeholderHook;
    }

    public void disableItalics(boolean disableItalics) {
        this.disableItalics = disableItalics;
    }

    public void listeners(Listeners listeners) {
        this.listeners = listeners;
    }

    public void customPlaceholders(Map<String, String> customPlaceholders) {
        this.customPlaceholders = customPlaceholders;
    }

    public void messages(Messages messages) {
        this.messages = messages;
    }

    public void ignoreLegacy(boolean ignoreLegacy) {
        this.ignoreLegacy = ignoreLegacy;
    }
}
