package me.mrafonso.runway.config.section;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class Messages {
    private String reloadSuccess = "<gradient:#0000ff:#0077ff>Runway » </gradient><white>Configuration was reloaded successfully!";

    public String reloadSuccess() {
        return reloadSuccess;
    }

    public void reloadSuccess(String reloadSuccess) {
        this.reloadSuccess = reloadSuccess;
    }
}
