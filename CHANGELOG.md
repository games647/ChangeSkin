#Changelog

##### 1.5

* Added support for multiple BungeeCord proxies
* Fixed skin set if the player is longer online than 3 hours
* Fixed missing translations in BungeeCord
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
* Removed player name specific permissions. Use the uuids ones

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
* Added specific skin permssions. You can use changeskin.skin.* changeskin.skin.playerName changeskin.skin.uuid
* Fixed invisible players will be make visible
* Ignore invalid usernames

##### 0.7.2

* Fixed support for PaperSpigot and TacoSpigot
* Fixed support for Craftbukkit servers
* Fixed instant skin apply

##### 0.7.1

* Added native bukkit support

##### 0.7

* Minimize preferences size if you choose your own profile
* Add instant skin changes (skin changes work without relogs)

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
* If you set it to your own UUID your preferences will be reseted

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