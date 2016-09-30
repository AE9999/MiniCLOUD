package com.ae.sat.servers.master.service.docker.local;


import com.ae.sat.servers.master.service.docker.AbstractDockerClientServiceFactoryImpl;
import com.ae.sat.servers.master.service.docker.MyDockerClientConfigBuilder;
import com.ae.sat.servers.master.service.docker.Task;
import com.github.dockerjava.core.DockerClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

/**
 * Created by ae on 7-2-16.
 */

@Profile("localDocker")
@Component
public class LocalDockerClientServiceFactory extends AbstractDockerClientServiceFactoryImpl {

  @Value("${localDockerIP}")
  private String localDockerIP;

  @Value("${localDockerPort}")
  private int localDockerPort;

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  protected Future<MyDockerClientConfigBuilder> doGetNewDockerService(Task task) {
    String dockerUrl = String.format("tcp://%s:%s", localDockerIP, localDockerPort);

    DockerClientConfig.DockerClientConfigBuilder dockerConfig;
    dockerConfig = DockerClientConfig.createDefaultConfigBuilder()
            .withDockerTlsVerify(false)
            .withDockerHost(dockerUrl);

    MyDockerClientConfigBuilder myConfig = new MyDockerClientConfigBuilder(dockerConfig);
    myConfig.setIp(localDockerIP);
    myConfig.setPort(localDockerPort);

    log.info(String.format("Creating a config for %s ..", task));
    return new AsyncResult<>(myConfig);
  }

  @Override
  protected void close(Task task) {
    log.info(String.format("Closing task %s ..", task));
    // IGNORE
  }

  @Override
  protected int getMaxInpactForSingeMachine() {
    return Integer.MAX_VALUE;
  }

}
