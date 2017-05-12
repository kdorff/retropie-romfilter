package retropie.romfilter

import org.apache.commons.io.FilenameUtils
import grails.core.GrailsApplication
import groovy.util.slurpersupport.GPathResult

class RomfilterDataService {

    /**
     * GrailsApplication (auto-injected).
     */
    GrailsApplication grailsApplication

    Map<String, Map<String, GamelistEntry>> systemToNameGamelistCache = [:]

    synchronized Map<String, GamelistEntry> gamelistForSystem(String system) {
        Map<String, GamelistEntry> gamelist = systemToNameGamelistCache["${system}.nameToEntry"]

        if (!gamelist) {
            File gamelistFile = new File(getGamelistPathForSystem(system))
            if (gamelistFile.exists() &&
                gamelistFile.canRead() &&
                gamelistFile.isFile()) {
                gamelist = parseGamelistFromXml(system, gamelistFile.text)
                systemToNameGamelistCache["${system}.nameToEntry"] = gamelist
            }
            else {
                gamelist = [:]
            }
        }
        return gamelist
    }

    GamelistEntry gamelistEntryForId(String system, Integer id) {
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
                id: game.'@id'.toString() as int,
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
        def systemsFolderFile = new File(romsPath)
        systemsInSystemsFolder(systemsFolderFile).each { File systemFile ->
            SystemEntry systemEntry = new SystemEntry(
                name: FilenameUtils.getName(systemFile.toString()),
                romCount: romsInSystemFolder(systemFile).size(),
            )
            result << systemEntry
        }
        return result
    }

    List<File> systemsInSystemsFolder(File systemsFolderFile) {
        return systemsFolderFile.listFiles().findAll { File systemCandidateFile ->
            return isSystemFolder(systemCandidateFile)
        }
    }

    boolean isSystemFolder(File systemCandidateFile) {
        return (systemCandidateFile.canRead() &&
                    systemCandidateFile.isDirectory() &&
                    !systemCandidateFile.isHidden())
    }

    Map<String, File> listRomsForSystem(String system) {
        Map<String, File> result = [:]
        File systemFolderFile = new File(getRomsPathForSystem(system))
        romsInSystemFolder(systemFolderFile).each { romFile ->
            // Get rid of the path, just keep filename including ext
            String romName = FilenameUtils.getName(romFile.toString())
            result[romName] = romFile
        }
        return result
    }

    List<File> romsInSystemFolder(File systemFolderFile) {
        return systemFolderFile.listFiles().findAll { File romCandidateFile ->
            return isFileRom(romCandidateFile)
        }
    }

    boolean isFileRom(File romCandidateFile) {
        return (romCandidateFile.canRead() &&
            romCandidateFile.isFile() &&
            !romCandidateFile.isHidden())
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

    String getImagesPathForSystem(String system) {
        return "${imagesPath}/${system}"
    }
}
