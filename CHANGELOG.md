#Changelog

##### 0.9.1

* Try to fix the save process for players who doesn't have a skin set

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