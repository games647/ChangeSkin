# ChangeSkin

## Description

This plugin allows your players to change their skins by command. These skins have to be downloaded from Mojang, because
Minecraft clients only accept from Mojang signed skins. But you can choose every skin that another Minecraft user
currently has.

Moreover it will be possible to set a custom standard skin. If you want to, this plugin can set this standard skin for
all players who still have the default steve or alex skin.

## Dev builds

https://ci.codemc.org/job/Games647/job/ChangeSkin/changes

## Features

* Skins for offline mode
* Upload custom skins (using the skin upload command)
* BungeeCord and Sponge support
* SQL-Storage
* Change your skin - Every skin which has ever uploaded to Mojang is allowed
* Lightweight
* Easy to use
* Implemented cache to benefit performance
* Nearly no mojang rate limits due caching
* No client modification needed
* Possibility to create a standard skin

## Commands

    /setskin <uuid> [keep] - Sets your skin to be equal to the player with the selected uuid. 
    /setskin <playerName> [keep] - Sets the skin equal to the owner of that Mojang with the given playername
    /setskin reset - Removes the current skin of the invoker
    /setskin <onlinePlayer> <newSkinUUID/newSkinPlayerName> [keep]
    /skinupdate [onlinePlayer] - Invalidates the database entry and fetches the fresh skin from the Mojang servers
    /skinupload <url> Upload a skin to a mojang account
    /skin-select <name> Choose a skin from the database which has a specified name
    /skinskull Changes the skin of a holding skull from the database which has a specified name

Keep prevents the skin from auto updating. See the config for more information about auto updating.

/skin, /set-skin, /changeskin can used as an alias for /setskin

## Permissions

For Sponge users

    Sponge uses a different permissions model. This means changeskin.command.setskin includes all child permissions.
    (Including changeskin.command.setskin.other). To workaround this please use 
    changeskin.command.<command>.base instead of changeskin.command.<command> to give the permission only to modify 
    their own skin. **This workaround will be removed in the next major version 4.0. Then the Bukkit and BungeeCord
    will have the same logic.

* changeskin.command.skinupdate - Command to refresh the own skin
* changeskin.command.skinupdate.other.uuid - Allowed to update the skin of that specific user
* changeskin.command.skinupdate.other.* - Allowed to update all other skins
* changeskin.command.setskin.* - All the commands below
* changeskin.command.setskin - Set your own skin
* changeskin.command.setskin.other - Set the skin of other players
* changeskin.command.skinselect - Select a skin from the database
* changeskin.command.skinupload - Upload a skin to one of the configured accounts
* changeskin.command.skinskull - Use the skull command

Whitelist and blacklist permissions

Whitelist

* changeskin.skin.whitelist.uuid - Allow this specific skin
* changeskin.skin.whitelist.* - allows all skins

Blacklist

* changeskin.skin.whitelist.*
* -changeskin.skin.whitelist.uuid

=> This means all skins are allowed except the ones with the uuid in the blacklist list

## Upload a skin to the database

1. Put a Minecraft account into the config
2. Check the logs if it's authenticated correctly (ChangeSkin startup).
3. /skin-upload < url > example: https://i.imgur.com/4lV1m26.png
4. Now you see the skin id it's saved as
5. /skin-select 3 or /skin-select skin-3

## How to install on BungeeCord
1. Install the plugin on both BungeeCord and Bukkit server
2. Activate BungeeCord support in your Spigot configuration
3. Check the settings of the ChangeSkin config in the BungeeCord instance
    * BungeeCord doesn't support SQLite per default, so you should change the configuration to MySQL or MariaDB
