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
     * High:
     * TODO: Highlight with Lucene. The Datatables highligher won't cut it.
     * TODO: Make scanning quartz jobs
     * TODO: On demand complete re-scanning (delete and rebuild)
     * TODO: Restore show game link
     * TODO: Add remaining file extensions to the various systems to config
     * TODO: Fancify name/path comparison
     * TODO: Make / go to /games/browse or to /games which already goes to /games/browse
     *
     * Lower:
     * TODO: Change table rows to be ALL top justified
     * TODO: More search help
     * TODO: How does recalbox webapp launch games? (the django code I worked on)
     * TODO: Filter to show dissimilar name/filenames
     * TODO: Card View?
     * TODO: More unit tests
     * TODO: More integration tests
     * TODO: Some common filters? (Unl), (World) (Beta) (Proto) (countries), etc.
     * TODO: Filtering should set the URL? And if you go there, apply the filter.
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
     * DONE: Move to a consolidated view of systems instead of view of one system?
     * DONE: Use endless scrolling datatables to load and filter the data on demand
     * DONE: Search box should support field search.
     * DONE: Index correctly for ordering all fields except description.
     * DONE: Move to Bootstrap css for the datatables
     * DONE: ALL fields, start most hidden
     * DONE: Additional field that is name over filename with visual comparison.
     * DONE: Make game search input test box wider
     * DONE: new field named "metadata" that contains "true" or "false" to find roms with/without gamelist entries
     * DONE: Ordering in lucene based on datatables request
     * DONE: What happened to the application header?
     * DONE: Column picker (and list of fields) (also shows if searchable and orderable).
     * DONE: When sorting, always put missing data at bottom.
     * DONE: Delete button in name/path comparison field. Working. Some filenames cause it problems ('[', ']' ?). Good enough.
     */

    /**
     * Methods where we enforce specific http verbs.
     */
    static allowedMethods = [
        feed: 'POST',
        delete: 'DELETE'
    ]

    /**
     * GrailsApplication (auto-injected).
     */
    GrailsApplication grailsApplication

    /**
     * IndexerDataService (auto-injected).
     */
    IndexerDataService indexerDataService

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

    def index() {
        redirect action:'browse'
    }

        /**
     * List all all roms. AKA listRoms.
     *
     * @param system
     * @return
     */
    def browse() {
    }

    /**
     * The roms data feed for datatables server-side processing.
     * @param system
     * @return
     */
    def feed() {
        log.info("feed")
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
    def show(String hash) {
        Game game = indexerDataService.getGameForHash(hash)
        if (game) {
            return [
                game: game,
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
    def delete(String hash) {
        Game toDeleteEntry = indexerDataService.getGameForHash(hash)
        if (!toDeleteEntry) {
            log.error("GameEntry.hash ${hash} not found in index")
            response.status = 404
        }
        else {
            Path toDeletePath = Paths.get(configService.getRomsPath(), toDeleteEntry.system, toDeleteEntry.path)
            if (!Files.exists(toDeletePath)) {
                log.error("ROM not found in on disc ${toDeletePath}")
                indexerDataService.deleteGame(toDeleteEntry)
                log.error("Deleted missing from from database")
                response.status = 404
            } else {
                // Delete the file (move it to trash)
                String trashPathStr = configService.trashPath
                Path trashDestinationPath = Paths.get(trashPathStr, toDeleteEntry.system, toDeletePath.fileName.toString())

                try {
                    if (Files.move(toDeletePath, trashDestinationPath)) {
                        indexerDataService.deleteGame(toDeleteEntry)
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
    def image(String hash) {
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
