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
     * TODO: Count roms on system list. Or are we going to a consolidated view?
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


    def listSystems() {
        println "Listing systems"
        List<SystemEntry> systems = indexerDataService.systemEntries()
        Map<String, Integer> systemToNumRoms = [:]
        systems.each { SystemEntry systemEntry ->
            systemToNumRoms[systemEntry.name] = 1 // RomEntry.countBySystem(systemEntry.name)
        }
        return [
            systems: systems,
            systemToNumRoms: systemToNumRoms,
        ]
    }

    def listRomsForSystem(String system) {
        List<RomEntry> romEntryList = indexerDataService.romEntriesForSystem(system)
        return [
            system: system,
            roms: romEntryList,
        ]
    }

    def showRomForSystem(String system, String scrapeId) {
        GamelistEntry gamelistEntry = indexerDataService.gamelistEntryForQuery(/system:"${system}" AND scrapeId:"${scrapeId}"/)
        if (gamelistEntry) {
            return [
                system       : system,
                gamelistEntry: gamelistEntry,
            ]
        }
        else {
            log.error("No game found for ${system} ${id}")
            response.status = 404
        }
    }

    def deleteRomForSystem(String system, String hash) {
        RomEntry toDeleteEntry = indexerDataService.romEntryForQuery(/system:"${system}" AND hash:"${hash}"/)
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
     * Output the rom image for the specified system, rom id.
     *
     * @param system system name (atari2600)
     * @param id id of rom.
     * @return
     */
    def showRomImageForSystem(String system, String scrapeId) {
        GamelistEntry game = indexerDataService.gamelistEntryForQuery(/system:"${system}" AND scrapeId:"${scrapeId}"/)
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
            log.error("No game found for ${system} ${id}")
            response.status = 404
        }
    }
}
