#Changelog

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