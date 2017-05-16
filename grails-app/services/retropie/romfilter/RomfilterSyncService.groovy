package retropie.romfilter

import groovy.util.slurpersupport.GPathResult
import groovyx.gpars.GParsPool
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.Logger

import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger

class RomfilterSyncService {
    /**
     * Logger.
     */
    Logger log = Logger.getLogger(getClass())

    /**
     * ConfigService (auto-injected).
     */
    ConfigService configService

    /**
     * IndexerService (auto-injected).
     */
    IndexerIndexingService indexerIndexingService

    /**
     * IndexerService (auto-injected).
     */
    IndexerDataService indexerDataService

    /**
     * HashService.
     */
    HashService hashService

    /**
     * At startup or on demand. Scan all system and gamelist data,
     * TODO: Add on-demand.
     * TODO: Transaction sync by [system, per gamelist, romlist]
     */
    void scanAll() {
        long start = System.currentTimeMillis()
        log.info("Beginning startup scan.")
        scanGamelists()
        scanSystems()
        log.info("Found ${indexerDataService.systemEntryCount} systems")
        log.info("Found ${indexerDataService.gamelistEntryCount} gamelist.xml entries")
        log.info("Found ${indexerDataService.romEntryCount} roms")
        log.info("Startup scan complete. Took ${System.currentTimeMillis() - start}ms")
    }

    /**
     * Startup or on demand. Scan all gamelist.xml files, up to one per system.
     */
    void scanGamelists() {
        long start = System.currentTimeMillis()
        log.info("Finding gamelists.xml files")
        Path gamelistsPath = Paths.get(configService.gamelistsPath)
        GParsPool.withPool {
            foldersContainedWithin(gamelistsPath).eachParallel { Path gamelistFolderPath ->
                scanGamelist(gamelistFolderPath.getFileName().toString(),
                    Paths.get(gamelistFolderPath.toString(), 'gamelist.xml'))
            }
        }
        log.info("All gamelist.xml files found. Took ${System.currentTimeMillis() - start}ms")
    }

    /**
     * Scan a single gamelist.xml file for a specific system.
     */
    void scanGamelist(String system, Path gamelistPath) {
        long start = System.currentTimeMillis()
        List<String> skipSystems = configService.getSystemsToSkip()
        if (system in skipSystems) {
            log.info("Skipping gamelist parsing for ${system}")
        }
        else {
            log.info("Parsing gamelist for ${system}")
            if (Files.exists(gamelistPath) &&
                Files.isReadable(gamelistPath) &&
                Files.isRegularFile(gamelistPath)) {
                parseGamelistFromXml(system,
                    new String(Files.readAllBytes(gamelistPath))
                )
            }
            else {
                log.warn("... Gamelist not found")
            }

            log.info("Gamelist for ${system} parsing complete. Took ${System.currentTimeMillis() - start}ms")
        }
    }

    /**
     * Finds all systems, scans roms for each.
     */
    void scanSystems() {
        long start = System.currentTimeMillis()
        log.info("Looking for systems with roms")
        Path systemsFolderPath = Paths.get(configService.romsPath)
        List<String> skipSystems = configService.getSystemsToSkip()
        GParsPool.withPool {
            foldersContainedWithin(systemsFolderPath).eachParallel { Path systemPath ->
                String system = systemPath.fileName.toString()
                if (system in skipSystems) {
                    log.info("Skipping system ${system}.")
                }
                else {
                    SystemEntry systemEntry = new SystemEntry(
                        name: system,
                    )
                    int count = scanRomsForSystem(system)
                    if (count) {
                        // Only save if the system had roms.
                        indexerIndexingService.saveSystemEntry(systemEntry)
                    }
                }
            }
        }
        log.info("Finished looking for systems. Took ${System.currentTimeMillis() - start}ms")
    }

    /**
     * Scan roms for a system.
     * gamelist.xml for this system MUST already be scanned since we look up the rom's gamelistEntry when
     * creating the RomEntry.
     *
     * @param system
     * @return
     */
    int scanRomsForSystem(String system) {
        Path systemFolderPath = Paths.get(configService.getRomsPathForSystem(system))
        String romGlob = configService.getRomGlobForSystem(system)
        AtomicInteger count = new AtomicInteger(0)
        filesContainedWithin(systemFolderPath, romGlob).each { Path romPath ->
            // Get rid of the path, just keep filename including ext
            String filename = FilenameUtils.getName(romPath.toString())
            RomEntry romEntry = new RomEntry(
                system: system,
                filename: filename,
                size: Files.size(romPath),
            )
            romEntry.hash = hashService.hash(romEntry.filename)
            // The gamelistEntry for this system MUST already be scanned
            romEntry.hasGamelistEntry = romEntry.gamelistEntry != null
            indexerIndexingService.saveRomEntry(romEntry)
            count.incrementAndGet()
        }
        return count.get()
    }

