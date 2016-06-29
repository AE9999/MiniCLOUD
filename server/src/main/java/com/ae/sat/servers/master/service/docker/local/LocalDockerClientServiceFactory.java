package com.ae.sat.servers.master.service.docker.local;

import com.ae.sat.servers.master.service.docker.DockerClientService;
import com.ae.sat.servers.master.service.docker.DockerClientServiceFactory;
import com.ae.sat.servers.master.service.docker.MyDockerClientConfigBuilder;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by ae on 7-2-16.
 */

@Component
public class LocalDockerClientServiceFactory implements DockerClientServiceFactory {

  @Value("${localDockerIP}")
  private String localDockerIP;

  @Value("${localDockerPort}")
  private int localDockerPort;

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  public DockerClientService getDockerClientService() throws IOException {

    String dockerUrl = String.format("tcp://%s:%s", localDockerIP, localDockerPort);

    DockerClientConfig.DockerClientConfigBuilder dockerConfig;
    dockerConfig = DockerClientConfig.createDefaultConfigBuilder()
                                     .withDockerTlsVerify(false)
                                     .withDockerHost(dockerUrl);

    final MyDockerClientConfigBuilder config = new MyDockerClientConfigBuilder(dockerConfig);
    config.setIp(localDockerIP);
    config.setPort(localDockerPort);

    DockerClientConfig.DockerClientConfigBuilder builder;
    builder = config.getDockerClientConfigBuilder();

    final DockerClientBuilder dockerClientBuilder;
    dockerClientBuilder = DockerClientBuilder.getInstance(builder.build());

    return new DockerClientService() {

      @Override
      public String ip() {
        return config.getIp();
      }

      @Override
      public int port() {
        return config.getPort();
      }

      @Override
      public DockerClient getNewDockerClient() {
        return dockerClientBuilder.build();
      }

      @Override
      public void close() {
      }

      @Override
      public String toString() {
        return String.format(" { ip: %s, port:%s } ", ip(), port());
      }
    };

  }
}