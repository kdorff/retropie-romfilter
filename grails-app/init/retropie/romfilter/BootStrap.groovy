package retropie.romfilter

import grails.core.GrailsApplication
import org.apache.log4j.Logger

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


    def init = { servletContext ->
        log.info("retropie-romfilter configuration:")
        showConfig(grailsApplication.config.retropie, "retropie.")

        log.info("SystemEntry.count=${indexerDataService.systemEntryCount}")
        log.info("RomEntry.count=${indexerDataService.romEntryCount}")
        log.info("GamelistEntry.count=${indexerDataService.gamelistEntryCount}")

        if (configService.scanAtStartup && (indexerDataService.romEntryCount + indexerDataService.systemEntryCount + indexerDataService.gamelistEntryCount) == 0) {
            log.info("Indexes are all empty. Starting full scan.")
            romfilterSyncService.scanAll()
        }
    }

    def destroy = {
    }

    void showConfig(config, String prefix) {
        config.each { String key, value ->
            if (value instanceof Map) {
                showConfig(value, "${prefix}${key}.")
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
