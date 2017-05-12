package retropie.romfilter

import grails.core.GrailsApplication

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class SystemController {

    /**
     * GrailsApplication (auto-injected).
     */
    GrailsApplication grailsApplication

    /**
     * ResourceService (auto-injected).
     */
    ResourceService resourceService

    final static List<String> VALID_IMAGE_TYPES = ['png', 'jpg'].asImmutable()

    /**
     * GamelistParserService (auto-injected).
     */
    GamelistParserService gamelistParserService

    def listSystems() {
        println "Listing systems"
        return [
            systems: grailsApplication.config.retropie.romfilter.systems
        ]
    }

    def listRomsForSystem(String system) {
        println "Listing roms for ${system}"


        Map<String, File> gameFilenameToFileMap = gamelistParserService.listRomsForSystem(system)

        Map<String, GamelistEntry> filenameToDetails = gamelistParserService.parseGamelistForSystem(system)

        return [
            system: system,
            gamelist: gameFilenameToFileMap.keySet(),
            filenameToDetails: filenameToDetails,
        ]
    }

    def showRomImage(String romImagePath) {
        String imageFilename = "${gamelistParserService.imagesPath}/${romImagePath}"
        println "Planning to output ${imageFilename}"
        String imageType = FilenameUtils.getExtension(imageFilename).toLowerCase()
        if (imageType in VALID_IMAGE_TYPES) {
            File imageFile = new File(imageFilename)
            BufferedImage originalImage = ImageIO.read(imageFile)
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
            ImageIO.write(originalImage, "jpg", outputStream)
            byte[] imageInByte = outputStream.toByteArray()
            response.setHeader("Content-Length", imageInByte.length.toString())
            response.contentType = "image/jpg"
            response.outputStream << imageInByte
            response.outputStream.flush()
        }
        else {
            throw new Exception("Invalid extension for image ${path}")
        }

    }
}
