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
        return [
            systems: SystemEntry.list()
        ]
    }

    def listRomsForSystem(String system) {
        Map<String, Path> localRoms = romfilterDataService.listRomsForSystem(system)
        Map<String, GamelistEntry> filenameToDetails = romfilterDataService.filenameToGamelistEntryForSystem(system)
        return [
            system: system,
            gamelist: localRoms.keySet(),
            filenameToDetails: filenameToDetails,
            romfilterDataService: romfilterDataService,
        ]
    }

    def showRomForSystem(String system, Long id) {
        GamelistEntry gameDetails = GamelistEntry.get(id)
        Map<String, GamelistEntry> filenameToDetails = romfilterDataService.filenameToGamelistEntryForSystem(system)
        if (gameDetails) {
            return [
                system: system,
                gameDetails: gameDetails,
                filenameToDetails: filenameToDetails,
                romfilterDataService: romfilterDataService,
            ]
        }
        else {
            log.error("No game found for ${system} ${id}")
            response.status = 404
        }
    }

    def deleteRomForSystem(String system, String hash) {
        Map<String, Path> roms = romfilterDataService.listRomsForSystem(system)
        Map.Entry<String, Path> toDeleteEntry = roms.find { Map.Entry<String, Path> romEntry ->
            // Search the filesystem for the entry whose filename has the same has
            return romfilterDataService.hash(romEntry.key) == hash
        }
        if (!toDeleteEntry) {
            log.error("ROM for hash ${hash} not found")
            response.status = 404
        }
        else {
            // Delete the file (move it to trash)
            String trashPathStr = romfilterDataService.getTrashPathForSystem(system)
            Path toDeletePath = toDeleteEntry.value
            Path trashDestinationPath = Paths.get(trashPathStr, toDeletePath.fileName.toString())

            try {
                if (Files.move(toDeletePath, trashDestinationPath)) {
                    log.trace("Moved ${toDeletePath} to ${trashDestinationPath}")
                    response.status = 200
                    /**
                     * TODO: Decrement rom count in database.
                     */
                }
                else {
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
