package me.mrafonso.runway.config.section;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class Listeners {
    @Comment("Whether to parse system messages, also known as plugin messages. (default: true)")
    private boolean systemMessages = true;
    @Comment("Whether to parse the MOTD. (default: true)")
    private boolean motd = true;
    @Comment("Whether to parse the tablist. (default: true)")
    private boolean tablist = true;
    @Comment("Whether to parse titles and subtitles. (default: true)")
    private boolean titles = true;
    @Comment("Whether to parse scoreboards, which work using Teams. (default: true)")
    private boolean scoreboards = true;
    private Inventory inventory = new Inventory();
    @Comment("Whether to parse items, including the item name and lore. (default: true)")
    private boolean items = true;

    public boolean systemMessages() {
        return systemMessages;
    }

    public boolean motd() {
        return motd;
    }

    public boolean tablist() {
        return tablist;
    }

    public boolean titles() {
        return titles;
    }

    public boolean scoreboards() {
        return scoreboards;
    }

    public Inventory inventory() {
        return inventory;
    }

    public boolean items() {
        return items;
    }

    public void systemMessages(boolean systemMessages) {
        this.systemMessages = systemMessages;
    }

    public void motd(boolean motd) {
        this.motd = motd;
    }

    public void tablist(boolean tablist) {
        this.tablist = tablist;
    }

    public void titles(boolean titles) {
        this.titles = titles;
    }

    public void scoreboards(boolean scoreboards) {
        this.scoreboards = scoreboards;
    }

    public void inventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void items(boolean items) {
        this.items = items;
    }
}