package retropie.romfilter

import grails.test.mixin.integration.Integration
import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger
import retropie.romfilter.indexed.Game
import spock.lang.Specification
import spock.lang.Unroll

@Integration
class IndexerDataerviceSpec extends Specification {
    /**
     * Load all from resources.groovy.
     */
    static loadExternalBeans = true

    /**
     * Logger.
     */
    Logger log = Logger.getLogger(getClass())

    /**
     * The service we are integration testing (auto-injected).
     */
    IndexerDataService indexerDataService

    /**
     * Before the class tests run.
     *
     * @return
     */
    def setupSpec() {
        // This path needs to line up with the test paths in application.yml
        String indexesPath = 'build/resources/test/integration/indexes'
        println "!!DELETEING indexes from previous execution"
        try {
            // I'm not certain this is necessary but, to to be sure we aren't starting with indexes.
            FileUtils.deleteDirectory(new File(indexesPath))
        }
        catch (IOException e) {
            println "Problem deleting ${indexesPath}, ${e.message}"
        }
    }

    @Unroll
    def "Correct number of games indexed"() {
        expect:
        gamesCount == indexerDataService.gamesCount

        where:
        gamesCount | _
        3                 | 0
    }


    @Unroll
    def "query for game with hash #hash"() {
        when:
        Game game = indexerDataService.getGameForHash(hash)

        then:
        game
        game.system == system
        game.name == name
        game.hash == hash
        game.path == path

        where:
        system      | name                       | hash                               | path
        'atari2600' | 'The Activision Decathlon' | 'b15d819c62fdf63c153d716899ba840c' | 'Activision Decathlon, The (1983) (Activision, David Crane) (AG-930-04, AZ-030) [fixed] ~.zip'
        'atari2600' | 'Adventure'                | '2058f605ec4190cbec8969e9ce45047d' | 'Adventure (1980) (Atari, Warren Robinett - Sears) (CX2613 - 49-75154) ~.zip'
        'atari2600' | ''                         | '97485e45a737b2375cd94084b25a0bfc' | '3-D Tic-Tac-Toe (1980) (Atari, Carol Shaw - Sears) (CX2618 - 49-75123) ~.zip'
    }

    @Unroll
    def "Find game with hash"() {
        when:
        Game game = indexerDataService.getGameForHash(hash)

        then:
        game
        game.system == system
        game.name == name
        game.hash == hash

        where:
        system      | name                       | hash                               | path
        'atari2600' | 'The Activision Decathlon' | 'b15d819c62fdf63c153d716899ba840c' | 'Activision Decathlon, The (1983) (Activision, David Crane) (AG-930-04, AZ-030) [fixed] ~.zip'
        'atari2600' | 'Adventure'                | '2058f605ec4190cbec8969e9ce45047d' | 'Adventure (1980) (Atari, Warren Robinett - Sears) (CX2613 - 49-75154) ~.zip'
        'atari2600' | ''                         | '97485e45a737b2375cd94084b25a0bfc' | '3-D Tic-Tac-Toe (1980) (Atari, Carol Shaw - Sears) (CX2618 - 49-75123) ~.zip'
    }

    def "Verify xml to document creation for game with no gamelist"() {
        when:
        Game game = indexerDataService.getGameForHash('97485e45a737b2375cd94084b25a0bfc')

        then:
        game
        game.system == 'atari2600'
        game.scrapeId == ''
        game.scrapeSource == ''
        game.path == '3-D Tic-Tac-Toe (1980) (Atari, Carol Shaw - Sears) (CX2618 - 49-75123) ~.zip'
        game.name == ''
        game.desc == ''
        game.image == ''
        game.thumbnail == ''
        game.rating == 0
        game.releasedate == 0
        game.developer == ''
        game.publisher == ''
        game.genre == ''
        game.players == 1
        game.region == ''
        game.size == 187
        game.romtype == ''
        game.playcount == 0
        game.lastplayed == 0
        game.hash == '97485e45a737b2375cd94084b25a0bfc'
    }

    def "Verify no gamelist document creation"() {
        when:
        Game game = indexerDataService.getGameForHash('2058f605ec4190cbec8969e9ce45047d')

        then:
        game
        game.system == 'atari2600'
        game.scrapeId == '2570'
        game.scrapeSource == 'theGamesDB.net'
        game.path == 'Adventure (1980) (Atari, Warren Robinett - Sears) (CX2613 - 49-75154) ~.zip'
        game.name == 'Adventure'
        game.desc == 'Adventure was the first action-adventure game on a video console, the first to contain a widely-known Easter egg, and the first to allow a player to have a stash of items, which required the player to select which one to use at any given moment, usually through keyboard or joystick input. Adventure allowed the player to drop one item and pick up another without having to type in any commands. The graphics, on the other hand, were not that great, and Robinett even described the dragons as looking like ducks.'
        game.image.endsWith('atari2600/adventure.jpg')  // do better
        game.thumbnail.endsWith('atari2600/adventure-thumb.jpg')  // do better
        game.rating == 78
        game.releasedate == 19780101
        game.developer == 'Atari'
        game.publisher == 'Atari'
        game.genre == 'Adventure'
        game.players == 5
        game.region == "USA"
        game.size == 187
        game.romtype == 'tiny'
        game.playcount == 37
        game.lastplayed == 20170415
        game.hash == '2058f605ec4190cbec8969e9ce45047d'
    }

    def "fetch all games for atari2600"() {
        when:
        List<Game> games = indexerDataService.getGamesForQuery(/+system:"atari2600"/)

        then:
        games.size() == 3
    }

    def "fetch all games for atari2600 described with action"() {
        when:
        List<Game> games = indexerDataService.getGamesForQuery(/+system:"atari2600" +desc:action/)
        log.info(games)

        then:
        games.size() == 1
    }

    def "fetch all games for atari2600 all all: containing 'action'"() {
        when:
        List<Game> games = indexerDataService.getGamesForQuery(/+system:"atari2600" +all:action/)
        log.info(games)

        then:
        games.size() == 1
    }
}
