package com.ae.sat.servers.master.service.docker.mixed;

import com.ae.sat.servers.master.service.docker.DockerClientService;
import com.ae.sat.servers.master.service.docker.DockerClientServiceFactory;
import com.ae.sat.servers.master.service.docker.SafeDockerClient;
import com.ae.sat.servers.master.service.docker.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by ae on 6-9-16.
 */

@Profile("mixedDocker")
@Primary
@Component
public class MixedDockerClientServiceFactory implements DockerClientServiceFactory {

    // All tasks should be refactored to 1
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Qualifier("localDockerClientServiceFactory")
    @Autowired
    private DockerClientServiceFactory localDockerClientServiceFactory;

    @Qualifier("machineDockerClientServiceFactory")
    @Autowired
    private DockerClientServiceFactory machineDockerClientServiceFactory;

    Set<Task> localTasks = new HashSet<>();

    @Value("${maxLocalTasksInpact}")
    private int maxLocalTasksInpact;

    private DockerClientService doGetDockerClientService(int inpact) throws ExecutionException,
                                                                            CannotProvideException,
                                                                            InterruptedException {
        final Task task = new Task(inpact);
        boolean local = false;
        synchronized (localTasks) {
            int potentialInpact = localTasks.stream().mapToInt(Task::getInpact).sum() + task.getInpact();
            if (potentialInpact <= maxLocalTasksInpact) {
                localTasks.add(task);
                local = true;
            }
        }
        Future<DockerClientService> future;
        future = local ? localDockerClientServiceFactory.getDockerClientService(task.getInpact())
                       :  machineDockerClientServiceFactory.getDockerClientService(task.getInpact());
        try {
            final DockerClientService dockerClientService = future.get();
            return new DockerClientService() {

                @Override
                public String ip() {
                    return dockerClientService.ip();
                }

                @Override
                public int port() {
                    return dockerClientService.port();
                }

                @Override
                public SafeDockerClient getNewDockerClient() {
                    return dockerClientService.getNewDockerClient();
                }

                @Override
                public void close() {
                    synchronized (localTasks) {
                        localTasks.remove(task);
                    }
                    dockerClientService.close();
                }
            };
        } catch (InterruptedException | ExecutionException e) {
            log.warn("Could not get machine", e);
            synchronized (localTasks) {
                localTasks.remove(task);
            }
            throw e;
        }
    }

    @Override
    public Future<DockerClientService> getDockerClientService(int inpact) throws ExecutionException, CannotProvideException, InterruptedException {
        return new AsyncResult<>(doGetDockerClientService(inpact));
    }
}
