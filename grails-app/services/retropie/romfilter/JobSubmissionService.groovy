package retropie.romfilter

import com.google.common.cache.CacheBuilder
import grails.plugins.quartz.JobManagerService
import org.apache.log4j.Logger
import org.quartz.JobExecutionContext
import org.quartz.Scheduler
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.beans.factory.InitializingBean
import retropie.romfilter.jobsupport.RomfilterJobsListener

import java.util.concurrent.ConcurrentMap

/**
 * Class to submit jobs to quarts and return the uuid of the job back.
 * The jobUuid value is created and used solely by this service.
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
     * List of running (or soon to be running?) jobs.
     */
    ConcurrentMap<String, JobExecutionContext> runningJobs =
        CacheBuilder.newBuilder().<String, JobExecutionContext>build().asMap();

    /**
     * List of recently finished or vetoed jobs.
     */
    ConcurrentMap<String, JobExecutionContext> recentlyCompletedJobs =
        CacheBuilder.newBuilder().maximumSize(50).<String, JobExecutionContext>build().asMap();

    /**
     * Submit a job. Return a uuid identifier for the job.
     *
     * @param jobClass
     * @param params
     * @return
     */
    String submitJob(Class jobClass, Map params) throws IllegalArgumentException {
        // Insert a new uuid into the jobs params so we can find it again
        Map updatedParams = (Map) params?.clone() ?: [:]
        String newJobUuid = UUID.randomUUID().toString()
        updatedParams.uuid = newJobUuid

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
        else {
            // Could not submit the job
            throw new IllegalArgumentException("Specified jobClass parameter doesn't match a known Job class")
        }
        return newJobUuid
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
        JobExecutionContext context = null
        while (remainingAttempts--) {
            context = recentlyCompletedJobs[uuid]
            if (context) {
                break
            }
            Thread.sleep(1000)
        }
        log.info("Waited for jobs.uuid ${uuid} and got ${context}")
        return context
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
            if (runningJobs.size() == 0) {
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
}
