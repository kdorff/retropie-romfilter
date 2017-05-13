package retropie.romfilter

import org.apache.commons.io.FilenameUtils
import grails.core.GrailsApplication
import groovy.util.slurpersupport.GPathResult

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.stream.Collectors

class RomfilterDataService {

    /**
     * GrailsApplication (auto-injected).
     */
    GrailsApplication grailsApplication

    /**
     * At startup or on demand. Scan all system and gamelist data,
     * TODO: Add on-demand.
     */
    void scanAll() {
        long start = System.currentTimeMillis()
        log.info("Beginning startup scan.")
        scanSystems()
        scanGamelists()

        log.info("Found ${SystemEntry.count()} systems")
        log.info("Found ${GamelistEntry.count()} games")
        log.info("Startup scan complete. Took ${System.currentTimeMillis() - start}ms")
    }

    /**
     * Scan all systems to determine how many roms exact for each system.
     */
    void scanSystems() {
        long start = System.currentTimeMillis()
        log.info("Looking for systems with roms")
        Path systemsFolderPath = Paths.get(romsPath)
        foldersContainedWithin(systemsFolderPath).each { Path systemPath ->
            SystemEntry systemEntry = new SystemEntry(
                name: FilenameUtils.getName(systemPath.toString()),
                romCount: filesContainedWithin(systemPath).size(),
            )
            if (systemEntry.romCount > 0) {
                systemEntry.save(flush: true, failOnError: true)
            }
        }
        log.info("System scan complete. Took ${System.currentTimeMillis() - start}ms")
    }

    /**
     * Startup or on demand. Scan all gamelist.xml files, up to one per system.
     */
    void scanGamelists() {
        long start = System.currentTimeMillis()
        log.info("Finding gamelists.xml files")
        Path gamelistsPath = Paths.get(gamelistsPath)
        foldersContainedWithin(gamelistsPath).each { Path gamelistFolderPath ->
            scanGamelist(gamelistFolderPath.getFileName().toString(),
                Paths.get(gamelistFolderPath.toString(), 'gamelist.xml'))
        }
        log.info("All gamelist.xml files found. Took ${System.currentTimeMillis() - start}ms")
    }

    /**
     * Scan a single gamelist.xml file for a specific system.
     */
    void scanGamelist(String system, Path gamelistPath) {
        long start = System.currentTimeMillis()
        log.info("Parsing gamelist.xml ${gamelistPath}")
        if (Files.exists(gamelistPath) &&
            Files.isReadable(gamelistPath) &&
            Files.isRegularFile(gamelistPath)) {
            parseGamelistFromXml(system, new String(Files.readAllBytes(gamelistPath)))
        }
        else {
            log.warn("... Gamelist not found")
        }

        log.info("gamelist.xml parsing complete. Took ${System.currentTimeMillis() - start}ms")
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
        String imagesPrefix = getImagesPrefix()

        // Start parsing the XML
        GPathResult gamelist = new XmlSlurper().parseText(gamelistXml)
        gamelist.children().each { game ->
            GamelistEntry entry = new GamelistEntry(
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
                releasedate: game.releasedate?.toString() ?: "",
                rating: (game.players?.toString() ?: "0.0") as double,
                playcount: (game.players?.toString() ?: "0") as int,
                lastplayed: game.releasedate?.toString() ?: "",
                system: system,
            )

            // Transform path
            if (entry.path.startsWith('./')) {
                entry.path -= './'
            }

            // Transform image
            if (entry.image?.startsWith(imagesPrefix)) {
                entry.image -= imagesPrefix
                entry.image = "${imagesPath}/${entry.image}".toString()
            }
            else {
                // Image in unknown location, remove it
                entry.image = ''
            }

            // Transform thumbnail
            if (entry.thumbnail?.startsWith(imagesPrefix)) {
                entry.thumbnail -= imagesPrefix
                entry.thumbnail = "${imagesPath}/${entry.thumbnail}".toString()
            }
            else {
                // Thumbnail in unknown location, remove it
                entry.thumbnail = ''
            }

            entry.save(flush: true, failOnError: true)
        }
    }

    /**
     * For a given system, search the filesystem and return all of the
     * a map of the rom filename (without path) to that files's path.
     *
     * @param system system to list roms for
     * @return
     */
    Map<String, Path> listRomsForSystem(String system) {
        Map<String, Path> result = [:]
        Path systemFolderPath = Paths.get(getRomsPathForSystem(system))
        filesContainedWithin(systemFolderPath).each { romPath ->
            // Get rid of the path, just keep filename including ext
            String romName = FilenameUtils.getName(romPath.toString())
            result[romName] = romPath
        }
        return result
    }

