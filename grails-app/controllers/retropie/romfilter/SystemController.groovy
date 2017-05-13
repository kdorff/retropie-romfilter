package retropie.romfilter

import grails.core.GrailsApplication
import org.apache.commons.io.FilenameUtils

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SystemController {

    /**
     * TODO: Use endless scrolling datatables to load and filter the data on demand. Much more responsive.
     * TODO: Search box should support field search.
     * TODO: Show name/filename similarity?
     * TODO: Filter to show dissimilar name/filenames
     * TODO: Some common filters? (Unl), (World) (Beta) (Proto) (countries), etc.
     * TODO: A "delete all visible" button?
     */

    /**
     * GrailsApplication (auto-injected).
     */
    GrailsApplication grailsApplication

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
     * RomfilterDataService (auto-injected).
     */
    RomfilterDataService romfilterDataService

    def listSystems() {
        println "Listing systems"
        List<SystemEntry> systems = SystemEntry.list()
        Map<String, Integer> systemToNumRoms = [:]
        systems.each { SystemEntry systemEntry ->
            systemToNumRoms[systemEntry.name] = RomEntry.countBySystem(systemEntry.name)
        }
        return [
            systems: systems,
            systemToNumRoms: systemToNumRoms,
        ]
    }

    def listRomsForSystem(String system) {
        List<RomEntry> romEntryList = RomEntry.findAllBySystem(system)
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
                romfilterDataService.getRomsPathForSystem(system),
                toDeleteEntry.filename)
            if (!Files.exists(toDeletePath)) {
                log.error("ROM not found in on disc ${toDeletePath}")
                response.status = 404
            } else {
                // Delete the file (move it to trash)
                String trashPathStr = romfilterDataService.getTrashPathForSystem(system)
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
