package retropie.romfilter

import grails.core.GrailsApplication
import org.apache.commons.io.FilenameUtils
import retropie.romfilter.feed.GamesDataFeed
import retropie.romfilter.feed.datatables.DatatablesRequest
import retropie.romfilter.indexed.Game

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class GamesController {

    /**
     * TODO: action: Fixed delete?
     * TODO: Highlight?
     * TODO: Card View?
     * TODO: Move to a consolidated view of systems instead of view of one system?
     * TODO: More unit tests
     * TODO: More integration tests
     * TODO: Use endless scrolling datatables to load and filter the data on demand
     * TODO: Search box should support field search.
     * TODO: Show name/filename similarity?
     * TODO: Filter to show dissimilar name/filenames
     * TODO: Filter those without gamelistEntry
     * TODO: Some common filters? (Unl), (World) (Beta) (Proto) (countries), etc.
     * TODO: Filtering should set the URL? And if you go there, apply the filter.
     * TODO: Add remaining file extensions to the various systems to config
     * TODO: Make scanning quarts jobs
     * TODO: On demand complete re-scanning (delete and rebuild)
     * TODO: Ordering in lucene based on datatables request
     * TODO: Show images again.
     * TODO: Test delete game.
     *
     * DONE: Indexing, move from database to Lucene.
     * DONE: Why is the gamelist.name field missing?
     * DONE: Add an all field that isn't stored for multi-field search for all the cards
     * DONE: Any fields to not tokenize? I think even filenames should be tokenized
     * DONE: How do I use config value or service method value in resources.groovy to specify paths for indexes.
     * DONE: Data access methods so things outside the data access service don't pass raw queries.
     * DONE: Store the hash of the gamelist instead of the boolean?
     * DONE: Integration tests.
     * DONE: Tests for parsing gamelist.xml file.
     * DONE: More reliable, non-changing hash method. Explicit field list.
     * DONE: Re-use Lucene Document object to whatever extent possible to improve indexing speed.
     */

    /**
     * GrailsApplication (auto-injected).
     */
    GrailsApplication grailsApplication

    /**
     * IndexerDataService (auto-injected).
     */
    IndexerDataService indexerDataService

    /**
     * RomfilterDataService (auto-injected).
     */
    IndexerIndexingService indexerIndexingService

    /**
     * ConfigService (auto-injected).
     */
    ConfigService configService

    /**
     * Valid rom image file extensions and their associated mime type.
     */
    final static Map<String, String> IMAGE_EXT_TO_MIME = [
        'png':  'image/png',
        'jpg':  'image/jpeg',
        'jpeg': 'image/jpeg',
        'gif':  'image/gif',
    ].asImmutable()

    /**
     * List all all roms. AKA listRoms.
     *
     * @param system
     * @return
     */
    def index() {
        String romsDataFeedUrl = g.createLink(mapping: 'gamesDataFeed', params: [:])
        render view: 'listGames', model: [romsDataFeed: romsDataFeedUrl ]
    }

    /**
     * The roms data feed for datatables server-side processing.
     * @param system
     * @return
     */
    def gamesDataFeed() {
        log.info("gamesDataFeed")
        log.info("params ${params}")
        DatatablesRequest datatablesRequest = new DatatablesRequest(params)
        log.error("request ${datatablesRequest}")
        GamesDataFeed gamesDataFeed = indexerDataService.getGameDataFeedForRequest(datatablesRequest)
        respond gamesDataFeed
    }

    /**
     * Show details for a single rom.
     *
     * @param system
     * @param hash
     * @return
     */
    def showGame(int hash) {
        Game gamelistEntry = indexerDataService.getGameForHash(hash)
        if (gamelistEntry) {
            return [
                gamelistEntry: gamelistEntry,
            ]
        }
        else {
            log.error("No game found for ${hash}")
            response.status = 404
        }
    }

    /**
     * Try to delete a single rom (move it to trash).
     *
     * @path system
     * @path hash
     * @return
     */
    def deleteGame(int hash) {
        Game toDeleteEntry = indexerDataService.getGameForHash(hash)
        if (!toDeleteEntry) {
            log.error("GameEntry.hash ${hash} not found in index")
            response.status = 404
        }
        else {
            Path toDeletePath = Paths.get(configService.getRomsPath(), toDeleteEntry.system, toDeleteEntry.path)
            if (!Files.exists(toDeletePath)) {
                log.error("ROM not found in on disc ${toDeletePath}")
                indexerIndexingService.deleteGame(toDeleteEntry)
                log.error("Deleted missing from from database")
                response.status = 404
            } else {
                // Delete the file (move it to trash)
                String trashPathStr = configService.trashPath
                Path trashDestinationPath = Paths.get(trashPathStr, toDeleteEntry.system, toDeletePath.fileName.toString())

                try {
                    if (Files.move(toDeletePath, trashDestinationPath)) {
                        indexerIndexingService.deleteGame(toDeleteEntry)
                        log.trace("Moved ${toDeletePath} to ${trashDestinationPath}")
                        response.status = 200
                    } else {
                        log.error("Unable to move ${toDeletePath} to ${trashDestinationPath}. Note that this may not work across filesystems, etc.")
                        response.status = 500
                    }
                }
                catch (IOException e) {
                    log.error("Exception moving ${toDeletePath} to ${trashDestinationPath}. Note that may will not work across filesystems, etc.", e)
                    response.status = 500
                }
            }
        }
    }

    /**
     *
     * Output the rom image for the specified system, hash.
     *
     * @param system system name (atari2600)
     * @param hash
     * @return
     */
    def showGameImage(int hash) {
        Game game = indexerDataService.getGameForHash(hash)
        if (game) {
            String fileType = FilenameUtils.getExtension(game.image).toLowerCase()
            String mimeType = IMAGE_EXT_TO_MIME[fileType]
            if (!mimeType) {
                log.error("Invalid file extension for image ${fileType}")
                response.status = 404
            }
            else {
                try {
                    render contentType: mimeType, file: game.image
                }
                catch (Exception e) {
                    log.error ("Error sending image ${game.image}", e)
                    response.status = 500
                }
            }
        }
        else {
            log.error("No game (for image) found for ${hash}")
            response.status = 404
        }
    }
}
