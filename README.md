# RetroPie romfilter

This application is designed to let you browse the
list of ROMs you have installed for each of your systems on
a RetroPie. You can quickly filter your lists to search
for specific details or even delete ROMs
(move them to a trash folder).

You CAN run this application directly on a RetroPie, but
you might not be pleased with the performance. Instead, I prefer to
run RetroPie romfilter on another machine on the same network.

# Disclaimer

This code is a work in progress and is not ready for public consumption.

The first time you run the app it will need to scan your system. This can take several minutes
depending on how many roms you have.

## Installing and running from source on a Mac

### Fetching the source

git clone https://github.com/kdorff/retropie-romfilter.git


### Mounting roms and configs

On the system you intend to run the application, mount RetroPie's "roms" and "config" shares. 
The simplest way to do this to run Finder and press Command-K and mount both of

```
smb://192.168.1.22/roms/
smb://192.168.1.22/configs/
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
named ".retropie-romfilter.yml". The contents of this file should be similar to

```
retropie:
    emulationStation:
        romsPath: "/Volumes/roms"
        gamelistsPath: "/Volumes/configs/all/emulationstation/gamelists"
        imagesPath: "/Volumes/configs/all/emulationstation/downloaded_images"
    romfilter:
        trashPath: "/Volumes/roms/.trash"
```        

Note the trashPath here. When you "Delete" a rom using RetroPie-romfilter it will move the 
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

A: Currently it scans the roms and gamelist.xml files at first startup. This takes time. I'll make this nicer in the future (maybe).

Q: I'm having a problem

A: Wait for the app to become more stable. Try deleting the database file stored in the app directory.

## Installing and running from source on a RetroPie

### Fetching the source

Coming soon

### Running the app

Coming soon

### FAQs

Coming soon

