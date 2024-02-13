package me.mrafonso.runway.config.section;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class Listeners {
    private boolean systemMessages = true;
    private boolean motd = true;
    private boolean tablist = true;
    private boolean titles = true;
    private boolean scoreboards = true;
    private Inventory inventory = new Inventory();
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