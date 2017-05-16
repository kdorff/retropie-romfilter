package retropie.romfilter

import grails.converters.JSON
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
     * TODO: Can I re-use (make a bean for) StandardQueryParser and IndexSearcher?
     * TODO: Fix image
     * TODO: Fix rom details
     * TODO: Fix delete
     * TODO: How do I use config value or service method value in resources.groovy to specify paths for indexes.
     * TODO: Multiple indexes in a directory?
     * TODO: Highlight?
     * TODO: Card View?
     * TODO: Is there a native date format for Lucene for release date and last played?
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
     * TODO: Break out data access from IndexerService
     */

    /**
     * GrailsApplication (auto-injected).
     */
    GrailsApplication grailsApplication

    /**
     * RomfilterDataService (auto-injected).
     */
    IndexerService indexerService

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
        List<SystemEntry> systems = indexerService.systemEntries()
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
        List<RomEntry> romEntryList = indexerService.romEntriesForSystem(system)
        return [
            system: system,
            roms: romEntryList,
        ]
    }

    def showRomForSystem(String system, Long id) {
        GamelistEntry gamelistEntry = GamelistEntry.get(id)
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

    def deleteRomForSystem(String system, Long id) {
        RomEntry toDeleteEntry = RomEntry.get(id)
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
                        toDeleteEntry.delete(flush: true)
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
    def showRomImageForSystem(String system, Long id) {
        GamelistEntry game = GamelistEntry.get(id)
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
