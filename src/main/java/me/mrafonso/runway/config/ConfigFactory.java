package me.mrafonso.runway.config;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class ConfigFactory {

    private final Path dataFolder;

    public ConfigFactory(Path dataFolder) {
        this.dataFolder = dataFolder;
    }

    public Config config() {
        Config config = create(Config.class, "config.yml");
        return Objects.requireNonNullElseGet(config, Config::new);
    }

    private @Nullable <T> T create(Class<T> clazz, String fileName) {
        try {
            if (!Files.exists(dataFolder)) {
                Files.createDirectories(dataFolder);
            }

            Path path = dataFolder.resolve(fileName);

            YamlConfigurationLoader loader = loader(path);
            CommentedConfigurationNode node = loader.load();
            T config = node.get(clazz);

            if (!Files.exists(path)) {
                Files.createFile(path);
                node.set(clazz, config);
            }

            loader.save(node);
            return config;
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private YamlConfigurationLoader loader(Path path) {
        return YamlConfigurationLoader.builder()
                .path(path)
                .nodeStyle(NodeStyle.BLOCK)
                .indent(2)
                .build();
    }
}
