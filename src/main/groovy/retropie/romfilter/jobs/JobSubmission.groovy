package retropie.romfilter.jobs

import org.joda.time.DateTime
import org.quartz.JobExecutionContext

/**
 * Class to track the states a job sumbissions goes through. This class supports
 * JobSubmissionService.
 */
class JobSubmission {

    /**
     * The states a job can be in.
     */
    enum State {
        SUBMITTED,
        RUNNING,
        VETOED,
        COMPLETE,
    }

    /**
     * A combination of State and DateTime. Used to track the states a job
     * goes through.
     */
    class StateDateTime {
        /**
         * The state at a specific time.
         */
        State state

        /**
         * The specific time.
         */
        DateTime dateTime

        /**
         * Constructor.
         *
         * @param state the state
         */
        StateDateTime(State state) {
            this.state = state
            dateTime = new DateTime()
        }
    }

    /**
     * Identifier of JobSubmission.
     */
    String uuid

    /**
     * Job's execution context.
     * This will be null for just submitted jobs. When the job moves to
     * RUNNING it will get a context.
     */
    JobExecutionContext context

    /**
     * The states the job as been in. The last state is the most recent.
     */
    List<StateDateTime> states

    JobSubmission() {
        states = [new StateDateTime(State.SUBMITTED)]
        uuid = UUID.randomUUID().toString()
    }

    /**
     * Add a new state to a job.
     * @param state
     */
    void addState(State state) {
        states << new StateDateTime(state)
    }

    /**
     * Get the job's current StateDateTime (the state of the job and when it changed to that state).
     */
    StateDateTime currentStateDateTime() {
        return states[-1]
    }

    /**
     * Get the job's current State.
     */
    State getCurrentState() {
        // We always have at least one state (see constructor). Grab the last one.
        return states[-1].state
    }
}
