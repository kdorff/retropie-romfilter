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
    def "Correct number of roms indexed"() {
        expect:
        gamelistentyCount == indexerDataService.gamesCount

        where:
        gamelistentyCount | _
        3                 | 0
    }


    @Unroll
    def "query for gamelist with hash #hash"() {
        when:
        Game gamelistEntry = indexerDataService.getGameForHash(hash)

        then:
        gamelistEntry
        gamelistEntry.system == system
        gamelistEntry.name == name
        gamelistEntry.hash == hash
        gamelistEntry.path == path

        where:
        system      | name                       | hash                               | path
        'atari2600' | 'The Activision Decathlon' | 'b15d819c62fdf63c153d716899ba840c' | 'Activision Decathlon, The (1983) (Activision, David Crane) (AG-930-04, AZ-030) [fixed] ~.zip'
        'atari2600' | 'Adventure'                | '2058f605ec4190cbec8969e9ce45047d' | 'Adventure (1980) (Atari, Warren Robinett - Sears) (CX2613 - 49-75154) ~.zip'
        'atari2600' | ''                         | '97485e45a737b2375cd94084b25a0bfc' | '3-D Tic-Tac-Toe (1980) (Atari, Carol Shaw - Sears) (CX2618 - 49-75123) ~.zip'
    }

    @Unroll
    def "Find gamelist with system and hash"() {
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

    def "fetch all gamelists for atari2600"() {
        when:
        List<Game> games = indexerDataService.getGamesForQuery(/+system:"atari2600"/)

        then:
        games.size() == 3
    }

    def "fetch all gamelists for atari2600 described with action"() {
        when:
        List<Game> games = indexerDataService.getGamesForQuery(/+system:"atari2600" +desc:action/)
        log.info(games)

        then:
        games.size() == 1
    }

    def "fetch all gamelists for atari2600 all all: containing 'action'"() {
        when:
        List<Game> games = indexerDataService.getGamesForQuery(/+system:"atari2600" +all:action/)
        log.info(games)

        then:
        games.size() == 1
    }
}
