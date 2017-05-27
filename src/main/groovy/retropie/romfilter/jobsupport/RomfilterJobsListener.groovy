package retropie.romfilter.jobsupport

import grails.util.Holders
import org.apache.log4j.Logger
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobListener
import retropie.romfilter.JobSubmissionService

/**
 * Listen for changes to quartz jobs.
 * This provides data to the JobSubmissionService.
 */
class RomfilterJobsListener implements JobListener {

    /**
     * Logger.
     */
    Logger log = Logger.getLogger(getClass())

    static final String LISTENER_NAME = "romfilter-jobs-listener";

    @Override
    public String getName() {
        return LISTENER_NAME;
    }

    @Override
    void jobToBeExecuted(JobExecutionContext context) {
        String jobName = context.getJobDetail().getKey().toString()
        log.trace("jobToBeExecuted")
        log.trace("Job ${jobName} is to be executed wutg data ${context.mergedJobDataMap}.")
        String uuid = (String) context.mergedJobDataMap.uuid
        if (uuid) {
            jobSubmissionService.runningJobs[uuid] = context
        }
        else {
            log.warn("No uuid in job.")
        }
    }

    @Override
    void jobExecutionVetoed(JobExecutionContext context) {
        log.trace("jobExecutionVetoed");
        String uuid = (String) context.mergedJobDataMap.uuid
        if (uuid) {
            jobSubmissionService.recentlyCompletedJobs[uuid] = context
        }
        else  {
            log.warn("No uuid in job.")
        }
    }

    @Override
    void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        log.trace("jobWasExecuted");
        String uuid = (String) context.mergedJobDataMap.uuid
        if (uuid) {
            if (jobSubmissionService.runningJobs.containsKey(uuid)) {
                jobSubmissionService.runningJobs.remove(uuid)
            }
            /**
             * Should we do anything if there was an exception.
             */
            jobSubmissionService.recentlyCompletedJobs[uuid] = context
        }
        else {
            log.warn("No uuid in job.")
        }
    }

    /**
     * JobSubmissionService (in lieu of auto-injection).
     * @return
     */
    JobSubmissionService getJobSubmissionService() {
        return Holders.grailsApplication.mainContext.getBean('jobSubmissionService')
    }

}
