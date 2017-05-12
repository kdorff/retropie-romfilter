package retropie.romfilter

import grails.core.GrailsApplication
import org.apache.commons.io.FilenameUtils

class SystemController {

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
            systems: romfilterDataService.listSystems().findAll { it.romCount > 0 }
        ]
    }

    def listRomsForSystem(String system) {
        Map<String, File> gameFilenameToFileMap = romfilterDataService.listRomsForSystem(system)
        return [
            system: system,
            gamelist: gameFilenameToFileMap.keySet(),
            filenameToDetails: romfilterDataService.gamelistForSystem(system),
        ]
    }

    /**
     *
     * @param system
     * @param id
     * @return
     */
    def showRomImageForSystem(String system, Integer id) {
        /**
         * TODO: What if the path in gamelist.xml used .. to try to break out. Detect this?
         * TODO: Kind of important since I am serving up data from the filesystem
         * TODO: BUT this app isn't desiged to be exposed outside of the isolated environment.
         */
        println "Trying to show image for game system.id=${system}.${id}"
        GamelistEntry game = romfilterDataService.gamelistEntryForId(system, id)
        if (game) {
            String fileType = FilenameUtils.getExtension(game.image).toLowerCase()
            String mimeType = IMAGE_EXT_TO_MIME[fileType]
            if (!mimeType) {
                log.error("Invalid file extension for image ${fileType}")
                response.status = 404
            }
            else {
                File imageFile = new File(game.image)
                if (imageFile.exists() && imageFile.canRead() && imageFile.isFile()) {
                    println "Planning to output ${game.image}"
                    FileInputStream imageStream = new FileInputStream(imageFile)
                    response.setHeader("Content-Length", "${imageFile.length()}")
                    response.contentType = mimeType
                    response.outputStream << imageStream
                    response.outputStream.flush()
                } else {
                    log.error("Image not found: ${imageFile}")
                    response.status = 404
                }
            }
        }
        else {
            log.error("No game found for ${system} ${id}")
            response.status = 404
        }
    }
}
