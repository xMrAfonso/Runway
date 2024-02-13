package me.mrafonso.runway.util;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import io.github.miniplaceholders.api.MiniPlaceholders;
import me.clip.placeholderapi.PlaceholderAPI;
import me.mrafonso.runway.config.Config;
import me.mrafonso.runway.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ProcessHandler {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final GsonComponentSerializer gsonSerializer = GsonComponentSerializer.gson();
    private final ConfigManager configManager;
    private TagResolver customPlaceholders;
    private final TextComponent noItalics = Component.empty().decoration(TextDecoration.ITALIC, false);

    public ProcessHandler(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void reloadPlaceholders() {
        TagResolver.Builder builder = TagResolver.builder();
        for (String key : configManager.config().customPlaceholders().keySet()) {
            @Subst("") String placeholder = key.replace(" ", "_").toLowerCase();
            String value = configManager.config().customPlaceholders().get(key);

            builder.resolver(Placeholder.parsed(placeholder, value));
        }
        customPlaceholders = builder.build();
    }

    public Component processComponent(@Nullable Component component, @Nullable Player player) {
        if (component == null) return Component.empty();
        Config config = configManager.config();

        String s = miniMessage.serialize(component);
        if (s.contains("§")) {
            if (config.ignoreLegacy()) {
                s = s.replace("§r", "&");
            } else {
                config.ignoreLegacy(true);
                Bukkit.getLogger().log(Level.WARNING, "Detected Legacy colors! Runway is now ignoring legacy colors. \n" +
                                                      "To avoid receiving this message again, disable legacy colors in the config.");
            }
        }

        if (config.requirePrefix() && !s.contains("[mm]")) return component;

        s = s.replace("[mm]", "");
        s = s.replace("\\<", "<");

        if (config.placeholderHook().placeholderAPI() && player != null) s = PlaceholderAPI.setPlaceholders(player, s);

        Component c;

        if (player != null && config.placeholderHook().miniPlaceholders()) {
            TagResolver playerResolver = MiniPlaceholders.getAudienceGlobalPlaceholders(player);
            TagResolver resolver = TagResolver.builder().resolvers(playerResolver, customPlaceholders).build();
            c = miniMessage.deserialize(s, resolver);
        } else {
            c = miniMessage.deserialize(s, customPlaceholders);
        }

        if (config.disableItalics()) c = noItalics.append(c);
        return c;
    }

    public List<Component> processComponent(@Nullable List<Component> components, @Nullable Player player) {
        if (components == null) return new ArrayList<>();

        List<Component> componentList = new ArrayList<>();
        for (Component c : components) {
            componentList.add(processComponent(c, player));
        }
        return componentList;
    }

    public WrappedChatComponent processComponent(@Nullable WrappedChatComponent component, @Nullable Player player) {
        if (component == null) return null;
        return WrappedChatComponent.fromJson(gsonSerializer.serialize(processComponent(gsonSerializer.deserialize(component.getJson()), player)));
    }
}
