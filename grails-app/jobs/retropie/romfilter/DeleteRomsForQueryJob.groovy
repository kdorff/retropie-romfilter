package retropie.romfilter

import org.apache.log4j.Logger
import org.apache.lucene.search.Query
import org.quartz.JobExecutionContext
import retropie.romfilter.indexed.Game

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

class DeleteRomsForQueryJob {
    def description = 'Delete roms matching query'
    def group = "romfilter-jobs"
    def concurrent = false
    def sessionRequired = false

    /**
     * No triggers. This job is called only manually.
     */
    static triggers = {}

    /**
     * Pattern to match int / long type numbers within a string.
     */
    final static Pattern INT_STRING_PATTERN = ~/(\d+)/

    /**
     * Pattern to match int / long type numbers within a string.
     */
    final static Pattern FLOAT_STRING_PATTERN = ~/(\d+\.\d+)|(\d+)/

    /**
     * Logger.
     */
    Logger log = Logger.getLogger(getClass())

    /**
     * ConfigService (auto-injected).
     */
    ConfigService configService

    /**
     * IndexerService (auto-injected).
     */
    IndexerDataService indexerDataService

    /**
     *
     * @param context
     */
    Random random = new Random()

    void execute(JobExecutionContext context) {
        long start = System.currentTimeMillis()

        Map results = [
            deleted: 0,
            failed: 0,
        ]
        String queryStr = context.mergedJobDataMap.get('query')
        context.mergedJobDataMap.results = results
        try {
            if (!queryStr) {
                log.error("DeleteRomsForQueryJob called but no query parameter provided")
                return
            }

            Query query = indexerDataService.createQueryForString(queryStr)
            List<Game> games = indexerDataService.getGamesForQuery(query)
            indexerDataService.deleteAllForQuery(query)

            games.each { Game toDeleteGame ->
                Path toDeletePath = Paths.get(configService.romsPath, toDeleteGame.system, toDeleteGame.path)

                // Delete the file (move it to trash)
                String trashPathStr = configService.trashPath

                // Make sure system trash folder exists
                Path trashDestinationFolderPath = Paths.get(trashPathStr, toDeleteGame.system)
                Files.createDirectories(trashDestinationFolderPath)

                // Location with trash to move rom
                Path trashDestinationPath = Paths.get(trashPathStr, toDeleteGame.system, toDeleteGame.path)

                try {
                    if (Files.move(toDeletePath, trashDestinationPath)) {
                        log.trace("Moved ${toDeletePath} to ${trashDestinationPath}")
                        results.deleted++
                    }
                    else {
                        // TODO: Can this happen?
                        log.error("Unable to move ${toDeletePath} to ${trashDestinationPath}. Note that this may not work across filesystems, etc.")
                        results.failed++
                    }
                }
                catch (IOException e) {
                    log.error("Exception moving ${toDeletePath} to ${trashDestinationPath}. Note that may will not work across filesystems, etc.", e)
                    results.failed++
                }

            }
        }
        finally {
            log.info("DeleteRomsForQueryJob (deleted count=${results.deleted}, failed count=${results.failed}) for query ${queryStr} took ${System.currentTimeMillis() - start}ms")
        }
    }
}
