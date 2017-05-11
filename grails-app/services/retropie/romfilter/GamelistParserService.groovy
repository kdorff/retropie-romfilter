package retropie.romfilter

import grails.core.GrailsApplication
import groovy.util.slurpersupport.GPathResult

class GamelistParserService {

    /**
     * GrailsApplication (auto-injected).
     */
    GrailsApplication grailsApplication

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
    Map<String, GamelistEntry> parseGamelist(String gamelistXml) {
        String imagesPrefix = getImagesPrefix()

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
                developer: game.developer?.toString() ?: "",
                publisher: game.publisher?.toString() ?: "",
                genre: game.genre?.toString() ?: "",
                players: (game.players?.toString() ?: "1") as int,
                region: game.region?.toString() ?: "",
                romtype: game.romtype?.toString() ?: "",
                releasedate: game.releasedate?.toString() ?: "",
            )
            if (entry.path.startsWith('./')) {
                entry.path -= './'
            }

            if (entry.image.startsWith(imagesPrefix)) {
                entry.image -= imagesPrefix
            }
            else {
                // Unknown location
                entry.image = ''
            }

            gameslistMap[entry.path] = entry
        }

        return gameslistMap
    }

    String getImagesPrefix() {
        return grailsApplication.config.retropie.emulationStation.imagesPrefix
    }

    String getImagesPath() {
        return grailsApplication.config.retropie.emulationStation.imagesPath
    }
}
