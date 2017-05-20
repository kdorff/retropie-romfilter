package retropie.romfilter

import grails.test.mixin.TestFor
import retropie.romfilter.indexed.Game
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(RomfilterSyncService)
class RomfilterSyncServiceSpec extends Specification {
    /**
     * ResourceService For loading actual resources.
     */
    ResourceService resourceServiceActual
    IndexerIndexingService indexerIndexingService

    def setup() {
        resourceServiceActual = new ResourceService()
        service.configService = Mock(ConfigService) {
            getImagesPrefix() >> "~/.emulationstation/downloaded_images/"
            getImagesPath() >> "/home/pi/.emulationstation/downloaded_images"
        }
        indexerIndexingService = Mock(IndexerIndexingService)
        service.indexerIndexingService = indexerIndexingService
    }

    void "test xml parsing"() {
        setup:
        String gamelistXml = resourceServiceActual.loadResource('/XmlSamples/gamelist-sample.xml')
        List<Game> expectedGames = [
            new Game(
                system: 'arcade',
                scrapeId: '3129',
                scrapeSource: 'theGamesDB.net',
                path: '20 em 1 (Brazil).zip',
                name: '20 em 1 (Brazil)',
                desc: '20 em 1 (20 in 1) is a compilation of twenty previously unreleased games in a single cartridge, which came bundled with Tec Toy...',
                image: '/home/pi/.emulationstation/downloaded_images/arcade/20 em 1 (Brazil)-image.jpg',
                thumbnail: '',
                developer: 'Tec Toy Indústria de Brinquedos S.A.',
                publisher: 'Tec Toy Indústria de Brinquedos S.A.',
                genre: 'Action',
                players: 1,
                region: 'Brazil',
                romtype: 'Original',
                releasedate: 19970101,
                rating: 0.0D,
                playcount: 0,
                lastplayed: 0,
            ),
            new Game(
                system: 'arcade',
                scrapeId: '2676',
                scrapeSource: 'theGamesDB.net',
                path: 'Ace of Aces (Europe).zip',
                name: 'Ace of Aces (Europe)',
                desc: 'Ace of Aces is a 1st person, 2D flight simulation. You are onboard a British RAF Mosquito, maverick fighter bomber. Your mission? ...',
                image: '/home/pi/.emulationstation/downloaded_images/arcade/Ace of Aces (Europe)-image.jpg',
                thumbnail: '/home/pi/.emulationstation/downloaded_images/arcade/Ace of Aces (Europe)-thumb.jpg',
                developer: 'Artech Digital Entertainment',
                publisher: 'SEGA',
                genre: 'Flight Simulator',
                players: 2,
                region: 'Europe',
                romtype: 'Original',
                releasedate: 19910101,
                rating: 0.5D,
                playcount: 5,
                lastplayed: 20100101,
            )
        ]

        when:
        service.parseGamelistFromXml('arcade', gamelistXml)

        then:
        1 * indexerIndexingService.saveGame(expectedGames[0])
        1 * indexerIndexingService.saveGame(expectedGames[1])
    }
}
