package com.ae.sat.servers.master.service.job;

import com.ae.sat.message.Stats;
import com.ae.sat.model.Answer;
import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by ae on 28-5-16.
 */
public class Job {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private PlannedJob plannedJob;
    private List<SolverManager> solverManagers;
    private List<Answer> answers = new ArrayList<>();
    private JobListener jobListener;
    private JobStatus status;
    private Connection connection;

    public PlannedJob getPlannedJob() {
        return plannedJob;
    }

    public void setPlannedJob(PlannedJob plannedJob) {
        this.plannedJob = plannedJob;
    }

    public List<SolverManager> getSolverManagers() {
        return solverManagers;
    }

    public void setSolverManagers(List<SolverManager> solverManagers) {
        this.solverManagers = solverManagers;
    }

    public JobListener getJobListener() {
        return jobListener;
    }

    public void setJobListener(JobListener jobListener) {
        this.jobListener = jobListener;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    private void stop() {
        log.info(String.format("Stopping %s ..", plannedJob.getId()));
        jobListener.stop();
        solverManagers.stream().forEach(SolverManager::stop);
        try {
            connection.close();
        } catch (IOException e) {
            log.error("Could not close connection ", e);
        }
    }

    protected void recieveAnswer(Answer answer) {
        log.info(String.format("Recieved answer: %s for %s ..", answer, plannedJob.getId()));
        answers.add(answer);
        if (answer == Answer.SAT
            || answers.size() == plannedJob.getSolverAssignments().size()) {
            setStatus(JobStatus.DONE);
            stop();
        }
    }

    protected void recieveStatistics(Stats stats) {
        log.info(String.format("Recieved stats: %s for %s ..", stats.toString(), plannedJob.getId()));
    }

    protected void recieveJobFailure() {
        log.info(String.format("Recieved an exception for: %s ..", plannedJob.getId()));
        setStatus(JobStatus.FAILED);
        stop();
    }

    public void run(Executor taskExecutor) {
        jobListener.setJob(this);
        taskExecutor.execute(jobListener);
        solverManagers.forEach(taskExecutor::execute);
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }
}
