package com.ae.sat.servers.master.service.job;

import com.ae.sat.servers.master.service.docker.DockerClientService;
import com.ae.sat.model.SolverAssignment;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;

import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.rabbitmq.client.*;
import org.nustaq.serialization.FSTObjectOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by ae on 22-5-16.
 */
public class SolverManager implements Runnable {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private long dockerMachineTimeoutInMinutes;
    private String unitClauseChannelName;
    private String informationChannelName;
    private String workerImageName;
    private DockerClientService dockerClientService;
    private DockerClient dockerClient;
    private CreateContainerResponse container;
    private ConnectionFactory connectionFactory;
    private SolverAssignment solverAssignment;
    private Job job;
    private boolean killed = false;

    public String getUnitClauseChannelName() {
        return unitClauseChannelName;
    }

    public void setUnitClauseChannelName(String unitClauseChannelName) {
        this.unitClauseChannelName = unitClauseChannelName;
    }

    public String getInformationChannelName() {
        return informationChannelName;
    }

    public void setInformationChannelName(String informationChannelName) {
        this.informationChannelName = informationChannelName;
    }

    public String getWorkerImageName() {
        return workerImageName;
    }

    public void setWorkerImageName(String workerImageName) {
        this.workerImageName = workerImageName;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public SolverAssignment getSolverAssignment() {
        return solverAssignment;
    }

    public void setSolverAssignment(SolverAssignment solverAssignment) {
        this.solverAssignment = solverAssignment;
    }

    public long getDockerMachineTimeoutInMinutes() {
        return dockerMachineTimeoutInMinutes;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public void setDockerMachineTimeoutInMinutes(long dockerMachineTimeoutInMinutes) {
        this.dockerMachineTimeoutInMinutes = dockerMachineTimeoutInMinutes;
    }

    protected synchronized void stop() {
        if (!killed) {
            try {
                dockerClient.stopContainerCmd(container.getId()).exec();
            } catch (NotModifiedException e) {
                log.info("Container already stopped ..");
            }
            dockerClient.removeContainerCmd(container.getId());
            dockerClientService.close(); // Should be ignored by local service
        }
        killed = true;
    }

    private String getStartCommand(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("java -jar ");
        sb.append("-DfileToSolve=" + name + " ");
        sb.append("-DinformationChannelName=" + informationChannelName + " ");
        sb.append("-DunitClauseChannelName=" + unitClauseChannelName + " ");
        sb.append("-DrabbitMQHost=" + connectionFactory.getHost() + " ");
        sb.append("-DrabbitMQPort=" + connectionFactory.getPort() + " ");
        sb.append("worker.jar ");
        return sb.toString();
    }

    @Override
    public void run() {
        try {
            log.info("Starting solving job ..");
            log.info(String.format("Got My Client Service %s ..", dockerClientService.id()));
            dockerClient = dockerClientService.getNewDockerClient();
            log.info(String.format("Got My Client %s ..", dockerClient));

            String tagName = String.format("%s:latest", workerImageName);
            boolean needsImage = dockerClient.listImagesCmd()
                                             .exec()
                                             .stream()
                                             .map(f -> Arrays.asList(f.getRepoTags()))
                                             .noneMatch(f -> f.contains(tagName));

            if (needsImage) {
                log.info("Pulling SAT worker, might take a while ..");
                dockerClient.pullImageCmd(workerImageName)
                            .exec(new PullImageResultCallback())
                            .awaitSuccess();
                log.info("Done with pulling creating the container now ..");
            }

            File assignmentFile = File.createTempFile("assignment", "data");
            assignmentFile.createNewFile();
            FileOutputStream os = new FileOutputStream(assignmentFile);
            FSTObjectOutput out = new FSTObjectOutput(os);
            out.writeObject( solverAssignment, SolverAssignment.class);
            out.close();
            os.close();

            String startCommand = getStartCommand(assignmentFile.getName());
            String[] command = new String[] { "sh", "-c", startCommand };
            container = dockerClient.createContainerCmd(workerImageName)
                                    .withNetworkMode("host") // Makes running locally easier.
                                    .withCmd(command)
                                    .exec();

            dockerClient.copyArchiveToContainerCmd(container.getId())
                        .withHostResource(assignmentFile.getAbsolutePath())
                        .withRemotePath("/").exec();
            assignmentFile.delete();
            dockerClient.startContainerCmd(container.getId()).exec();
        } catch (IOException e) {
            log.error("We have a problem with", e);
            job.recieveJobFailure();
        }
    }

    public void setDockerClientService(DockerClientService dockerClientService) {
        this.dockerClientService = dockerClientService;
    }
}
