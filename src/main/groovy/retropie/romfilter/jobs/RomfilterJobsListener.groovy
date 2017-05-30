package retropie.romfilter.jobs

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

    /**
     * Constant name.
     */
    static final String LISTENER_NAME = "romfilter-jobs-listener";

    /**
     * Get name of listener.
     * @return
     */
    @Override
    public String getName() {
        return LISTENER_NAME;
    }

    /**
     * Observe a job to be executed.
     * @return
     */
    @Override
    void jobToBeExecuted(JobExecutionContext context) {
        log.trace("jobToBeExecuted ${context.jobDetail.key}")
        jobSubmissionService.observeJobStarted(context)
    }

    /**
     * Observe a job that has been vetoed.
     * @return
     */
    @Override
    void jobExecutionVetoed(JobExecutionContext context) {
        log.trace("jobExecutionVetoed ${context.jobDetail.key}")
        jobSubmissionService.observeJobVetoed(context)
    }

    /**
     * Observe a job that has was executed.
     * @return
     */
    @Override
    void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        log.trace("jobWasExecuted ${context.jobDetail.key}")
        jobSubmissionService.observeJobCompleted(context)
    }

    /**
     * JobSubmissionService (in lieu of auto-injection).
     * @return
     */
    JobSubmissionService getJobSubmissionService() {
        return Holders.grailsApplication.mainContext.getBean('jobSubmissionService')
    }

}
