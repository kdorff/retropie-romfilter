---
layout: page
title: Running
menu: main
permalink: /running/
---

## Disclaimer

This code is a very much work in progress. It is ready for early consumption but be prepared to
delete your index every time you download a new version. As of today, there ARE index changes
still to come (I want to support the additional flags that
[Kid's Mode](https://github.com/RetroPie/RetroPie-Setup/wiki/Child-friendly-EmulationStation) supports.

The first time you run the application, the browse page will give you a Javascript alert.
This is because you have no data in your romfilter index. You will need to scan your systems
under Systems and Jobs.

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

### Running the app (development mode)

The fastest way to get the app running is to run it in development mode.

On the command line, from the application's source directory, run the following commands to start
the application.

```
./gradlew clean bootRun
```

> **A few  notes on running the app**
> Here we aren't restricting the memory. This won't be terribly friendly to a Raspeberry Pi.
> If you intend to run with this method on a Pi, you should look into setting JAVA_OPTS or similar
> to restirct the memory. The application's index (like a database) will be stored in the application's
> source directory.

### Running the app (build the war, run it)

On the command line, from the application's source directory, run the following commands to start
the application.

```
./gradlew clean assemble
mkdir exec
cd exec
cp ../build/libs/*.war retropie-romfilter.war
java -Xmx256m -jar retropie-romfilter.war
```

> **Running the WAR notes**
> We are copying application (.war file) that is built during
> the "assemble" phase to the location where we want to run the application. This can be anywhere.
> The key is the directory where you run the applications is where the index (like a database)
> will be stored. If you ran the applicatinon out of the build/libs directory, your index would
> be destoryed when you did a "clean" of the application (and java -jar doesn't work
> as desired when the jar you are running is outside of the current directory). If you run the jar
> from the application source directory, it also seems it get confused, so I recommend you
> copy the war file to another location to run the app.
>
> **This application and memory**
> This application stores data using Lucene, which is an data indexing engine. Lucene will
> use a lot of memory if it is available. Which is great, this will speed up your searches.
> But if you are limited in meory, you should adjust the -Xmx value that Java uses to limit
> memory consumption. I wouldn't recommend going lower than the 256 specified above or you
> many find the application won't run or will become very slow. When
> I omitted the flag (or ran in development mode with no -Xmx setting) the application
> would consume happily >1.5GB of RAM on my Mac. When I ran the application on a Pi 3 with
> no -Xmx value it quickly consumed >50% of the Pi's RAM (according to "top").

### Browsing to the app once it is running

```
http://localhost:8080/games/browse
```

### Searching

See [Searching](/searching)

## Running on a RetroPie

You can (but may not want to) run this application directly on the Raspberry Pi. The setup
instructions are about the same as the Mac version, but even simpler as you shouldn't need
an external configuration file, ```.retropie-romfilter.yml```

You need to have Oracle Java 8 installed. This should be achievalbe via:

```
sudo apt-get update
sudo apt-get install oracle-java8-jdk
```

I've run romfilter in a Raspberry Pi 3b. The very first time you assemble (or start) the application
it has to download a large number of libraries. The first assemble took about 7 minutes on my Pi.
Once the app is running, it may take a few additional moments on the Pi to "warm up" the application
when accessing a page for the first time. Once warmed up, browsing and scanning the roms actually 
works pretty well and feels snappy. When idle, the application uses very little CPU.

The major issue with running on the Pi is the amount of memory used by Grails, the application, and Lucene.
According to Top, romfilter took over 50% of the Pi's memory. This might hinder the RetroPie's
ability to actually run games. For this reason, I continue to recommend running this application
on another system in your LAN
