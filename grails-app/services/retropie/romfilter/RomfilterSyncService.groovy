package retropie.romfilter

import groovy.util.slurpersupport.GPathResult
import groovyx.gpars.GParsPool
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.Logger
import org.apache.lucene.document.Document
import retropie.romfilter.indexed.Game

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
    IndexerDataService indexerDataService

    /**
     * At startup or on demand. Scan all system and gamelist data,
     * TODO: Add on-demand.
     * TODO: Transaction sync by [system, per gamelist, romlist]
     */
    void scanAll() {
        long start = System.currentTimeMillis()
        log.info("Staring scan of all systems.")
        scanGamelists()
        log.info("Found ${indexerDataService.gamesCount} Games")
        log.info("Scanning all systems complete. Took ${System.currentTimeMillis() - start}ms")
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
                String system = gamelistFolderPath.fileName.toString()
                if (system in configService.systemsToSkip) {
                    log.info("Skipping system ${system}")
                }
                else {
                    Path romsPath = Paths.get(configService.romsPath, system)
                    Map<String, Path> romFilnameToPath = filesContainedWithin(
                        romsPath, configService.getRomGlobForSystem(system)).collectEntries { Path romPath ->
                        String key = romPath.fileName.toString()
                        return [(key): romPath]
                    }

                    scanGamelist(
                        gamelistFolderPath.getFileName().toString(),
                        Paths.get(gamelistFolderPath.toString(), 'gamelist.xml'),
                        romFilnameToPath)
                }
            }
        }
        log.info("All gamelist.xml files found. Took ${System.currentTimeMillis() - start}ms")
    }

    /**
     * Scan a single gamelist.xml file for a specific system.
     */
    void scanGamelist(String system, Path gamelistPath, Map<String, Path> unmatchedRoms) {
        long start = System.currentTimeMillis()
        List<String> skipSystems = configService.getSystemsToSkip()
        if (system in skipSystems) {
            log.info("Skipping gamelist parsing for ${system}")
        }
        else {
            log.info("Parsing gamelist for ${system}")
            Document doc = new Document()
            if (Files.exists(gamelistPath) &&
                Files.isReadable(gamelistPath) &&
                Files.isRegularFile(gamelistPath)) {
                parseGamelistFromXml(
                    system,
                    new String(Files.readAllBytes(gamelistPath)),
                    doc,
                    unmatchedRoms
                )

                unmatchedRoms.each { String filenameUnused, Path path ->
                    createGameWithoutGamelistEntry(system, path, doc)
                }
            }
            else {
                log.warn("... Gamelist not found")
            }

            log.info("Gamelist for ${system} parsing complete. Took ${System.currentTimeMillis() - start}ms")
        }
    }

    /**
     * Convert gamelistXml (contents of gamelist.xml file) to Map[String, GamelistEntry]
     * where the key is the filename (path).
     *
     * Performed transformations:
     * 1. If path starts with "./", the "./" will be stripped off.
     * 2. if image startsWith expected location prefix, remove the location prefix.
     *
     * @param system system being scanned
     * @param gamelistXml content of a gamelist.xml file
     * @param doc a lucene document object for indexing. The same document is reused for all Game indexing.
     * @return
     */
    void parseGamelistFromXml(String system, String gamelistXml, Document doc, Map<String, Path> unmatchedRoms) {
        String imagesPrefix = configService.imagesPrefix

        // Start parsing the XML
        GPathResult gamelist = new XmlSlurper().parseText(gamelistXml)
        gamelist.children().each { gamelistGame ->
            Game game = new Game(
                system: system,
                scrapeId: gamelistGame.'@id'.toString(),
                scrapeSource: gamelistGame.'@source'.toString(),
                path: gamelistGame.path?.toString() ?: "",
                name: gamelistGame.name?.toString() ?: "",
                desc: gamelistGame.desc?.toString() ?: "",
                image: gamelistGame.image?.toString() ?: "",
                thumbnail: gamelistGame.thumbnail?.toString() ?: "",
                developer: gamelistGame.developer?.toString() ?: "",
                publisher: gamelistGame.publisher?.toString() ?: "",
                genre: gamelistGame.genre?.toString() ?: "",
                players: (gamelistGame.players?.toString() ?: "1") as int,
                region: gamelistGame.region?.toString() ?: "",
                romtype: gamelistGame.romtype?.toString() ?: "",
                releasedate: convertDateTimeToLong(gamelistGame.releasedate?.toString() ?: ""),
                rating: (gamelistGame.rating?.toString() ?: "0.0") as double,
                playcount: (gamelistGame.playcount?.toString() ?: "0") as int,
                lastplayed: convertDateTimeToLong(gamelistGame.lastplayed?.toString() ?: ""),
            )

            // Transform path
            if (game.path.startsWith('./')) {
                game.path -= './'
            }

            // Transform image
            if (game.image?.startsWith(imagesPrefix)) {
                game.image -= imagesPrefix
                game.image = "${configService.imagesPath}/${game.image}".toString()
            } else {
                // Image in unknown location, remove it
                game.image = ''
            }

            // Transform thumbnail
            if (game.thumbnail?.startsWith(imagesPrefix)) {
                game.thumbnail -= imagesPrefix
                game.thumbnail = "${configService.imagesPath}/${game.thumbnail}".toString()
            } else {
                // Thumbnail in unknown location, remove it
                game.thumbnail = ''
            }

            boolean romFound = false
            if (unmatchedRoms.containsKey(game.path)) {
                unmatchedRoms.remove(game.path)
                romFound = true
            }

            if (romFound) {
                indexerDataService.saveGame(game, doc)
            }
        }
    }

    /**
     * Create a Game entry for a rom that doesn't have a gamelist.xml entry.
     *
     * @param system system being scanned
     * @param filename (already stripped of anything pathing)
     * @param path
     * @return
     */
    void createGameWithoutGamelistEntry(String system, Path path, Document doc) {
        Game game = new Game(
            system: system,
            path: path.fileName.toString(),
            size: Files.size(path)
        )
        indexerDataService.saveGame(game, doc)
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
        log.info("Scanning folder ${system} matching files")
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
