# RetroPie romfilter

This application is designed to let you browse the
list of ROMs you have installed for each of your systems on
a RetroPie. You can quickly filter your lists to search
for specific details or even delete ROMs
(move them to a trash folder).

You should be able to run this directly on a your RetroPie if you install Oracle Java 8.
I haven't yet done any performance testing to see how well it runs on a Pi.

Currently, I prefer to run romfilter on another machine on the same network.

# Disclaimer

This code is a work in progress and is not ready for public consumption.

The first time you run the app it will need to scan your system. This will take a bit of time.

## A quick note about scraping

Not all scrapers are the same. The scraper that you can run directly within
EmulationStation is not work doing across all of your roms unless you are
going to give the Yes / No for EACH rom. Who has the desire to do this?

Instead, use [Steven Selph's Scraper](https://github.com/retropie/retropie-setup/wiki/scraper)
for bulk scraping and then use the built-in scraper for individual roms
that Steven's scraper didn't identify.

## Installing and running from source on a Mac

### Fetching the source

git clone https://github.com/kdorff/retropie-romfilter.git


### Mounting roms and configs

On the system you intend to run the application, mount RetroPie's "roms" and "config" shares. 
The simplest way to do this to run Finder and press Command-K and mount both
of the following shares as Guest.

```
smb://YOUR_RETRO_PIE_IP_ADDRESS/roms/
smb://YOUR_RETRO_PIE_IP_ADDRESS/configs/
```

You should now be able to find the data from your RetroPie in 

```
/Volumes/roms
/Volumes/configs
```

### Application Configuration

The default configuration for the application is to look for roms, images, and gamelists 
in the default location for running the application on a RetroPie. But, we want to use the 
"/Volumes/roms" and "/Volumes/config" to find the data the application will use.

To override the configuration to use the mounts, create a file in your home directory 
named ```.retropie-romfilter.yml``` The contents of this file should be similar to

```
retropie:
    emulationStation:
        romsPath: "/Volumes/roms"
        gamelistsPath: "/Volumes/configs/all/emulationstation/gamelists"
        imagesPath: "/Volumes/configs/all/emulationstation/downloaded_images"
    romfilter:
        trashPath: "/Volumes/roms/.trash"
```        

Note the trashPath here. When you "Delete" a rom using RetroPie romfilter it will move the 
ROM file to a system folder within the trashPath.

### Running the app

Via the command line, from the application's source directory run

```
./gradlew bootRun
```

And navigate your browser to

```
http://localhost:8080/systems
```

### FAQs

Q: When I first start the app it takes forever before it responds.

A: Currently it scans the roms and gamelist.xml files at first startup. This takes time. I'll make this nicer in the future.

Q: Things changed in the filesystem or I have re-scraped but I don't see the changes in romfilter

A: Yeah, I need to add on-demand re-scan. Stop the app, delete the database in the app's local directory, and start the app again. Sorry. I'll do something about this soon.

Q: I'm having a problem

A: Wait for the app to become more stable. Try deleting the database file stored in the app directory.

Q: Should I open this port up in my router so I can browse my roms while I am away from home or let me cousin look at it?

A: No. This app has very little security baked in. Don't do it. Run this on your private network, only.

## Installing and running from source on a RetroPie

### Fetching the source

Coming soon

### Running the app

Coming soon

### FAQs

Coming soon

## Screenshots

List of systems

![List of systems](https://github.com/kdorff/retropie-romfilter/blob/master/grails-app/assets/images/screenshots/listOfSystems.png)

Filtered, highlighted list of ROMS
![Filtered, highlighted list of ROMS](https://github.com/kdorff/retropie-romfilter/blob/master/grails-app/assets/images/screenshots/listOfRomsWithHighlightFiltering.png)

Details for one ROM
![Details for one ROM](https://github.com/kdorff/retropie-romfilter/blob/master/grails-app/assets/images/screenshots/gamelistEntryDetails.png)
