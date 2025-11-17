package me.mrafonso.runway.config

import kotlinx.serialization.Serializable

@Serializable
data class Lang(
    val reloadSuccess: String = "<gradient:#0050ff:#0099ff>Runway</gradient><gray> | <white>Configuration was reloaded successfully!",
    val parseSuccess: String = "<gradient:#0050ff:#0099ff>Runway</gradient><gray> | <white>Parsed text: <gray><text>",
    val parseFail: String = "<gradient:#0050ff:#0099ff>Runway</gradient><gray> | <white>Failed to parse the text. Make sure it can be parsed!",
    val invalidArgument: String = "<gradient:#0050ff:#0099ff>Runway</gradient><gray> | <white>Invalid argument. Please use a valid argument!",
    val unknownCommand: String = "<gradient:#0050ff:#0099ff>Runway</gradient><gray> | <white>Sorry but that command is unknown. Please try again!",
    val noPermission: String = "<gradient:#0050ff:#0099ff>Runway</gradient><gray> | <white>You do not have permission to use this command!",
    val notEnoughArguments: String = "<gradient:#0050ff:#0099ff>Runway</gradient><gray> | <white>Not enough arguments. Please use a valid argument!"
)
