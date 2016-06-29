package com.ae.sat.servers.master.service.docker.machine;

import com.ae.sat.servers.master.service.docker.DockerClientService;
import com.ae.sat.servers.master.service.docker.DockerClientServiceFactory;
import com.ae.sat.servers.master.service.docker.MyDockerClientConfigBuilder;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by ae on 28-5-16.
 */
@Component
public class MachineDockerClientServiceFactory implements DockerClientServiceFactory {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private MachineService machineService;

    @Override
    public DockerClientService getDockerClientService() throws IOException {

        final Future<Machine> machineFuture = machineService.getMachine();

        return new DockerClientService() {

            private Machine machine_;
            private MyDockerClientConfigBuilder config_;
            private DockerClientBuilder dockerClientBuilder_;
            private DockerClientConfig.DockerClientConfigBuilder builder_;

            private Machine machine() {
                if (machine_ == null) {
                    try {
                        machine_ = machineFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                       log.error("Could not obtain machine", e);
                        throw new RuntimeException(e);
                    }
                }
                return machine_;
            }

            private MyDockerClientConfigBuilder config() {
                if (config_ == null) {
                    config_ = machine().getConfig();
                }
                return config_;
            }

            private DockerClientConfig.DockerClientConfigBuilder builder() {
                if (builder_ == null) {
                    builder_ = config().getDockerClientConfigBuilder();
                }
                return builder_;
            }

            private DockerClientBuilder dockerClientBuilder() {
                if (dockerClientBuilder_ == null) {
                    dockerClientBuilder_ = DockerClientBuilder.getInstance(builder().build());
                }
                return dockerClientBuilder_;
            }


            @Override
            public String ip() {
                return config().getIp();
            }

            @Override
            public int port() {
                return config().getPort();
            }

            @Override
            public DockerClient getNewDockerClient() {
                return dockerClientBuilder().build();
            }

            @Override
            public void close() {
                try {
                    machineService.deleteMachine(machine().getName());
                } catch (IOException e) {
                    log.error("Could not delete machine", e);
                }
            }

            @Override
            public String toString() {
                return String.format(" { ip: %s, port:%s } ", ip(), port());
            }
        };
    }
}
