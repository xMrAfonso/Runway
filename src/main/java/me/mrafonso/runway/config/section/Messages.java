package me.mrafonso.runway.config.section;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class Messages {
    private String reloadSuccess = "<gradient:#0050ff:#0099ff>Runway » </gradient><white>Configuration was reloaded successfully!";
    private String parseSuccess = "<gradient:#0050ff:#0099ff>Runway » </gradient><white>Parsed text: <gray><text>";
    private String parseFail = "<gradient:#0050ff:#0099ff>Runway » </gradient><white>Failed to parse the text. Make sure it can be parsed!";
    private String invalidArgument = "<gradient:#0050ff:#0099ff>Runway » </gradient><white>Invalid argument. Please use a valid argument!";
    private String unknownCommand = "<gradient:#0050ff:#0099ff>Runway » </gradient><white>Sorry but that command is unknown. Please try again!";
    private String noPermission = "<gradient:#0050ff:#0099ff>Runway » </gradient><white>You do not have permission to use this command!";
    private String notEnoughArguments = "<gradient:#0050ff:#0099ff>Runway » </gradient><white>Not enough arguments. Please use a valid argument!";

    public String reloadSuccess() {
        return reloadSuccess;
    }

    public void reloadSuccess(String reloadSuccess) {
        this.reloadSuccess = reloadSuccess;
    }


    public String parseSuccess() {
        return parseSuccess;
    }

    public void parseSuccess(String parseTextSuccess) {
        this.parseSuccess = parseTextSuccess;
    }

    public String parseFail() {
        return parseFail;
    }

    public void parseFail(String parseTextFail) {
        this.parseFail = parseTextFail;
    }

    public String invalidArgument() {
        return invalidArgument;
    }

    public void invalidArgument(String invalidArgument) {
        this.invalidArgument = invalidArgument;
    }

    public String noPermission() {
        return noPermission;
    }

    public void noPermission(String noPermission) {
        this.noPermission = noPermission;
    }

    public String notEnoughArguments() {
        return notEnoughArguments;
    }

    public void notEnoughArguments(String notEnoughArguments) {
        this.notEnoughArguments = notEnoughArguments;
    }

    public String unknownCommand() {
        return unknownCommand;
    }

    public void unknownCommand(String unknownCommand) {
        this.unknownCommand = unknownCommand;
    }
}
