package retropie.romfilter

import groovy.util.slurpersupport.GPathResult
import org.apache.log4j.Logger
import org.apache.lucene.document.Document
import retropie.romfilter.indexed.Game

import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.regex.Pattern

class ScanSystemJob {
    def description = 'Scan roms and gamelists for single system'
    def group = "romfilter-jobs"
    def concurrent = true
    def sessionRequired = false

    /**
     * No triggers. This job is called only manually.
     */
    static triggers = {}

    /**
     * Pattern to match int / long type numbers within a string.
     */
    final static Pattern INT_STRING_PATTERN = ~/(\d+)/

    /**
     * Pattern to match int / long type numbers within a string.
     */
    final static Pattern FLOAT_STRING_PATTERN = ~/(\d+\.\d+)|(\d+)/

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
     *
     * @param context
     */
    Random random = new Random()

    void execute(context) {
        long start = System.currentTimeMillis()
        String system = context.mergedJobDataMap.get('system')
        try {
            if (!system) {
                log.error("ScanSystemJob called but no system parameter provided")
                return
            }

            Path gamelistFolderPath = Paths.get(configService.gamelistsPath, system)
            log.info("Starting scanning system ${system}")
            log.info("Scanning gamelist.xml in path ${gamelistFolderPath}")

            // We may have added it as a skip system after we indexed. Remove from the index.
            indexerDataService.deleteAllForSystem(system)

            if (system in configService.systemsToSkip) {
                log.info("Skipping system ${system}")
            } else {
                Path romsPath = Paths.get(configService.romsPath, system)
                gamelistFolderPath
                // Collect the list of roms for the system in question
                Map<String, Path> unmatchedRoms = filesContainedWithin(
                    romsPath, configService.getRomGlobForSystem(system)).collectEntries { Path romPath ->
                    String key = romPath.fileName.toString()
                    return [(key): romPath]
                }

                // Parse the gamelist.xml for the system in question
                // indexing only entries for which we have roms. As a gamelist.xml entriy
                // is found for the roms, it will be removed from unmatchedRoms.
                Document doc = new Document()
                scanGamelist(
                    gamelistFolderPath.getFileName().toString(),
                    Paths.get(gamelistFolderPath.toString(), 'gamelist.xml'),
                    doc,
                    unmatchedRoms)

                // Index roms which don't have a gamelist entry with minimum details.
                unmatchedRoms.each { String filenameUnused, Path path ->
                    createGameWithoutGamelistEntry(system, path, doc)
                }
            }
        }
        finally {
            log.info("Scanning system ${system} took ${System.currentTimeMillis() - start}ms")
        }
    }

    /**
     * Scan a single gamelist.xml file for a specific system.
     */
    void scanGamelist(String system, Path gamelistPath, Document doc, Map<String, Path> unmatchedRoms) {
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
                parseGamelistFromXml(
                    system,
                    new String(Files.readAllBytes(gamelistPath)),
                    doc,
                    unmatchedRoms
                )
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
                players: intString(gamelistGame.players.toString(), '1') as long,
                region: gamelistGame.region?.toString() ?: "",
                romtype: gamelistGame.romtype?.toString() ?: "",
                releasedate: convertDateTimeToLong(gamelistGame.releasedate?.toString() ?: ""),
                rating: Math.round((floatString(gamelistGame.rating?.toString(), "0.0") as double) * 100) as long,
                playcount: intString(gamelistGame.playcount?.toString(), "0") as long,
                lastplayed: convertDateTimeToLong(gamelistGame.lastplayed?.toString() ?: ""),
                metadata: true,
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
                game.size = Files.size(Paths.get(configService.romsPath, game.system, game.path))
                game.hash = indexerDataService.generateHash(game)
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
            size: Files.size(path),
            // Populate all numeric fields
            players: 1,
            releasedate: 0,
            rating: 0,
            playcount: 0,
            lastplayed: 0,
        )

        // Transform path
        if (game.path.startsWith('./')) {
            game.path -= './'
        }
        game.hash = indexerDataService.generateHash(game)
        indexerDataService.saveGame(game, doc)
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


    /**
     * Return the (first) integer portion of a String or the default value if no integer portion was found
     * as a String. "cat15fish622more" would return "15". "catfishmore" would return defultVal.
     * Note this should be suitable for long, also. null and empty string would return default, as well.
     *
     * @param val
     * @return
     */
    String intString(String val, String defaultVal = null) {
        return val?.find(INT_STRING_PATTERN) ?: defaultVal
    }

    /**
     * Return the (first) float portion of a String or the default value if no float portion was found
     * as a String. "cat15fish622more" would return "15", "mouse15.321runt" would return "15.321", and
     * "catfishmore" would return defultVal. null and empty string would return default, as well.
     * Note this should be suitable for double, also.
     *
     * @param val
     * @return
     */
    String floatString(String val, String defaultVal = null) {
        return val?.find(FLOAT_STRING_PATTERN) ?: defaultVal
    }
}
