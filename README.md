# ChangeSkin

## Description

This plugin allows your players to change their skins with a simple command. You can choose every skin another
Minecraft user currently has. It is also possible to set a custom standard skin. If you want to, this plugin can set
this standard skin for all players who still have the default Steve or Alex skin.

## Security

Software security is important. In order to inform people quickly and get patches deployed promptly, this projects
discloses issues transparently and detailed.

|Issue| Severity | Affected | Patched | Impact
|---|---|---|---|---|
|[#205](https://github.com/games647/ChangeSkin/issues/205)|Moderate|Bungee Module after [92ed029](https://github.com/games647/ChangeSkin/commit/92ed0296e6fcbd0acf04f4f06e417403d5b22ccd) - (2.3.2+) |[ba1957a](https://github.com/games647/ChangeSkin/commit/ba1957ac5eff29652f8161c72ff60f765a97453e)| DOS (Denial-of-Service) Modified Minecraft clients could send malicious plugin messages. This causes an exception. Exceptions are CPU intensive if they are spammed |

If you have any questions, please open a new issue or send a private message on Spigot. Later should be also used
for reporting in order to work on fix before publicly disclosing it.

## Features

* Skins for offline mode servers
* Upload custom skins (using the /skinupload command)
* BungeeCord and Sponge support
* SQL Storage
* Change your skin - Every skin which has ever uploaded to Mojang is allowed
* Lightweight
* Easy to use
* Implemented cache to benefit performance
* Nearly no mojang rate limits due to caching
* No client modification needed
* Possibility to set a standard skin

## Commands

    /setskin <uuid> [keep] - Sets your skin to be the same as the player with the selected uuid. 
    /setskin <username> [keep] - Sets the skin equal to the owner of that Mojang account with the given username.
    /setskin reset - Resets the skin of the invoker to his one.
    /setskin <onlinePlayer> <newSkinUUID|newSkinPlayerName> [keep]
    /skinupdate [onlinePlayer] - Invalidates the database entry and fetches the fresh skin from the Mojang servers.
    /skinupload <url> - Upload a skin to a mojang account
    /skin-select <name> - Choose a skin from the database which has a specified name
    /skinskull - Changes the skin of a holding skull from the database which has a specified name
    /skin-info - Show details about the currently assigned skin like skin url owner name, uuid and more

Keep prevents the skin from auto updating. See the config for more information about auto updating.

/skin, /set-skin, /changeskin can used as an alias for /setskin

## Permissions

For Sponge users:

    Sponge uses a different permissions model. This means changeskin.command.setskin includes all child permissions.
    (Including changeskin.command.setskin.other). To workaround this please use changeskin.command.<command>.base 
    instead of changeskin.command.<command> to give the permission only to modify their own skin. 
    **This workaround will be removed in the next major version 4.0. Then the Bukkit and BungeeCord will have the same logic.

* changeskin.command.skinupdate - Command to refresh a player's own skin
* changeskin.command.skinupdate.other.uuid - Allows to update the skin of that specific user
* changeskin.command.skinupdate.other.* - Allowed to update the skins of all players
* changeskin.command.setskin.* - Includes all the commands below
* changeskin.command.setskin - Set your own skin
* changeskin.command.setskin.other - Set the skin of other players
* changeskin.command.skinselect - Select a skin from the database
* changeskin.command.skinupload - Upload a skin to one of the configured accounts
* changeskin.command.skinskull - Use the skull command
* changeskin.command.skininfo - Use the info command

### Whitelist and blacklist permissions

Whitelist

* changeskin.skin.whitelist.uuid - Allow this specific skin
* changeskin.skin.whitelist.* - Allows all skins

Blacklist

* changeskin.skin.whitelist.*
* -changeskin.skin.whitelist.uuid

=> This means all skins are allowed except the ones with the uuid in the blacklist list

## Requirements

* Java 8+
* Minecraft server software
    * Spigot 1.8.8+ or any fork of it (ex: Paper)
    * Sponge 7+
    * BungeeCord 1.13+ (Keep in mind that BungeeCord is backwards compatible)

## Network requests

This plugin performs network requests to:

* https://api.mojang.com - retrieving skin reference and uuid data
* https://sessionserver.mojang.com - downloading skin data
* https://authserver.mojang.com - uploading skins
* https://api.ashcon.app - service for skin donwloads if rate limited

## Development builds

Development builds of this project can be acquired at the provided CI (continuous integration) server. It contains the
latest changes from the Source-Code in preparation for the following release. This means they could contain new
features, bug fixes and other changes since the last release.

Nevertheless builds are only tested using a small set of automated and a few manual tests. Therefore they **could**
contain new bugs and are likely to be less stable than released versions.

https://ci.codemc.org/job/Games647/job/ChangeSkin/changes

## Upload a skin to the database

1. Put a Minecraft account into the config
2. Check the logs if it's authenticated correctly (ChangeSkin startup).
3. Type /skin-upload [url] (example: https://i.imgur.com/4lV1m26.png)
4. Now you see the skin id it's saved as
5. Type /skin-select 3 or /skin-select skin-3

## How to install on BungeeCord

1. Install the plugin on both BungeeCord and Bukkit server
2. Activate BungeeCord support in your Spigot configuration (`bungeecord: true` in spigot.yml)
3. Check the settings of the ChangeSkin config in the BungeeCord instance
    * BungeeCord doesn't support SQLite per default, so you should change the configuration to MySQL or MariaDB
