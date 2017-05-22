package retropie.romfilter

import grails.core.GrailsApplication
import org.apache.log4j.Logger
import org.apache.lucene.index.IndexWriter
import retropie.romfilter.indexed.Game

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class BootStrap {

    /**
     * Logger.
     */
    Logger log = Logger.getLogger(getClass())

    /**
     * GrailsApplication (auto-injected).
     */
    GrailsApplication grailsApplication

    /**
     * RomfilterSyncService (auto-injected).
     */
    RomfilterSyncService romfilterSyncService

    /**
     * IndexerDataService (auto-injected).
     */
    IndexerDataService indexerDataService

    /**
     * ConfigService (auto-injected).
     */
    ConfigService configService

    /**
     * GamesIndexWriter (auto-injected).
     */
    IndexWriter gamesIndexWriter

    /**
     * Application startup.
     */
    def init = { servletContext ->
        log.info("retropie-romfilter configuration:")
        logRomfilterConfigs(grailsApplication.config.retropie, "retropie.")

        Path romsPath = Paths.get(configService.getRomsPath())
        if (!Files.exists(romsPath)) {
            throw new IOException("Roms path does not exist ${romsPath}")
        }

        Path gamelistsPath = Paths.get(configService.getGamelistsPath())
        if (!Files.exists(gamelistsPath)) {
            throw new IOException("Gamelists path does not exist ${gamelistsPath}")
        }

        log.info("GamelistEntry.count=${indexerDataService.gamesCount}")

        if (configService.scanAtStartup && indexerDataService.gamesCount == 0) {
            log.info("Indexes are all empty. Starting full scan.")
            romfilterSyncService.scanAll()
        }

        log.info("Warming up Game.GameColumn enum to validate that all " +
            "configuration appears correct ${Game.GameColumn.HASH.hashCode()}")
    }

    /**
     * Application shutdown.
     */
    def destroy = {
        println("Shutting down index writers")
        gamesIndexWriter.close()
        println("Writers closed.")
    }

    /**
     * Display romfilter config.
     *
     * @param config
     * @param prefix
     */
    void logRomfilterConfigs(config, String prefix) {
        config.each { String key, value ->
            if (value instanceof Map) {
                logRomfilterConfigs(value, "${prefix}${key}.")
            }
            else {
                String exists = ""
                if (key.endsWith('Path')) {
                    exists = "     | Exists? ${new File(value.toString()).exists()}"
                }
                log.info("|  ${prefix}${key} = ${value}${exists}")
            }
        }
    }

}
