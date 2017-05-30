package retropie.romfilter

import grails.converters.JSON
import grails.core.GrailsApplication
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import org.quartz.JobExecutionContext
import retropie.romfilter.jobs.JobSubmission

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

        // Order the jobs, recent changes to queue state on top oldest.
        // changes to the bottom.
        List<String> jobs = jobSubmissionService.jobs.values().collect { JobSubmission job ->
            JobExecutionContext context = job.context
            JobSubmission.StateDateTime stateDateTime = job.currentStateDateTime()
            String stateDateTimeStr = isoFormatter.print(stateDateTime.dateTime)
            String desc = context?.jobDetail?.description ?: ""
            Map dataMap = cleanDataMap(context?.mergedJobDataMap ?: [:])
            String data = dataMap ? dataMap.toString() : ""
            return "${stateDateTimeStr} | ${stateDateTime.state} | ${desc} ${data}"
        }.sort().reverse()

        render([
            systemToCount: romCounts,
            totalCount: indexerDataService.gamesCount,
            jobs: jobs,
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