    /**
     * Convert gamelistXml (contents of gamelist.xml file) to Map[String, GamelistEntry]
     * where the key is the filename (path).
     *
     * Performed transformations:
     * 1. If path starts with "./", the "./" will be stripped off.
     * 2. if image startsWith expected location prefix, remove the location prefix.
     *
     * @param gamelistXml
     * @return
     */
    void parseGamelistFromXml(String system, String gamelistXml) {
        String imagesPrefix = configService.imagesPrefix

        // Start parsing the XML
        GPathResult gamelist = new XmlSlurper().parseText(gamelistXml)
        gamelist.children().each { game ->
            GamelistEntry entry = new GamelistEntry(
                system: system,
                scrapeId: game.'@id'.toString(),
                scrapeSource: game.'@source'.toString(),
                path: game.path?.toString() ?: "",
                name: game.name?.toString() ?: "",
                desc: game.desc?.toString() ?: "",
                image: game.image?.toString() ?: "",
                thumbnail: game.thumbnail?.toString() ?: "",
                developer: game.developer?.toString() ?: "",
                publisher: game.publisher?.toString() ?: "",
                genre: game.genre?.toString() ?: "",
                players: (game.players?.toString() ?: "1") as int,
                region: game.region?.toString() ?: "",
                romtype: game.romtype?.toString() ?: "",
                releasedate: convertDateTimeToLong(game.releasedate?.toString() ?: ""),
                rating: (game.rating?.toString() ?: "0.0") as double,
                playcount: (game.playcount?.toString() ?: "0") as int,
                lastplayed: convertDateTimeToLong(game.lastplayed?.toString() ?: ""),
            )

            // Transform path
            if (entry.path.startsWith('./')) {
                entry.path -= './'
            }

            // Transform image
            if (entry.image?.startsWith(imagesPrefix)) {
                entry.image -= imagesPrefix
                entry.image = "${configService.imagesPath}/${entry.image}".toString()
            } else {
                // Image in unknown location, remove it
                entry.image = ''
            }

            // Transform thumbnail
            if (entry.thumbnail?.startsWith(imagesPrefix)) {
                entry.thumbnail -= imagesPrefix
                entry.thumbnail = "${configService.imagesPath}/${entry.thumbnail}".toString()
            } else {
                // Thumbnail in unknown location, remove it
                entry.thumbnail = ''
            }

            indexerIndexingService.saveGamelistEntry(entry)
        }
    }

    /**
     * Return all of the FOLDERS in the specified folder (1 level deep).
     *
     * @param folder folder to search
     * @return list of Path for folders
     */
    List<Path> foldersContainedWithin(Path folder) {
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(folder)
            List<Path> paths = stream.findAll { Path path ->
                return Files.isDirectory(path) && !Files.isHidden(path)
            }
            return paths
        } catch (IOException e) {
            log.error("Exception scanning systems folder", e)
            return []
        }
    }

    /**
     * Return all of the FILES in the specified folder (ignore subfolders).
     *
     * @param folder folder to search
     * @return list of Path for files
     */
    List<Path> filesContainedWithin(Path folder, String glob) {
        long start = System.currentTimeMillis()
        String system = folder.fileName.toString()
        log.info("Scanning for ${system} roms")
        int romsFound = 0
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(folder, glob)
            List<Path> paths = stream.findAll { Path path ->
                return Files.isRegularFile(path) && !Files.isHidden(path)
            }
            romsFound = paths.size()
            return paths
        } catch (IOException e) {
            log.error("Exception scanning system folder for roms", e)
            return []
        }
        finally {
            log.info("Scan for ${system} roms complete, found ${romsFound}. Took ${System.currentTimeMillis() - start}ms")
        }
    }

    /**
     * Convert an emulation station date to a long that just contains
     * yyyymmdd.
     *
     * @param dateTime
     * @return
     */
    long convertDateTimeToLong(String dateTime) {
        long result = 0
        if (dateTime) {
            String[] parts = dateTime.split('T')
            if (parts.length > 0 && parts[0].isLong()) {
                result = parts[0].toLong()
            }
        }
        return result
    }
}
