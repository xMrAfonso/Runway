# Runway

Runway allows you to support MiniMessage formatting in messages, menus, titles, scoreboards, items, MOTD and Tablist from any plugin. The plugin works at the protocol-level and modifies incoming packets sent by plugins.

If enabled, PlaceholderAPI/MiniPlaceholders is also supported in messages parsed by Runway. You also have the option to add your own static custom placeholders in MiniMessage.

[![image](https://github.com/xMrAfonso/Runway/assets/44532605/e29ace3a-b660-40b9-9751-933b2d91288a)](https://google.com) ![image](https://github.com/xMrAfonso/Runway/assets/44532605/4b5b30dc-2117-48f8-bf74-a25a7c38285d)


## Installation
### Requirements
**Server:**
- Paper 1.20.4+ (or Forks)
- Java 17

**Plugins:**
- ProtocolLib (Required)
- PlaceholderAPI (Optional)
- MiniPlaceholders (Optional)
  
### How to use
By default, to allow MiniMessage formatting in your messages, you need to include the prefix `[mm]` inside the line, there is no specific requirement if it needs to be in the front, back or middle of the sentence.
This can also be disabled in the *config.yml* which will make all incoming packets to be formatted using MiniMessage.

## Support & Contact
Feel free to open an issue if you find any issues. For feedback or further assistance, contact me via discord: `@mrafonso`

## Building
To compile the plugin, simply use `gradle clean build` and it should compile perfectly.

There is no need for shading/implementing anything since it uses Paper's Plugin Loader.
