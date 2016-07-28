# ChangeSkin

[![Build Status](https://travis-ci.org/games647/ChangeSkin.svg?branch=master)](https://travis-ci.org/games647/ChangeSkin)

## Description

This plugin allows your players to change their skins by command. These skins have to be downloaded from Mojang, because
Minecraft clients only accept from Mojang signed skins. But you can choose every skin that another minecraft user
currently has.

Moreover it will be possible to set a custom standard skin. If you want to, this plugin can set this standard skin for
all players who still have the default steve or alex skin.

## Features

* Skins for offline mode
* Upload custom skins
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

* /setskin <uuid > - Sets your skin to be equal to the player with the selected uuid
* /setskin < playerName>
* /setskin < onlinePlayer > < newSkinUUID/newSkinPlayerName >
* /skinupdate [ onlinePlayer ] - Invalidates the database entry and fetches the fresh skin from the Mojang servers

## Permissions

* changeskin.command.skinupdate - Command to refresh the own skin
* changeskin.command.setskin.* - All the commands below
* changeskin.command.setskin - Set your own skin
* changeskin.command.setskin.other - Set the skin of other players

Whitelist and blacklist permissions

Whitelist

* changeskin.skin.whitelist.uuid - Allow this specific skin
* changeskin.skin.whitelist.* - allows all skins

Blacklist

* changeskin.skin.whitelist.*
* -changeskin.skin.whitelist.uuid

=> This means all skins are allowed except the ones with the uuid in the blacklist list