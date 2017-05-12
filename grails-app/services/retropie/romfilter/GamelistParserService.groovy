package retropie.romfilter

import org.apache.commons.io.FilenameUtils
import grails.core.GrailsApplication
import groovy.util.slurpersupport.GPathResult

class GamelistParserService {

    /**
     * GrailsApplication (auto-injected).
     */
    GrailsApplication grailsApplication

    Map<String, GamelistEntry> parseGamelistForSystem(String system) {
        File gamelistFile = new File(getGamelistPathForSystem(system))
        println gamelistFile
        if (gamelistFile.exists() && 
                gamelistFile.canRead() && 
                gamelistFile.isFile()) {
            return parseGamelistFromXml(gamelistFile.text)
        }
        else {
            return [:]
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
     * @param gamelistXml
     * @return
     */
    Map<String, GamelistEntry> parseGamelistFromXml(String gamelistXml) {
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
            )

            // Transform path
            if (entry.path.startsWith('./')) {
                entry.path -= './'
            }

            // Transform image
            if (entry.image.startsWith(imagesPrefix)) {
                entry.image -= imagesPrefix
            }
            else {
                // Image in unknown location, remove it
                entry.image = ''
            }

            // Transform thumbnail
            if (entry.thumbnail.startsWith(imagesPrefix)) {
                entry.thumbnail -= imagesPrefix
            }
            else {
                // Thumbnail in unknown location, remove it
                entry.thumbnail = ''
            }

            gameslistMap[entry.path] = entry
        }

        return gameslistMap
    }

    Map<String, File> listSystems() {
        Map<String, File> result = [:]
        def romDirFile = new File("${romsPath}");
        romDirFile.listFiles().each { File systemCandidateFile ->
            if (keepCandidateSystem(systemCandidateFile)) {    
                String systemName = FilenameUtils.getName(systemCandidateFile.toString())
                result[systemName] = systemCandidateFile
            }
        }
        retirn result
    }

    private boolean keepCandidateSystem(File systemCandidateFile) {
        return (systemCandidateFile.canRead() && 
                    systemCandidateFile.isDirectory() && 
                    !systemCandidateFile.isHidden())
    }

    Map<String, File> listRomsForSystem(String system) {
        Map<String, File> result = [:]
        def romsDirFile = new File("${getRomsPathForSystem(system)}");
        romsDirFile.listFiles().each { File romCandidateFile ->
            if (keepCandidateRom(romCandidateFile)) {    
                String romName = FilenameUtils.getName(romCandidateFile.toString())
                result[romName] = romCandidateFile
            }
        }
        return result
    }

    private boolean keepCandidateRom(File romCandidateFile) {
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
