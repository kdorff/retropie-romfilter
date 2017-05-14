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
     * RomfilterDataService (auto-injected).
     */
    RomfilterSyncService romfilterSyncService


    def init = { servletContext ->
        log.info("retropie-romfilter configuration:")
        showConfig(grailsApplication.config.retropie, "retropie.")

        //romfilterSyncService.scanAll()
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
