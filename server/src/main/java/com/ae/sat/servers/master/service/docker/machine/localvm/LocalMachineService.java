package com.ae.sat.servers.master.service.docker.machine.localvm;

import com.ae.sat.servers.master.service.docker.MyDockerClientConfigBuilder;
import com.ae.sat.servers.master.service.docker.machine.Machine;
import com.ae.sat.servers.master.service.docker.machine.utils.Utils;
import com.ae.sat.servers.master.service.docker.machine.MachineService;
import com.github.dockerjava.core.DockerClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Created by ae on 6-2-16.
 */

public class LocalMachineService extends MachineService {

  public LocalMachineService() {
    setHome(System.getProperty("user.home") + "/.docker/machine");
  }

  @Override
  protected String getMachineCreationCommand(String machineName) {
    StringBuilder command = new StringBuilder();
    command.append(getDockerMachineCommand());
    command.append(String.format(" --tls-ca-cert=%s/ca.pem", getCertificateDir()));
    command.append(String.format(" --tls-ca-key=%s/ca-key.pem", getCertificateDir()));
    command.append(String.format(" --tls-client-cert=%s/cert.pem", getCertificateDir()));
    command.append(String.format(" --tls-client-key=%s/key.pem", getCertificateDir()));
    command.append(String.format(" create -d virtualbox %s", machineName));
    return command.toString();
  }


}

