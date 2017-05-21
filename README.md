# RetroPie romfilter

This application is designed to let you browse the
list of ROMs you have installed for each of your systems on
a RetroPie. You can quickly filter your lists to search
for specific details or even delete ROMs
(move them to a trash folder).

You should be able to run this directly on a your RetroPie if you install Oracle Java 8.
See the install guide and notes below.

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


### Searching

Since the focus of this application is browsing your ROM collection, here are some
example searches to help illustrate how to use the application:

```
tempest             | Find the term tempest in any field
"super mario"       | Find the phrase 'super mario' (the term super followed by the term mario) in any field
name:"pac man"      | Find the phrase 'pac man' (the term 'pac' followed by the term 'man' in the name field
name:pac man        | Find the term 'pac' in the name field OR the term 'man' in any field (probably not what you wanted)
+name:pac +name:man | Find the term 'pac' in the name field AND the term 'man' in the name field
```

Fields names you can use for search prefixes

```
system, size, name, desc, developer, publisher, genre, players,
region, romtype, releasedate, rating, playcount, lastplayed,
scrapeId, scrapeSource, hash
```

### FAQs

**Q:** When I first start the app it takes forever before it responds.

**A:** Currently it scans the roms and gamelist.xml files at first startup. This takes time. I'll make this nicer in the future.

**Q:** Things changed in the filesystem or I have re-scraped but I don't see the changes in romfilter

**A:** Yeah, I need to add on-demand re-scan. Stop the app, delete the index folder in the app's directory (romfilter-games.index), and start the app again.

**Q:** I'm having a problem

**A:** Wait for the app to become more stable. Try deleting the index folder in the app's directory (romfilter-games.index).

**Q:** Should I open this port up in my router so I can browse my roms while I am away from home or let me cousin look at it?

**A:** No. This app has very little security baked in. Don't do it. Run this on your private network, only.

**Q:** Is the 'hash' the hash of the ROM?

**A:**: No. That is a good idea and I may considering switch to that. But now it is a number that is combination of other fields in the entry.

## Running on a RetroPie


You can (but may not want to) run this application directly on the Raspberry Pi. The setup
instructions are about the same as the Mac version, but even simpler as you shouldn't need
an external configuration file, ```.retropie-romfilter.yml```

You need to have Oracle Java 8 installed. This should be achievalbe via:

```
sudo apt-get install oracle-java8-jdk
```

I've run romfilter in a Raspberry Pi 3b. Application startup was somewhat slow. Scanning the roms and
parsing the gamelist.xml files (about 8500 ROMs across about 15 systems) took less than
50 seconds, about the same amount of time as it took on my Mac.

When idle, the application uses very little CPU. Once warmed up, the application performs well and
searches feel snappy.

The major issue with running on the Pi is the amount of memory used by Grails, the application, and Lucene.
According to Top, romfilter took over 50% of the Pi's memory. This might hinder the RetroPie's
ability to actually run games. For this reason, I continue to recommend running this application
on another system in your LAN

## Screenshots

Browsing filtered list of ROMS
![Filtered, highlighted list of ROMS](https://github.com/kdorff/retropie-romfilter/blob/master/grails-app/assets/images/screenshots/browse.png)
