package retropie.romfilter

import grails.transaction.Transactional
import groovy.util.slurpersupport.GPathResult

@Transactional
class GamelistParserService {

    /**
     * Convert xml gamelist to Map<String, GamelistEntry>
     * @param gamelistXml
     * @return
     */
    Map<String, GamelistEntry> parseGamelist(String gamelistXml) {
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
            gameslistMap[entry.path] = entry
        }

        return gameslistMap
    }
}
