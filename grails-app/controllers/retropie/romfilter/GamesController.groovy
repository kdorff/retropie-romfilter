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
     * TODO: Restore show game link
     * TODO: Add remaining file extensions to the various systems to config
     * TODO: Fancify name/path comparison
     * TODO: Option to rescan a single system
     * TODO: Make jobs.index prettier
     * TODO: add tag support for kid friendly:
     * TODO: 		<favorite>true</favorite>
     * TODO:        <kidgame>true</kidgame>
     * TODO:        <hidden>true</hidden>
     * TODO: / to games/browse doesn't show games/browse. Change to redirect.
     * TODO: Make bulk deletion a job and have the ability to give status of the job?
     * TODO: Show "recent" jobs and that they finished and how long they took?
     * TODO: When scanning all, remove any systems that are in the index but not on disc
     * TODO: Fix log levels. Everything is info. Most should be trace.
     * TODO: Log file location to configuration
     *
     * Lower:
     * TODO: Make / go to /games/browse or to /games which already goes to /games/browse
     * TODO: How can I trigger an app restart of a spring boot app? such as if I re-write the index or ?
     * TODO: Change table rows to be ALL top justified
     * TODO: More search help
     * TODO: How does recalbox webapp launch games? (the django code I worked on)
     * TODO: Filter to show 'too dissimilar' name/filenames
     * TODO: Card View?
     * TODO: More unit tests
     * TODO: More integration tests
     * TODO: Some common filters? (Unl), (World) (Beta) (Proto) (countries), etc.
     * TODO: Filtering should set the URL? And if you go there, apply the filter.
     * TODO: How to stop the index to compress or ?? And restart. With Spring Beans.
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
     * DONE: Bulk deletion. Delete everything that matches a query. BE CAREFUL!
     * DONE: Highlight with Lucene. The Datatables highligher won't cut it.
     * DONE: The quartz jobs now exist and work, one for submitting all systems for scanning, one for scanning a system.
     * DONE: On demand via the /quartz endpoint
     * DONE: Replace quartz-monitor with custom scanning systems and jobs page (allows for rescan of all)
     * WONT: Compresses index or re-write index function. [It compresses at startup, it seems.]
     */

    /**
     * Methods where we enforce specific http verbs.
     */
    static allowedMethods = [
        feed: 'POST',
        delete: 'DELETE'
    ]

    /**
     * Delete response code to text message
     * for bulk deletes.
     */
    Map DELETE_RESPONSES = [
        200: "Deletion successful",
        404: "Index entry or ROM path not found",
        500: "Exception deleting ROM."
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
        long start = System.currentTimeMillis()
        DatatablesRequest datatablesRequest = new DatatablesRequest(params)
        GamesDataFeed gamesDataFeed
        try {
            gamesDataFeed = indexerDataService.getGameDataFeedForRequest(datatablesRequest)
        }
        catch (Exception e) {
            log.error("Error retrieving data. Maybe the index is empty.", e)
            gamesDataFeed = new GamesDataFeed([
                draw: params.draw,
                recordsTotal: 0,
                recordsFiltered: 0,
                error: 'Error retrieving data. Perhaps the index is empty.',
                games: [],
            ])
        }
        log.info("Retrieval of data from index took ${System.currentTimeMillis()-start}ms")
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

                // Make sure system trash folder exists
                Path trashDestinationFolderPath = Paths.get(trashPathStr, toDeleteEntry.system)
                Files.createDirectories(trashDestinationFolderPath)

                // Location with trash to move rom
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

    /**
     * Confirm the choice to delete all roms that match a query.
     *
     * @return
     */
    def deleteAllConfirm() {
        if (!params.query) {
            redirect(actionName: 'browse')
        }
        else {
            return [
                query: params.query
            ]
        }
    }

    /**
     * User Confirmed the choice to delete all roms that match a query.
     * Delete the roms and then provide a response that shows all the
     * roms and the status of the deletion. This isn't ideal
     * because all of the deletes happen THEN the result is shown. Deleting
     * hundreds of roms and be slow. An AJAX delete all of some sort would
     * be better. Or start it and monitor the deletes with AJAX.
     * It should be a background task like scanning.
     *
     * @return
     */
    def deleteAllConfirmed() {
        if (!params.query) {
            redirect(actionName: 'browse')
        }

        List<Game> toDeletes = indexerDataService.getGamesForQuery((String) params.query)

        /**
         * TODO: Consider deleting from the index in bulk and deleleting the files
         * TODO: in bulk. Might not matter and the consistency here might be more important.
         */
        Map deleteResult = [:]
        toDeletes.each { Game toDelete ->
            response.status = 0
            delete(toDelete.hash)
            deleteResult[toDelete.path] = DELETE_RESPONSES[response.status] ?: "Unknown"
        }
        response.status = 200

        return [
            query: params.query,
            deleteResult: deleteResult,
        ]
    }
}
