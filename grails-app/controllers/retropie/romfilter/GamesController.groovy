package retropie.romfilter

import grails.converters.JSON
import grails.core.GrailsApplication
import org.apache.commons.io.FilenameUtils
import org.apache.lucene.queryparser.classic.QueryParser
import retropie.romfilter.feed.GamesDataFeed
import retropie.romfilter.feed.datatables.DatatablesRequest
import retropie.romfilter.indexed.Game

class GamesController {

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
     * JobSubmissionService (auto-injected).
     */
    JobSubmissionService jobSubmissionService

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
        if (!hash) {
            log.error("Game.hash parameter not found in index")
            response.status = 404
            respond (['error': 'Cannot delete. Hash not provided.'] as JSON)
            return
        }
        String query = /+hash:"${QueryParser.escape(hash)}"/
        jobSubmissionService.submitJob(DeleteRomsForQueryJob, [query: query])
        redirect(controller: 'jobs', action: 'index')
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
            redirect(action: 'browse')
            return
        }

        jobSubmissionService.submitJob(DeleteRomsForQueryJob, [query: params.query])
        redirect(controller: 'jobs', action: 'index')
    }
}