    /**
     * For a given system return a map of game.path to GameEntry.
     * This returns gamedata.xml data for a specific system.
     *
     * @param system the system to obtain gamelist.xml data for.
     * @return
     */
    Map<String, GamelistEntry> filenameToGamelistEntryForSystem(String system) {
        Map<String, GamelistEntry> result = [:]
        GamelistEntry.findAllBySystem(system).each { GamelistEntry game ->
            result[game.path] = game
        }
        return result
    }

    /**
     * Return all of the FOLDERS in the specified folder (1 level deep).
     *
     * @param folder folder to search
     * @return list of Path for folders
     */
    List<Path> foldersContainedWithin(Path folder) {
        List<Path> result = []
        try {
            return Files.find(folder, 1, { path, attrs ->
                return attrs.isDirectory() && !Files.isHidden(path)
            }).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Exception scanning systems folder", e)
        }
        return result
    }

    /**
     * Return all of the FILES in the specified folder (ignore subfolders).
     *
     * @param folder folder to search
     * @return list of Path for files
     */
    List<Path> filesContainedWithin(Path folder) {
        long start = System.currentTimeMillis()
        log.info("Scanning roms folder ${folder}")
        List<Path> result = []
        try {
            return Files.find(folder, 1, { path, attrs ->
                return attrs.isRegularFile() && !Files.isHidden(path)
            }).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Exception scanning system folder for roms", e)
        }
        finally {
            log.info("Scanning of roms folder complete. Took ${System.currentTimeMillis() - start}ms")
        }
    }

    /**
     * The gamelists path. This references a folder that contains one folder per system. Each of these
     * folders contains a file 'gamelist.xml'.
     *
     * @return
     */
    String getGamelistsPath() {
        return grailsApplication.config.retropie.emulationStation.gamelistsPath
    }

    /**
     * The gamelists path for a specific system. This references a folder that might contain a gamelist.xml
     * for the specified system.
     *
     * @param system
     * @return
     */
    String getGamelistPathForSystem(String system) {
        return "${gamelistsPath}/${system}/gamelist.xml"
    }

    /**
     * The rom path. This references a folder that contains one folder per system. Each of these
     * folders contains roms for that system.
     *
     * @param system
     * @return
     */
    String getRomsPath() {
        return grailsApplication.config.retropie.emulationStation.romsPath
    }

    /**
     * The roms path for a specific system.
     *
     * @param system
     * @return
     */
    String getRomsPathForSystem(String system) {
        return "${romsPath}/${system}"
    }

    /**
     * Return the images prefix. When scanned, this is placed before the system and filename. If
     * the whole image path is "~/.emulationstation/downloads/atari2600/blah.jpg" the prefix is
     * probably "~/.emulationstation/downloads/".
     *
     * @return
     */
    String getImagesPrefix() {
        return grailsApplication.config.retropie.emulationStation.imagesPrefix
    }

    /**
     * Return the images path.
     *
     * @return
     */
    String getImagesPath() {
        return grailsApplication.config.retropie.emulationStation.imagesPath
    }

    /**
     * Return the images path for a given system
     *
     * @param system
     * @return
     */
    String getImagesPathForSystem(String system) {
        return "${imagesPath}/${system}"
    }

    /**
     * Return the trash path.
     *
     * @return
     */
    String getTrashPath() {
        String trashPathStr = grailsApplication.config.retropie.romfilter.trashPath
        Path trashPath = Paths.get(trashPathStr)
        if (!Files.exists(trashPath)) {
            Files.createDirectories(trashPath)
        }
        return trashPathStr
    }

    /**
     * Return the trash path for a given system
     *
     * @param system
     * @return
     */
    String getTrashPathForSystem(String system) {
        String systemTrashPathStr = "${trashPath}/${system}"
        Path systemTrashPath = Paths.get(systemTrashPathStr)
        if (!Files.exists(systemTrashPath)) {
            Files.createDirectories(systemTrashPath)
        }
        return systemTrashPathStr
    }

    /**
     * Return an md5 has for a given string.
     *
     * @param s string to hash
     * @return md5 has of string
     */
    String hash(String s) {
        MessageDigest.getInstance("MD5").digest(s.bytes).encodeHex().toString()
    }
}
