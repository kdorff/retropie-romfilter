package retropie.romfilter

import org.apache.commons.io.FilenameUtils
import grails.core.GrailsApplication
import groovy.util.slurpersupport.GPathResult

import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest

class RomfilterDataService {

    /**
     * GrailsApplication (auto-injected).
     */
    GrailsApplication grailsApplication

    Map<String, Map<String, GamelistEntry>> systemToNameGamelistCache = [:]

    synchronized Map<String, GamelistEntry> gamelistForSystem(String system) {
        Map<String, GamelistEntry> gamelist = systemToNameGamelistCache["${system}.nameToEntry"]
        if (!gamelist) {
            Path gamelistPath = Paths.get(getGamelistPathForSystem(system))
            if (Files.exists(gamelistPath) &&
                Files.isReadable(gamelistPath) &&
                Files.isRegularFile(gamelistPath)) {
                gamelist = parseGamelistFromXml(system, Files.readAllLines(gamelistPath).join('\n'))
                systemToNameGamelistCache["${system}.nameToEntry"] = gamelist
            }
            else {
                gamelist = [:]
            }
        }
        return gamelist
    }

    GamelistEntry gamelistEntryForId(String system, String id) {
        Map.Entry<String, GamelistEntry> entry = gamelistForSystem(system).find {
            GamelistEntry game = it.value
            return (game.id == id && game.system == system)
        }
        return entry.value
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
    Map<String, GamelistEntry> parseGamelistFromXml(String system, String gamelistXml) {
        String imagesPrefix = imagesPrefix

        // The result
        Map<String, GamelistEntry> gameslistMap = [:]
        // Start parsing the XML
        GPathResult gamelist = new XmlSlurper().parseText(gamelistXml)
        gamelist.children().each { game ->
            GamelistEntry entry = new GamelistEntry(
                id: game.'@id'.toString(),
                source: game.'@source'.toString(),
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

            gameslistMap[entry.path] = entry
        }

        return gameslistMap
    }

    List<SystemEntry> listSystems() {
        List<SystemEntry> result = []
        Path systemsFolderPath = Paths.get(romsPath)
        systemsInSystemsFolder(systemsFolderPath).each { Path systemPath ->
            SystemEntry systemEntry = new SystemEntry(
                name: FilenameUtils.getName(systemPath.toString()),
                romCount: romsInSystemFolder(systemPath).size(),
            )
            result << systemEntry
        }
        return result
    }

    List<Path> systemsInSystemsFolder(Path systemsFolderPath) {
        List<Path> result = []
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(systemsFolderPath)
            for (Path systemCandidatePath : stream) {
                if (isSystemFolder(systemCandidatePath)) {
                    result << systemCandidatePath
                }
            }
        } catch (IOException e) {
            log.error("Exception scanning systems folder", e)
        }
        return result
    }

    boolean isSystemFolder(Path systemCandidatePath) {
        return Files.exists(systemCandidatePath) &&
            Files.isReadable(systemCandidatePath) &&
            Files.isDirectory(systemCandidatePath) &&
            !Files.isHidden(systemCandidatePath) &&
            !isDirectoryEmpty(systemCandidatePath)
    }

    Map<String, Path> listRomsForSystem(String system) {
        Map<String, Path> result = [:]
        Path systemFolderPath = Paths.get(getRomsPathForSystem(system))
        romsInSystemFolder(systemFolderPath).each { romPath ->
            // Get rid of the path, just keep filename including ext
            String romName = FilenameUtils.getName(romPath.toString())
            result[romName] = romPath
        }
        return result
    }

    List<Path> romsInSystemFolder(Path systemFolderPath) {
        List<Path> result = []
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(systemFolderPath)
            for (Path romCandidatePath : stream) {
                if (isFileRom(romCandidatePath)) {
                    result << romCandidatePath
                }
            }
        } catch (IOException e) {
            log.error("Exception scanning system folder for roms", e)
        }
        return result
    }

    boolean isFileRom(Path romCandidatePath) {
        return Files.exists(romCandidatePath) &&
                Files.isReadable(romCandidatePath) &&
                Files.isRegularFile(romCandidatePath) &&
                !Files.isHidden(romCandidatePath)
    }

    String getGamelistsPath() {
        return grailsApplication.config.retropie.emulationStation.gamelistsPath
    }

    String getGamelistPathForSystem(String system) {
        return "${gamelistsPath}/${system}/gamelist.xml"
    }

    String getRomsPath() {
        return grailsApplication.config.retropie.emulationStation.romsPath
    }

    String getRomsPathForSystem(String system) {
        return "${romsPath}/${system}"
    }

    String getImagesPrefix() {
        return grailsApplication.config.retropie.emulationStation.imagesPrefix
    }

    String getImagesPath() {
        return grailsApplication.config.retropie.emulationStation.imagesPath
    }

    String getTrashPath() {
        String trashPathStr = grailsApplication.config.retropie.romfilter.trashPath
        Path trashPath = Paths.get(trashPathStr)
        if (!Files.exists(trashPath)) {
            Files.createDirectories(trashPath)
        }
        return trashPathStr
    }

    String getTrashPathForSystem(String system) {
        String systemTrashPathStr = "${trashPath}/${system}"
        Path systemTrashPath = Paths.get(systemTrashPathStr)
        if (!Files.exists(systemTrashPath)) {
            Files.createDirectories(systemTrashPath)
        }
        return systemTrashPathStr
    }

    String getImagesPathForSystem(String system) {
        return "${imagesPath}/${system}"
    }

    String hash(String s) {
        MessageDigest.getInstance("MD5").digest(s.bytes).encodeHex().toString()
    }

    boolean isDirectoryEmpty(Path directory) throws IOException {
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory);
        boolean empty = !directoryStream.iterator().hasNext();
        return empty
    }
}
