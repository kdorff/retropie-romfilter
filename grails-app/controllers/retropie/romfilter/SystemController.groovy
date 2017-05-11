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

    def listSystems() {
        println "Listing systems"
        return [
            systems: grailsApplication.config.retropie.romfilter.systems
        ]
    }

    def listRomsForSystem(String system) {
        println "Listing roms for ${system}"
        List<String> gamelist = ['hi bingo.zip', 'there.zip', "woot.zip"]
        String imagePrefix = grailsApplication.config.retropie.emulationStation.imagesPrefix
        String xml =
"""

"""



        Map gameslistXmlData = [
            gameList: [
                game : [
                    [
                        path: 'hi bingo.zip',
                        name: 'brain hi bongo',
                        image: './somepath.jpg',
                        desc: 'description of hi bingo',
                        hasImg: true,
                        genre: 'genre of hi',
                        size: 200,
                    ],
                    [
                        path: 'there.zip',
                        name: 'there',
                        image: '',
                        desc: 'description of there',
                        hasImg: false,
                        genre: 'genre of there',
                        size: 300,
                    ]
                ]
            ]
        ]
        Map filenameToDetails = [:]
        gameslistXmlData.gameList.game.each { Map game ->
            filenameToDetails[game.path] = [
                path: game.path,
                name: game.name,
                image: game.image,
                desc: game.desc,
                hasImg: game.image?.size() > 0 ?: false,
                genre: game.genre,
                size: game.size,
            ]
        }

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
