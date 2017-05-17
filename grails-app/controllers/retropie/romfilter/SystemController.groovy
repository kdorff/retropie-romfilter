package retropie.romfilter

import grails.core.GrailsApplication
import org.apache.commons.io.FilenameUtils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SystemController {

    /**
     * TODO: Indexing:
     * TODO: Why is the gamelist.name field missing?
     * TODO: Add an all field that isn't stored for multi-field search for all the cards
     * TODO: Any fields to not tokenize? I think even filenames should be tokenized
     * TODO: action: Fixed image?
     * TODO: action: Fixed rom details?
     * TODO: action: Fixed delete?
     * TODO: How do I use config value or service method value in resources.groovy to specify paths for indexes.
     * TODO: Multiple indexes in a directory?
     * TODO: Highlight?
     * TODO: Card View?
     * TODO: Is there a native date format for Lucene for release date and last played?
     * TODO: Data access methods so things outside the data access service don't pass raw queries.
     * TODO: Move to a consolidated view of systems instead of view of one system?
     * TODO: Store the hash of the gamelist instead of the boolean?
     * TODO: Integration tests.
     * TODO: Move to Luceue 5. Lucene 6 IntPoint isn't integrated into querying?
     *
     * TODO: Use endless scrolling datatables to load and filter the data on demand. Much more responsive.
     * TODO: Search box should support field search.
     * TODO: Show name/filename similarity?
     * TODO: Filter to show dissimilar name/filenames
     * TODO: One search for all systems, eliminating the system screen?
     * TODO: Filter those without gamelistEntry
     * TODO: Some common filters? (Unl), (World) (Beta) (Proto) (countries), etc.
     * TODO: A "delete all visible" button?
     * TODO: Filtering should set the URL? And if you go there, apply the filter.
     * TODO: Add remaining file extensions to the various systems to config
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
        println "Listing systems"
        List<SystemEntry> systems = indexerDataService.systemEntries()
        Map<String, Integer> systemToNumRoms = [:]
        systems.each { SystemEntry systemEntry ->
            systemToNumRoms[systemEntry.name] = indexerDataService.getRomEntryCountForSystem(systemEntry.name)
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
        List<RomEntry> romEntryList = indexerDataService.romEntriesForSystem(system)
        return [
            system: system,
            roms: romEntryList,
        ]
    }

    /**
     * Show details for a single rom.
     *
     * @param system
     * @param hash
     * @return
     */
    def showRomForSystem(String system, int hash) {
        GamelistEntry gamelistEntry = indexerDataService.gamelistEntryForSystemAndHash(system, hash)
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
        RomEntry toDeleteEntry = indexerDataService.romEntryForSystemAndHash(system, hash)
        if (!toDeleteEntry) {
            log.error("ROM for RomEntry.id ${id} not found in database")
            response.status = 404
        }
        else {
            Path toDeletePath = Paths.get(
                configService.getRomsPathForSystem(system),
                toDeleteEntry.filename)
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
        GamelistEntry game = indexerDataService.gamelistEntryForSystemAndHash(system, hash)
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
