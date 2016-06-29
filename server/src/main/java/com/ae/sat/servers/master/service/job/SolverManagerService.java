package com.ae.sat.servers.master.service.job;

import com.ae.sat.model.SolverAssignment;
import com.ae.sat.servers.master.service.docker.DockerClientService;
import com.ae.sat.servers.master.service.docker.DockerClientServiceFactory;
import com.ae.sat.servers.master.service.rabbitmq.RabbitMQService;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by ae on 28-5-16.
 */

@Component
public class SolverManagerService {

    @Autowired
    private DockerClientServiceFactory dockerServiceFactory;

    @Value("${workerImageName}")
    private String workerImageName;

    @Value("${dockerMachineTimeoutInMinutes}")
    private long dockerMachineTimeoutInMinutes;

    public SolverManager getSolverManager(String informationChannelName,
                                          String unitClauseChannelName,
                                          SolverAssignment solverAssignment,
                                          ConnectionFactory connectionFactory)
                         throws InterruptedException,
                                ExecutionException,
                                TimeoutException,
                                IOException {
        SolverManager solverManager = new SolverManager();
        solverManager.setInformationChannelName(informationChannelName);
        solverManager.setUnitClauseChannelName(unitClauseChannelName);
        solverManager.setConnectionFactory(connectionFactory);
        solverManager.setSolverAssignment(solverAssignment);
        solverManager.setDockerClientService(dockerServiceFactory.getDockerClientService());
        solverManager.setDockerMachineTimeoutInMinutes(dockerMachineTimeoutInMinutes);
        solverManager.setWorkerImageName(workerImageName);
        return solverManager;
    }

}
