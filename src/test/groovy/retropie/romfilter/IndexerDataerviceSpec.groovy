package retropie.romfilter

import grails.test.mixin.integration.Integration
import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger
import org.apache.lucene.queryparser.classic.QueryParser
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
        systemCount == indexerDataService.systemEntryCount
        romCount == indexerDataService.romEntryCount

        where:
        gamelistentyCount | systemCount | romCount
        4                 | 1           | 3
    }


    @Unroll
    def "query for gamelist with system and hash"() {
        setup:
        String hashVal = QueryParser.escape(hash.toString())
        String hashRange = "[${hashVal} TO ${hashVal}]"
        String query = /+system:"${QueryParser.escape((String) system)}" +hash:${hashRange}/

        when:
        Game gamelistEntry = indexerDataService.getGameForQuery(query)

        then:
        gamelistEntry
        gamelistEntry.system == system
        gamelistEntry.name == name
        gamelistEntry.hash == hash

        where:
        system      | name                       | hash
        'atari2600' | 'The Activision Decathlon' | 1519037557
        'atari2600' | 'Adventure'                | 555897016
        'atari2600' | 'Asteroids'                | -47592823
        'atari2600' | 'Astrowar (USA)'           | 1999839840
    }

    @Unroll
    def "Find gamelist with system and hash"() {
        when:
        Game gamelistEntry = indexerDataService.getGamelistEntryForSystemAndHash((String) system, (int) hash)

        then:
        gamelistEntry
        gamelistEntry.system == system
        gamelistEntry.name == name
        gamelistEntry.hash == hash

        where:
        system      | name                       | hash
        'atari2600' | 'The Activision Decathlon' | 1519037557
        'atari2600' | 'Adventure'                | 555897016
        'atari2600' | 'Asteroids'                | -47592823
        'atari2600' | 'Astrowar (USA)'           | 1999839840
    }

    def "fetch all gamelists for atari2600"() {
        when:
        List<Game> games = indexerDataService.getGamesForQuery(/+system:"atari2600"/)

        then:
        games.size() == 4
    }

    def "fetch all all sysetms"() {
        when:
        List<SystemEntry> systems = indexerDataService.getAllSystemsEntries()
        log.info(systems)

        then:
        systems.size() == 1
    }

    def "fetch all gamelists for atari2600 described with action"() {
        when:
        List<Game> games = indexerDataService.getGamesForQuery(/+system:"atari2600" +desc:action/)
        log.info(games)

        then:
        games.size() == 2
    }

    def "fetch all gamelists for atari2600 all all: containing 'action'"() {
        when:
        List<Game> games = indexerDataService.getGamesForQuery(/+system:"atari2600" +all:action/)
        log.info(games)

        then:
        games.size() == 2
    }

    @Unroll
    def "query for roms with system and path"() {
        setup:
        when:
        RomEntry romEntry = indexerDataService.getRomEntryForSystemAndHash(system, hash)

        then:
        romEntry
        romEntry.system == system
        romEntry.hash == hash
        romEntry.path == path

        where:
        system      | hash       | gamelistHash | path
        'atari2600' | -480653922 | null         | '3-D Tic-Tac-Toe (1980) (Atari, Carol Shaw - Sears) (CX2618 - 49-75123) ~.zip'
        'atari2600' | -433859633 | 1659784634   | 'Activision Decathlon, The (1983) (Activision, David Crane) (AG-930-04, AZ-030) [fixed] ~.zip'
        'atari2600' | -508239167 | -1661632215  | 'Adventure (1980) (Atari, Warren Robinett - Sears) (CX2613 - 49-75154) ~.zip'
    }

    @Unroll
    def "query for roms for system"() {
        setup:
        when:
        List<RomEntry> roms = indexerDataService.getRomEntriesForSystem('atari2600')
        log.info(roms)

        then:
        roms.size() == 3
    }

    @Unroll
    def "query for gamelist with system and path"() {
        setup:
        when:
        Game gamelistEntry = indexerDataService.getGamelistEntryForSystemAndPath(system, path)

        then:
        gamelistEntry
        gamelistEntry.system == system
        gamelistEntry.name == name
        gamelistEntry.hash == hash
        gamelistEntry.path == path

        where:
        system      | name                       | hash       | path
        'atari2600' | 'The Activision Decathlon' | 1519037557 | 'Activision Decathlon, The (1983) (Activision, David Crane) (AG-930-04, AZ-030) [fixed] ~.zip'
        'atari2600' | 'Adventure'                | 555897016  | 'Adventure (1980) (Atari, Warren Robinett - Sears) (CX2613 - 49-75154) ~.zip'
        'atari2600' | 'Asteroids'                | -47592823  | 'Asteroids (1981) (Atari, Brad Stewart - Sears) (CX2649 - 49-75163) ~.zip'
        'atari2600' | 'Astrowar (USA)'           | 1999839840 | 'Astrowar (Unknown).zip'
    }

}
