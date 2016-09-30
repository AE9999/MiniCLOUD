package com.ae.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by ae on 19-2-16.
 */
public abstract class AbstractDockerClientServiceFactoryImpl implements DockerClientServiceFactory {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Value("${dockerUsername}")
  private String dockerUsername;

  @Value("${dockerPassword}")
  private String dockerPassword;

  @Value("${dockerEmail}")
  private String dockerEmail;

  @Value("${dockerMachineTimeoutInMinutes}")
  private long dockerMachineTimeoutInMinutes;

  @Value("${dockerApiVersion}") //
  private String dockerApiVersion;

  protected void setDockerUserName(String userName) {
    this.dockerUsername = userName;
  }

  protected void setDockerPassword(String dockerPassword) {
    this.dockerPassword = dockerPassword;
  }

  protected abstract Future<MyDockerClientConfigBuilder> doGetNewDockerService(Task task)
            throws ExecutionException;

  protected abstract void close(Task task);

  protected abstract int getMaxInpactForSingeMachine();

  @Async
  @Override
  public Future<DockerClientService> getDockerClientService(int inpact)
          throws ExecutionException,
                 CannotProvideException,
                 InterruptedException {

    if (inpact > getMaxInpactForSingeMachine()) {
      String m = String.format("Task with inpact %s is too large for " +
                               "underlying machines with max inpact %d ..",
                               inpact,
                               getMaxInpactForSingeMachine() );
      throw new CannotProvideException(m);
    }

    final Task task = new Task(inpact);

    log.info(String.format("Obtaining builder promise for %s", task.getId()));
    Future<MyDockerClientConfigBuilder> builderPromise;
    builderPromise = doGetNewDockerService(task);
    log.info(String.format("Recieved builder promise for %s", task.getId()));

    MyDockerClientConfigBuilder config;
    config = builderPromise.get();
    DockerClientConfig.DockerClientConfigBuilder builder;
    builder = config.getDockerClientConfigBuilder();
    if (!StringUtils.isEmpty(dockerApiVersion)) {
      builder.withApiVersion(dockerApiVersion); // Terrible hack!
    }
    final boolean hasAuth = !StringUtils.isEmpty(dockerUsername)
                             && !("\"\"".equals(dockerUsername));
    if (hasAuth) {
      log.info(String.format("Logging into docker with %s provided ..",
              dockerUsername));
      builder = builder.withRegistryEmail(dockerEmail)
              .withRegistryUsername(dockerUsername)
              .withRegistryPassword(dockerPassword);
    } else {
      log.info("No docker username provided ..");
    }

    final DockerClientBuilder dockerClientBuilder;
    dockerClientBuilder = DockerClientBuilder.getInstance(builder.build());
    final AbstractDockerClientServiceFactoryImpl selfReference = this;

    DockerClientService dockerClientService = new DockerClientService() {

      @Override
      public String ip() {
        return config.getIp();
      }

      @Override
      public int port() {
        return config.getPort();
      }

      @Override
      public SafeDockerClient getNewDockerClient() {
        DockerClient dockerClient = dockerClientBuilder.build();
        if (hasAuth) {
          dockerClient.authCmd().exec();
        }
        return new SafeDockerClient(dockerClient);
      }

      @Override
      public void close() {
        logger.info(String.format("Closing DockerClientService %s ..", this));
        selfReference.close(task);
      }

      @Override
      public String toString() {
        return String.format(" { id: %s, ip: %s, port:%s } ", id, ip(), port());
      }

    };

    Future<DockerClientService> promise = new AsyncResult<>(dockerClientService);
    log.info(String.format("Returning promise %s, for task %s", promise, task.getId()));
    return promise;
  }

}
