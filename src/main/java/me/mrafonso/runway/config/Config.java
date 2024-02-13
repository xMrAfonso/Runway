package me.mrafonso.runway.config;

import me.mrafonso.runway.config.section.Listeners;
import me.mrafonso.runway.config.section.Messages;
import me.mrafonso.runway.config.section.PlaceholderHook;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Collections;
import java.util.Map;

@ConfigSerializable
public class Config {

    private boolean requirePrefix = true;
    private PlaceholderHook placeholderHook = new PlaceholderHook();
    private boolean disableItalics = true;
    private boolean ignoreLegacy = false;
    private Listeners listeners = new Listeners();
    private Map<String, String> customPlaceholders = Collections.singletonMap("server_name", "RunwayMC");
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
