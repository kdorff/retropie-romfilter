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
        List<String> gamelist = [
            '20 em 1 (Brazil).zip',
            'Ace of Aces (Europe).zip',
            'woot.zip'
        ]

        String imagePrefix = grailsApplication.config.retropie.emulationStation.imagesPrefix

        Map<String, GamelistEntry> filenameToDetails = gamelistParserService.parseGamelist(
            resourceService.loadResource('/XmlSamples/gamelist-sample.xml')
        )
        return [
            system: system,
            gamelist: gamelist,
            filenameToDetails: filenameToDetails,
        ]
    }

    def renderImage() {
        String imageName = params.imageName
        File imageFile = new File("${PICTURES_DIR}/${imageName}")
        BufferedImage originalImage = ImageIO.read(imageFile)
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ImageIO.write(originalImage, "jpg", outputStream)
        byte[] imageInByte = outputStream.toByteArray()
        response.setHeader("Content-Length", imageInByte.length.toString())
        response.contentType = "image/jpg"
        response.outputStream << imageInByte
        response.outputStream.flush()
    }
}
