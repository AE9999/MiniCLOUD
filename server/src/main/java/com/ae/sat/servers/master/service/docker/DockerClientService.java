package com.ae.sat.servers.master.service.docker;

import com.github.dockerjava.api.DockerClient;
import org.apache.commons.lang.NotImplementedException;

import java.util.UUID;

/**
 * Created by ae on 4-2-16.
 */
public interface DockerClientService {

  UUID id = UUID.randomUUID();

  default  String id() { return id.toString(); }

  String ip();

  int port();

  DockerClient getNewDockerClient();

  void close();
}
