package retropie.romfilter

import grails.converters.JSON
import grails.core.GrailsApplication
import grails.plugins.quartz.JobManagerService
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
     * JobManagerService (auto-injected).
     */
    JobManagerService jobManagerService

    /**
     * IndexerDataService (auto-injected).
     */
    IndexerDataService indexerDataService

    /**
     * JobSubmissionService (auto-injected).
     */
    JobSubmissionService jobSubmissionService

    /**
     * Show the jobs and systems page.
     */
    def index() {
    }

    /**
     * systems and romcount ajax json data.
     */
    def systemToRomCountData() {
        List<String> systems = indexerDataService.listValuesForField('system')
        List<String> results = systems.collect { String system ->
            return "${system}: ${indexerDataService.getCountForSystem(system)} roms"
        }
        render([
            systemToCount: results.sort(),
            totalCount: indexerDataService.gamesCount
        ] as JSON)
    }

    /**
     * Running jobs ajax json data.
     */
    def runningJobsData() {
        List<String> runningJobs = jobManagerService.runningJobs.collect { JobExecutionContext runningJob ->
            return "${runningJob.jobDetail.description} ${runningJob.mergedJobDataMap?.system}"
        }
        render(runningJobs.sort() as JSON)
    }

    /**
     * Ajax json call to scan ALL systems.
     */
    def rescanAll() {
        jobSubmissionService.submitJob(ScanAllSystemsJob, [:])
        log.info("Scan All Systems job submitted.")
        render([status: "Scan All Systems job submitted."] as JSON)
    }
}
