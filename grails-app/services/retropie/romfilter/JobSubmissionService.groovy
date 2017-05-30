package retropie.romfilter

import com.google.common.cache.CacheBuilder
import grails.plugins.quartz.JobManagerService
import org.apache.log4j.Logger
import org.quartz.JobExecutionContext
import org.quartz.Scheduler
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.beans.factory.InitializingBean
import retropie.romfilter.jobs.JobSubmission
import retropie.romfilter.jobs.RomfilterJobsListener

import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit

/**
 * Class to submit jobs to quarts and return the uuid of the job back.
 * The jobUuid value is created and used solely by this service.
 * Works in concert with the RomfilterJobsListener.
 */
class JobSubmissionService implements InitializingBean {

    /**
     * Logger.
     */
    Logger log = Logger.getLogger(getClass())

    /**
     * JobManagerService (auto-injected).
     */
    JobManagerService jobManagerService

    /**
     * Map of jobSubmission.uuid to JobSubmission for
     * submitted, running, vetoed, or completed jobs from the last N minutes.
     */
    ConcurrentMap<String, JobSubmission> jobs =
        CacheBuilder.newBuilder().
            expireAfterWrite(20, TimeUnit.MINUTES).
            <String, JobSubmission>build().asMap()

    /**
     * Submit a job. Return a uuid identifier for the job.
     *
     * @param jobClass
     * @param params
     * @return
     */
    String submitJob(Class jobClass, Map params) throws IllegalArgumentException {
        // Insert a new uuid into the jobs params so we can find it again
        // when the job starts running (or is vetoed?). See the RomfilterJobsListener.
        Map updatedParams = (Map) params?.clone() ?: [:]
        JobSubmission jobSubmission = new JobSubmission()
        updatedParams.uuid = jobSubmission.uuid
        jobs[jobSubmission.uuid] = jobSubmission

        /**
         * TODO: Wouldn't it be nice if I didn't have to do this for
         * TODO: each individual kind of Job that I support?
         */
        if (jobClass == ScanAllSystemsJob) {
            // Submit the job
            log.info("Submitting ScanAllSystemsJob")
            ScanAllSystemsJob.triggerNow(updatedParams)
        }
        else if (jobClass == ScanSystemJob) {
            // Submit the job
            log.info("Submitting SystemScanJob")
            ScanSystemJob.triggerNow(updatedParams)
        }
        else if (jobClass == DeleteRomsForQueryJob) {
            // Submit the job
            log.info("Submitting DeleteRomsForQueryJob")
            DeleteRomsForQueryJob.triggerNow(updatedParams)
        }
        else {
            // Could not submit the job
            throw new IllegalArgumentException("Specified jobClass parameter doesn't match a known Job class")
        }
        return jobSubmission.uuid
    }

    /**
     * Wait for a job with a specific uuid to complete.
     *
     * @param uuid
     * @param secondsToWait
     * @return job execution context or null if the completed job was never found.
     */
    JobExecutionContext waitForCompletedJob(String uuid, int secondsToWait = 60) {
        int remainingAttempts = secondsToWait
        JobSubmission jobSubmission = null
        while (remainingAttempts--) {
            jobSubmission = jobs[uuid]
            if (jobSubmission.currentState in [JobSubmission.State.COMPLETE, JobSubmission.State.VETOED]) {
                break
            }
            Thread.sleep(1000)
        }
        log.info("Waited for jobs.uuid ${uuid} and got ${jobSubmission}")
        return jobSubmission?.context
    }

    /**
     * Wait for the scheduler to become quiet.
     *
     * TODO: This should have an number of seconds to remain quiet check?
     *
     * @param secondsToWait
     * @return
     */
    boolean waitForQuietScheduler(int secondsToWait = 60) {
        int remainingAttempts = secondsToWait
        boolean quiet = false
        while (remainingAttempts--) {
            // Find the job that is running or waiting to run
            JobSubmission runningOrWaiting = jobs.find { String uuid, JobSubmission jobSubmission ->
                return (jobSubmission.currentState in [JobSubmission.State.RUNNING, JobSubmission.State.SUBMITTED])
            }?.value
            if (!runningOrWaiting) {
                quiet = true
                break
            }
            Thread.sleep(1000)
        }
        return quiet
    }

    /**
     * This service depends on the RomfilterJobsListener to learn what jobs
     * are running and what jobs have completed. Install this listener here.
     *
     * @throws Exception
     */
    @Override
    void afterPropertiesSet() throws Exception {
        Scheduler quartzScheduler = jobManagerService.quartzScheduler
        RomfilterJobsListener jobsListener = new RomfilterJobsListener()
        quartzScheduler.getListenerManager().addJobListener(
            jobsListener,
            GroupMatcher.groupEquals('romfilter-jobs')
        )
    }

    /**
     * Observe job is running.
     */
    void observeJobStarted(JobExecutionContext context) {
        observeStateChange(context, JobSubmission.State.RUNNING)
    }

    /**
     * Observe job was vetoed.
     */
    void observeJobVetoed(JobExecutionContext context) {
        observeStateChange(context, JobSubmission.State.VETOED)
    }

    /**
     * Observe job was completed.
     */
    void observeJobCompleted(JobExecutionContext context) {
        observeStateChange(context, JobSubmission.State.COMPLETE)
    }

    /**
     * Move job to map.
     *
     * @param context
     * @param state
     */
    void observeStateChange(JobExecutionContext context, JobSubmission.State state) {
        String uuid = (String) context.mergedJobDataMap.uuid
        if (uuid) {
            JobSubmission jobSubmission = jobs[uuid]
            if (jobSubmission) {
                if (!jobSubmission.context) {
                    jobSubmission.context = context
                }
                jobSubmission.addState(state)
            }
            else {
                log.error("No JobSubmission found for uuid.")
            }
        }
        else {
            log.error("No uuid in job.")
        }
    }
}
