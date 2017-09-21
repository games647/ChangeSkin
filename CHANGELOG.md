# Changelog

##### 2.4.2

* Consider more player attributes for instant skin changes
    * Current available health
    * Player experience
    * Food level
* Use player UUID rather than custom offline UUID (Related #86)

##### 2.4.1

* Uncomment MySQL settings by default
* Load upload on all other platforms besides Bukkit too
* Remove mcapi.de because it hits the rate-limit too
* Re-apply skin after you switch from a blacklisted server
* Document skull command. Should have done this way earlier

##### 2.4

* Add support for HTTP-proxies
* Fix showing nametag for NameTagEdit after skin change
* Clear skin for servers on the blacklist (Fixes #69)
* Use read-/write String for plugin messages in Sponge (Removes usage of deprecated methods)
* General code cleanup
* Fix compatibility with NCP (Fixes #58)
* Add pom data to the plugin meta file automatically (Sponge)
* Fix too strict catch block for non premium players (Fixes #71) (related to #68)
* Replace usage of deprecated methods for 1.12
* Fix create loginresult after breaking bungee update (Fixes #65)
* Fix config loading in Sponge

##### 2.3.3

* Forward skinPerm config to bukkit to check it only if necessary 
* Fix missing bungeecord aliases
* Fix forwarding permission checking
* Fix lowercase bungee perm

##### 2.3.2

* Add permission node for /skinupdate < other >

##### 2.3.1

* Remove debug from SkinDownloader (Fix #42)

##### 2.3

* Added skin upload for Sponge and Bungee
* Added skin select for Sponge and Bungee
* Keep the held item slot after skin updating
* Added a keepskin property to ignore auto updating
* Fix NPE on login for sponge
* Cache Sponge name->uuid and skins using the Sponge API
* Fix skin auto updating by selecting only the newest skin from database

##### 2.2

* Add Hikari database connection pooling
* Fix skin uploading

##### 2.1

* Remove aggressive UUID caching
* Remove name resolves from database
* Automatically updates the skin if the stored skin is older than x minutes
* Fix loading skin for the new database schema
* Fixed 429 for cracked uuids

##### 2.0

* Correctly catch not premium name resolves from third-party api
* Added skin upload support for bukkit
* Added specific skin select command for bukkit

##### 1.13

* Load database record async too for restoring skins in BungeeCord
* Enforce a specific timeout (Fixes #37)
* Fix antibot condition

##### 1.12.3

* Fix Bungee anti bot feature is not applying the skin

##### 1.12.2

* Re-add valid name checking
* [Security] Fix forwarding permission on bukkitPermissions enabled

##### 1.12.1

* Fixed temporarily permissions forwarding for bukkit permissions

##### 1.12

* Added Mojang independent skin downloading
* Fix wildcard other permission in BungeeCord
* Fix reset command not working
* Fix NPE on bukkit perm check and skin reset
* Shrink database size by truncating mojang prefix url which is constant
* Fix database creation

##### 1.11.1

* Fix updateabilities results into always flight for incompatibility with other plugins
* Fixed wrong profile save on BungeeCord

##### 1.11

* Add command forward support if the command only runs on the backend server

##### 1.10

* Fix NPE on skinupdate bungeecord
* Fix missing skinupdate < other > command for bungee
* Fix json parsing from third party api
* Add BungeeCord support for Sponge servers
* Add instant update support for sponge
* Add missing skin permissions for the Sponge server

##### 1.9

* Fixed health abilities shown on instant updates
* Add skinupdate < other > command for console usage
* Fixed NPE on BungeeCord lazy loading
* Switch to mcapi.ca as they fixed rate-limiting issues and seems to be better

##### 1.8

* Allow the command /skin set name as alternative command
* Set a useragent for accessing the third-party API
* Added the possibility to limit mojang requests
* Fixed skin cooldown if it's set to 0 - Fixed memory leak
* Removed lazy loading if a skin is already present (online-mode)
* Switch to offline uuid matching (temp)
* Added bukkit instead of bungee permissions checking (configurable)
* Allow lazy loading for certain servers in a BungeeCord network

##### 1.7.3

* Fix NPE on instant updates for not finding the correct player (Fixes #24)

##### 1.7.2

* Completely clean up database resources
* Update abilities on instant update
* Added config option about how many requests should be established until the plugin uses the third-party API

##### 1.7.1

* Fix table creation in SQLite
* Fix BungeeCord permissions (Thanks to @FabioZumbi12)
* Fix duplicate no permission message (Thanks to @FabioZumbi12)
* Fix skinupdate command in BungeeCord (Thanks to @FabioZumbi12)

##### 1.7

* Added skin data index (as suggested by @ieti)
* Added experimental Sponge support
* Added third-party api for fetching the uuid
* skinupdate command to invalidate the database entry (Added new locale messages)
* Removed error message if message file already exists (as suggested by @ieti)
* Load embed message file per default
* Fix NPE on skin reset
* Fix display name is used instead of tablist name for instant updates
* Fix self instant update if the player is in a vehicle

##### 1.6

* Added Bungee API methods for setting a new skin
* Fetch UUIDs from the database first before asking Mojang -> reduce Mojang requests
* Fix caching of pure cracked players (who don't use a premium username) -> reduce Mojang requests
* Fixed missing no permission message
* More user friendly messages on rate-limiting (added new locale messages)
* More aggressive uuid caching

##### 1.5

* Added support for multiple BungeeCord proxies
* Fixed skin set if the player is longer online than 3 hours
* Fixed missing translations in BungeeCord
* Fixed error messages text on uuid rate-limiting. (It's not a skin downloading rate-limit)
* Removed database cache as the sql cache is powerful enough

##### 1.4.1

* Fixed instant updates for BungeeCord servers
* Fix chunk loading issues on instant updates

##### 1.4

* Added skin cooldown
* Added localization

##### 1.3.2

* Fixed saving for empty skins

##### 1.3.1

* Fixed command permission other not working

##### 1.3

* Fixed skin apply if the player has already a skin (for example: online mode)

##### 1.2

* Fixed blacklist permissions
    * Changed from an extra blacklist permission to a negative one
    * Sorry for that change it wasn't possible otherwise

##### 1.1

* Fix bungeecord detection error on Craftbukkit

##### 1.0

* Add blacklist and whitelist permission nodes
* Introduced new permissions
    * changeskin.skin.whitelist.*
    * changeskin.skin.whitelist.uuid
    * changeskin.skin.blacklist.uuid
* Removed deprecated methods from the BungeeCord module
* Removed player name specific permissions. Use the UUIDs ones

##### 0.9.4

* Fix support for slim skin models

##### 0.9.3

* Fix preference saving if the same skin is already in the database

##### 0.9.2

* Fix Bukkit command permission node

##### 0.9.1

* Try to fix the save process for players who doesn't have a skin set
* Restore bukkit compatibility
* Fix skin loading from a mySQL database

##### 0.9

* Added BungeeCord support
* Fixed support for MySQL
* Fixed support for default skins, so that they will be stored in the database too
* Fixed support for user who doesn't have a skin set in their Mojang account.

##### 0.8

* Added database storage
* Added reset command /skin reset
* Added specific skin permissions. You can use changeskin.skin.* changeskin.skin.playerName changeskin.skin.uuid
* Fixed invisible players will be make visible
* Ignore invalid usernames

##### 0.7.2

* Fixed support for Paper and TacoSpigot
* Fixed support for Craftbukkit servers
* Fixed instant skin apply

##### 0.7.1

* Added native bukkit support

##### 0.7

* Minimize preferences size if you choose your own profile
* Add instant skin changes (skin changes work without relogin)

##### 0.6

* Added command to set skin for other players /setskin player <uuid/name>
* Added permissions to the plugin.yml
* Added child permissions

##### 0.5

* [Experimental] Re fetch skins for player who don't have one
* Add UUID cache

##### 0.4

* Add /setskin <playerName> command
* Player skin can now be resolved also by their names
* Add default skins
* If you set it to your own UUID your preferences will be reset

##### 0.3

* [Fix] Properly ignore cancelled login events
* Clean up saved data on plugin disable
* Save user preferences

##### 0.2

* Added /changeskin as an alias for setskin
* Added setskin API method
* Fix skin loading if no longer in cache
* Use skins from already playing players if possible -> reduces skin requests

#### 0.1

+ First release
