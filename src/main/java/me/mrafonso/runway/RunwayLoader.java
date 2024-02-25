package me.mrafonso.runway;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

public class RunwayLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addRepository(new RemoteRepository.Builder("triumph-repo", "default", "https://repo.triumphteam.dev/snapshots/").build());
        resolver.addRepository(new RemoteRepository.Builder("jitpack-repo", "default", "https://jitpack.io").build());
        resolver.addRepository(new RemoteRepository.Builder("codemc-repo", "default", "https://repo.codemc.io/repository/maven-releases/").build());

        resolver.addDependency(new Dependency(new DefaultArtifact("com.github.simplix-softworks:simplixstorage:3.2.7"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("dev.triumphteam:triumph-cmd-bukkit:2.0.0-ALPHA-9"), null));
        //resolver.addDependency(new Dependency(new DefaultArtifact("com.github.retrooper.packetevents:spigot:2.2.1"), null));

        classpathBuilder.addLibrary(resolver);
    }
}