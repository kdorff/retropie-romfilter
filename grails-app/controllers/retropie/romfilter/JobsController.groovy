package retropie.romfilter

import grails.converters.JSON
import grails.core.GrailsApplication
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import org.quartz.JobExecutionContext

class JobsController {

    /**
     * Methods where we enforce specific http verbs.
     */
    static allowedMethods = [
    ]

    /**
     * Delete response code to text message
     * for bulk deletes.
     */
    Map DELETE_RESPONSES = [
        200: "Deletion successful",
        404: "Index entry or ROM path not found",
        500: "Exception deleting ROM."
    ]

    /**
     * GrailsApplication (auto-injected).
     */
    GrailsApplication grailsApplication

    /**
     * IndexerDataService (auto-injected).
     */
    IndexerDataService indexerDataService

    /**
     * JobSubmissionService (auto-injected).
     */
    JobSubmissionService jobSubmissionService

    /**
     * DateTimeFormatter
     */
    DateTimeFormatter isoFormatter = ISODateTimeFormat.dateTime();

    /**
     * Show the jobs and systems page.
     */
    def index() {
    }

    /**
     * systems and romcount ajax json data.
     */
    def jobsAndRomsData() {

        List<String> systems = indexerDataService.listValuesForField('system')

        List<String> romCounts = systems.collect { String system ->
            return "${system}: ${indexerDataService.getCountForSystem(system)} roms"
        }.sort()

        List<String> runningJobs = jobSubmissionService.runningJobs.values().collect { JobExecutionContext runningJob ->
            String fireTime = isoFormatter.print(new DateTime(runningJob.fireTime))
            return "${fireTime} | ${runningJob.jobDetail.description} ${cleanDataMap(runningJob.mergedJobDataMap) ?: ""}"
        }.sort()

        List<String> recentJobs = jobSubmissionService.recentlyCompletedJobs.values().collect { JobExecutionContext finishedJob ->
            long runTime = finishedJob.jobRunTime
            DateTime fireStartDateTime = new DateTime(finishedJob.fireTime)
            DateTime fireEndDateTime = fireStartDateTime.plusMillis(runTime as int)
            String fireEndStr = isoFormatter.print(fireEndDateTime)
            return "${fireEndStr} | ${finishedJob.jobDetail.description} ${cleanDataMap(finishedJob.mergedJobDataMap) ?: ""} took ${runTime}ms"
        }.sort().reverse()

        render([
            systemToCount: romCounts,
            totalCount: indexerDataService.gamesCount,
            runningJobs: runningJobs,
            recentJobs: recentJobs,
        ] as JSON)
    }

    /**
     * Ajax json call to scan ALL systems.
     */
    def rescanAll() {
        jobSubmissionService.submitJob(ScanAllSystemsJob, [:])
        log.info("Scan All Systems job submitted.")
        render([status: "Scan All Systems job submitted."] as JSON)
    }

    private Map cleanDataMap(Map details) {
        Map results = (Map) details.clone()
        results.remove('org.grails.plugins.quartz.grailsJobName')
        results.remove('uuid')
        return results
    }
}
