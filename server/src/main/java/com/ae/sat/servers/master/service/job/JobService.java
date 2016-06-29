package com.ae.sat.servers.master.service.job;

import com.ae.sat.message.Stats;
import com.ae.sat.model.Answer;
import com.ae.sat.model.SolverAssignment;
import com.ae.sat.servers.master.service.rabbitmq.RabbitMQService;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by ae on 21-5-16.
 */
@Component
public class JobService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private SolverManagerService solverManagerService;

    @Autowired
    private RabbitMQService rabbitMQService;

    public Job getJob(PlannedJob plannedJob)
               throws IOException,
                      TimeoutException,
                      ExecutionException,
                      InterruptedException {

        Job job = new Job();
        job.setPlannedJob(plannedJob);
        String unitClauseChannelName  = "UNIT_CLAUSE_CHANNEL-" + plannedJob.getId();
        String informationChannelName = "INFORMATION_CHANNEL-" + plannedJob.getId();

        log.info("Making rabbit mq connection ..");
        ConnectionFactory connectionFactory = rabbitMQService.getConnectionFactory();
        Connection connection = connectionFactory.newConnection();
        job.setConnection(connection);

        Channel channel = connection.createChannel();

        JobListener jobListener = new JobListener(informationChannelName, channel);
        job.setJobListener(jobListener);

        List<SolverManager> solverManagers = new ArrayList<>();
        for (SolverAssignment solverAssignment : plannedJob.getSolverAssignments()) {
            SolverManager solverManager;
            solverManager = solverManagerService.getSolverManager(informationChannelName,
                                                                  unitClauseChannelName,
                                                                  solverAssignment,
                                                                  connectionFactory);
            solverManager.setJob(job);
            solverManagers.add(solverManager);
        }
        job.setSolverManagers(solverManagers);
        return job;
    }

}
