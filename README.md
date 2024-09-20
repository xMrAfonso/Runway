![Main Logo](https://cdn.modrinth.com/data/cached_images/e4bcf9fa21266eef2674f7459f2ff1cd786012a1.png)

Runway allows you to support MiniMessage formatting in messages, menus, titles, items, MOTD and Tablist from any plugin. The plugin works at the protocol-level and modifies incoming packets sent by plugins.

If enabled, PlaceholderAPI/MiniPlaceholders is also supported in messages parsed by Runway. You also have the option to add your own static custom placeholders in MiniMessage.

[![image](https://github.com/xMrAfonso/Runway/assets/44532605/e29ace3a-b660-40b9-9751-933b2d91288a)](https://hangar.papermc.io/Afonso/Runway/) [![image](https://github.com/xMrAfonso/Runway/assets/44532605/4b5b30dc-2117-48f8-bf74-a25a7c38285d)](https://modrinth.com/project/runway)

## Installation
### Requirements
**Server:**
- [Paper 1.20.6+ (below is untested)](https://papermc.io/downloads/paper)
- Java 21

**Plugins:**
- [PlaceholderAPI](https://hangar.papermc.io/HelpChat/PlaceholderAPI) (Optional)
- [MiniPlaceholders](https://hangar.papermc.io/MiniPlaceholders/MiniPlaceholders) (Optional)
  
### How to use
By default, to allow MiniMessage formatting in your messages, you need to include the prefix `[mm]` inside the line, there is no specific requirement if it needs to be in the front, back or middle of the sentence.
This can also be disabled in the *config.yml* which will make all incoming packets to be formatted using MiniMessage.

PlaceholderAPI/MiniPlaceholders support uses the prefix `[p]` to detect when it should parse placeholders. The same rules of MiniMessage formatting apply here.

You can also make system messages be sent in the action bar by including the prefix `[actionbar]` in the message.
**Commands:**
- `/runway reload` - Reloads the plugin configuration.
- `/runway parse <text>` - Parses the given text to MiniMessage.
## Support & Contact
Feel free to open an issue if you find any issues. For feedback or further assistance, contact me via discord: `@mrafonso`

## Building
To compile the plugin, simply use `gradle clean build` and it should compile perfectly.

There is no need for shading/implementing anything since it uses Paper's Plugin Loader.
