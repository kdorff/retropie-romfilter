package retropie.romfilter

import grails.core.GrailsApplication
import org.apache.commons.io.FilenameUtils
import retropie.romfilter.feed.RomsDataFeed
import retropie.romfilter.indexed.GamelistEntry
import retropie.romfilter.indexed.RomEntry
import retropie.romfilter.indexed.SystemEntry

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SystemController {

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
     * List all systems that have roms.
     *
     * @return
     */
    def listSystems() {
        log.info("Listing systems")
        List<SystemEntry> systems = indexerDataService.getAllSystemsEntries()
        Map<String, Integer> systemToNumRoms = [:]
        systems.each { SystemEntry systemEntry ->
            systemToNumRoms[systemEntry.system] = indexerDataService.getRomEntryCountForSystem(systemEntry.system)
        }
        return [
            systems: systems,
            systemToNumRoms: systemToNumRoms,
        ]
    }

    /**
     * List all all roms for a single system.
     *
     * @param system
     * @return
     */
    def listRomsForSystem(String system) {
        List<RomEntry> romEntryList = indexerDataService.getRomEntriesForSystem(system)
        String romsDataFeedUrl = g.createLink(mapping: 'romsDataFeed', params: [system: system])
        return [
            system: system,
            roms: romEntryList,
            romsDataFeed: romsDataFeedUrl
        ]
    }

    /**
     * The roms data feed for datatables server-side processing.
     * @param system
     * @return
     */
    def romsDataFeed(String system) {
        println "romsDataFeed for ${system}"
        println "params ${params}"
        List<RomEntry> roms = indexerDataService.getRomEntriesForSystem(system)
        RomsDataFeed romsDataFeed = new RomsDataFeed(
            draw: params.int('draw') ?: 0,
            roms: roms,
            recordsTotal: roms.size(),
            recordsFiltered: roms.size(),
        )
        respond romsDataFeed
    }

    /**
     * Show details for a single rom.
     *
     * @param system
     * @param hash
     * @return
     */
    def showRomForSystem(String system, int hash) {
        GamelistEntry gamelistEntry = indexerDataService.getGamelistEntryForSystemAndHash(system, hash)
        if (gamelistEntry) {
            return [
                system       : system,
                gamelistEntry: gamelistEntry,
            ]
        }
        else {
            log.error("No game found for ${system} ${hash}")
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
    def deleteRomForSystem(String system, int hash) {
        RomEntry toDeleteEntry = indexerDataService.getRomEntryForSystemAndHash(system, hash)
        if (!toDeleteEntry) {
            log.error("ROM for RomEntry.id ${id} not found in database")
            response.status = 404
        }
        else {
            Path toDeletePath = Paths.get(
                configService.getRomsPathForSystem(system),
                toDeleteEntry.path)
            if (!Files.exists(toDeletePath)) {
                log.error("ROM not found in on disc ${toDeletePath}")
                toDeleteEntry.delete(flush: true)
                log.error("Deleted missing from from database")
                response.status = 404
            } else {
                // Delete the file (move it to trash)
                String trashPathStr = configService.getTrashPathForSystem(system)
                Path trashDestinationPath = Paths.get(trashPathStr, toDeletePath.fileName.toString())

                try {
                    if (Files.move(toDeletePath, trashDestinationPath)) {

                        indexerIndexingService.deleteRomEntry(toDeleteEntry)
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
    def showRomImageForSystem(String system, int hash) {
        GamelistEntry game = indexerDataService.getGamelistEntryForSystemAndHash(system, hash)
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
            log.error("No game (for image) found for ${system} ${hash}")
            response.status = 404
        }
    }
}
