package me.mrafonso.runway.util;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import de.leonhard.storage.Config;
import io.github.miniplaceholders.api.MiniPlaceholders;
import me.clip.placeholderapi.PlaceholderAPI;
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
    private final TextComponent noItalics = Component.empty().decoration(TextDecoration.ITALIC, false);
    private final Config config;
    private final Config placeholders;
    private TagResolver placeholdersResolver;

    public ProcessHandler(ConfigManager configManager) {
        this.config = configManager.config();
        this.placeholders = configManager.placeholders();
    }

    public void reloadPlaceholders() {
        TagResolver.Builder builder = TagResolver.builder();

        for (String key : placeholders.singleLayerKeySet("custom-placeholders")) {
            @Subst("") String placeholder =  key.toLowerCase()
                                                .replace(" ", "_")
                                                .replace("-", "_");

            String value = placeholders.getString("custom-placeholders." + key);

            builder.resolver(Placeholder.parsed(placeholder, value));
        }
        placeholdersResolver = builder.build();
    }

    public Component processComponent(@Nullable Component component, @Nullable Player player) {
        if (component == null) return Component.empty();

        boolean requirePrefixMM = config.getOrDefault("require-prefix.minimessage", true);
        boolean placeholderapi = config.getOrDefault("placeholder-hook.placeholderapi", false);
        boolean miniPlaceholders = config.getOrDefault("placeholder-hook.miniplaceholders", false);
        boolean requirePrefixP = config.getOrDefault("require-prefix.placeholders", true);
        boolean ignoreLegacy = config.getOrDefault("ignore-legacy", false);

        String s = miniMessage.serialize(component);
        if (s.contains("§")) {
            if (ignoreLegacy) {
                s = s.replace("§r", "&");
            } else {
                config.set("ignore-legacy", true);
                Bukkit.getLogger().log(Level.WARNING, "Detected Legacy colors! Runway is now ignoring legacy colors. \n" +
                                                      "To avoid receiving this message again, disable legacy colors in the config.");
            }
        }

        if (requirePrefixMM && !s.contains("[mm]")) return component;

        s = s.replace("[mm]", "");
        s = s.replace("\\<", "<");

        if (player != null && (miniPlaceholders || placeholderapi)) {
            if (requirePrefixP && !s.contains("[p]")) {
                return miniMessage.deserialize(s, placeholdersResolver);
            }

            s = s.replace("[p]", "");
            if (placeholderapi) s = PlaceholderAPI.setPlaceholders(player, s);
            TagResolver resolver = placeholdersResolver;
            if (miniPlaceholders) {
                TagResolver playerResolver = MiniPlaceholders.getAudienceGlobalPlaceholders(player);
                resolver = TagResolver.builder().resolvers(playerResolver, placeholdersResolver).build();
            }
            return miniMessage.deserialize(s, resolver);
        }
        return miniMessage.deserialize(s, placeholdersResolver);
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
