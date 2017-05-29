# RetroPie romfilter

I was inspired to write this application after playing with
[RetroPie-Manager](https://github.com/botolo78/RetroPie-Manager) .
See my FAQ for why I started this app.

This application is designed to let you browse and filter the list of ROMs you have installed
across all of your systems on your RetroPie.

Features include
* Filtering and sorting your the details of your ROMs in nearly any way you can imagine
* Delete individual ROMs

You *can* run this directly on a your RetroPie if you install Oracle Java 8,
**but at this time I don't recommend it** unless you are prepared to reduce the memory provided to romfilter
and accept the risk that the reduced system memory might keep games from running.
I don't know how much memory these emulators require. I'd love some advise on this.

I currently run romfilter on another machine on the same network
and configure it to communicate with my RetroPie using the SMB shares
RetroPie automatically creates.

# Disclaimer

This code is a very much work in progress. It is ready for early consumption but be prepared to
delete your index every time you download a new version. As of today, there ARE index changes
still to come (I want to support the additional flags that
[Kid's Mode](https://github.com/RetroPie/RetroPie-Setup/wiki/Child-friendly-EmulationStation) supports.

The first time you run the application, the browse page will give you a Javascript alert.
This is because you have no data in your romfilter index. You will need to scan your systems
under Systems and Scan Jobs.

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

**A:** Update! The latest version of the application supports scanning on-demand under Systems and Scan Jobs.

**Q:** Things changed in the filesystem or I have re-scraped but I don't see the changes in romfilter

**A:** Re-scan your systems under Systems and Scan Jobs.

**Q:** I'm having a problem

**A:** Wait for the app to become more stable. Try stopping the app, deleting the index folder in the app's directory (romfilter-games.index), and restarting the app. You will need to re-scan your systems (Systems and Scan Jobs).

**Q:** Should I open this port up in my router so I can browse my roms while I am away from home or let me cousin look at it?

**A:** No. This app has no security baked in. Don't do it. Run this on your private network, only.

**Q:** Is the 'hash' the hash of the ROM?

**A:**: No. It is a unique number based on a combination of other fields in the entry (path, system, rom file size).

**Q**: Why did you make this app?

**A**: I started to extend RetroPie-Manager (I made a PR) but it seems that that project has been mostly
abandoned. I'm not well versed enough in Python / Django to become the new maintainer.
After playing with Django for a few hours, I was convinced I could just make the app I needed with
far less effort than augmenting (and maintaining) the Django app.
I'm a Groovy / Grails programmer by day, so it was a natural choice.
The features I needed I knew how to develop with Groovy and Grails where I didn't
feel confident I could do what I wanted this app to do in PHP or Python in someone else's project,
given my level of experience in those languages (and lack of libraries?).
And this search is amazing.

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

## Screenshots

Browsing filtered list of ROMS
![Filtered, highlighted list of ROMS](https://github.com/kdorff/retropie-romfilter/blob/master/grails-app/assets/images/screenshots/browse.png)

Systems and Scan Jobs
![Filtered, highlighted list of ROMS](https://github.com/kdorff/retropie-romfilter/blob/master/grails-app/assets/images/screenshots/scanning.png)
