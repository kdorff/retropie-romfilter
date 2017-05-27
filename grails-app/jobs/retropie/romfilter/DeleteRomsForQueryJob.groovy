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
            deleted: [],
            failed: [],
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

            games.each { Game game ->
                Path path = Paths.get(game.path)
                try {
                    Files.delete(path)
                    results.deleted << games.name
                }
                catch (Exception e) {
                    log.error("Error deleting path ${path}", e)
                    results.failed << games.name
                }
            }
        }
        finally {
            log.info("DeleteRomsForQueryJob (deleted count=${results.deleted.size()}, failed count=${results.failed.size()}) for query ${queryStr} took ${System.currentTimeMillis() - start}ms")
        }
    }
}
