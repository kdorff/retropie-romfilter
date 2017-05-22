package retropie.romfilter

import grails.web.mapping.LinkGenerator
import retropie.romfilter.indexed.Game

class GameService {

    // Inject link generator
    LinkGenerator grailsLinkGenerator

    String namePathComparison(Game game) {
        String comparison
        // path is always in the index.
        if (!game.name) {
            comparison = "${game.path}"
        }
        else {
            comparison = "${game.name}<br/>${game.path}"
        }
        String deleteUrl = grailsLinkGenerator.link([
            controller: 'games',
            action: 'delete',
            params: [hash: game.hash],
        ])
        return "${comparison}<p>&nbsp;</p><p><button class='deleteRom' data-delete-url='${deleteUrl}' type='button'>Delete ROM</button></p>"
    }
}
