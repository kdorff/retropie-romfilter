import grails.util.BuildSettings
import grails.util.Environment

// See http://logback.qos.ch/manual/groovy.html for details on configuration
if (Environment.current == Environment.DEVELOPMENT || Environment.current == Environment.TEST) {
    appender('STDOUT', ConsoleAppender) {
        encoder(PatternLayoutEncoder) {
            pattern = "%level %logger - %msg%n"
        }
    }
    root(ERROR, ['STDOUT'])
}
else {
    appender("LOGFILE", FileAppender) {
        file = "./logs/romfilter.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%level %d %logger - %msg%n"
        }
    }
    root(ERROR, ['LOGFILE'])
}

def targetDir = BuildSettings.TARGET_DIR
if (Environment.current == Environment.DEVELOPMENT && targetDir) {
    appender("FULL_STACKTRACE", FileAppender) {
        file = "${targetDir}/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%level %d %logger - %msg%n"
        }
    }
    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
}

logger 'retropie.romfilter', TRACE
logger'grails.app.controllers', DEBUG
logger'grails.app.jobs', DEBUG
