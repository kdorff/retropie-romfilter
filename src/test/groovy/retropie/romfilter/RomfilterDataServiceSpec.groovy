package retropie.romfilter

import spock.lang.Specification

class RomfilterDataServiceSpec extends Specification {

    /**
     * Service to test.
     */
    RomfilterDataService service

    /**
     * ResourceService For loading actual resources.
     */
    ResourceService resourceServiceActual

    def setup() {
        resourceServiceActual = new ResourceService()
        service = new RomfilterDataService() {
            @Override
            String getImagesPrefix() {
                return "~/.emulationstation/downloaded_images/"
            }

            @Override
            String getImagesPath() {
                return "/home/pi/.emulationstation/downloaded_images"
            }
        }
    }

    void "test xml parsing"() {
        setup:
        String gamelistXml = resourceServiceActual.loadResource('/XmlSamples/gamelist-sample.xml')

        when:
        Map<String, GamelistEntry> gamesMap = service.parseGamelist(gamelistXml)
        GamelistEntry game1 = gamesMap['20 em 1 (Brazil).zip']
        GamelistEntry game2 = gamesMap['Ace of Aces (Europe).zip']

        then:
        gamesMap.size() == 2

        game1.id == 3129
        game1.source == 'theGamesDB.net'
        game1.path == '20 em 1 (Brazil).zip'
        game1.name == '20 em 1 (Brazil)'
        game1.desc == '20 em 1 (20 in 1) is a compilation of twenty previously unreleased games in a single cartridge, which came bundled with Tec Toy...'
        game1.image == '20 em 1 (Brazil)-image.jpg'
        game1.releasedate == '19970101T000000'
        game1.developer == 'Tec Toy Indústria de Brinquedos S.A.'
        game1.publisher == 'Tec Toy Indústria de Brinquedos S.A.'
        game1.genre == 'Action'
        game1.players == 1
        game1.region == 'Brazil'
        game1.romtype == 'Original'

        game2.id == 2676
        game2.source == 'theGamesDB.net'
        game2.path == 'Ace of Aces (Europe).zip'
        game2.name == 'Ace of Aces (Europe)'
        game2.desc == 'Ace of Aces is a 1st person, 2D flight simulation. You are onboard a British RAF Mosquito, maverick fighter bomber. Your mission? ...'
        game2.image == 'Ace of Aces (Europe)-image.jpg'
        game2.releasedate == '19910101T000000'
        game2.developer == 'Artech Digital Entertainment'
        game2.publisher == 'SEGA'
        game2.genre == 'Flight Simulator'
        game2.players == 1
        game2.region == 'Europe'
        game2.romtype == 'Original'
    }
}
