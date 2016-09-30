package com.ae.sat.servers.master.service.docker.machine.localvm;


import com.ae.sat.servers.master.service.docker.machine.DockerMachineService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Created by ae on 6-2-16.
 */

// Needs  5.1.2 r108956 to work. otherwise docker-machine call will end with non zero code.

@Profile("localvmDocker")
@Component
public class LocalVMMachineService extends DockerMachineService {

  @Value("${localVMMaximumNrOfOrderableMachines}")
  private int localVMMaximumNrOfOrderableMachines;

  @Value("${localVMCapacity}")
  private int localVMCapacity;

  @Override
  public int getMaxInpactForSingeMachine() {
    return localVMCapacity;
  }

  @Override
  public int getMaximumNrOfOrderableMachines() {
    return localVMMaximumNrOfOrderableMachines;
  }

  @Override
  protected String setupHome() throws IOException {
    return System.getProperty("user.home") +
           File.separator +
            ".docker" +
            File.separator +
            "machine";
  }

  @Override
  protected String getMachineCreationCommand(String machineName) {
    StringBuilder command = new StringBuilder();
    command.append(getDockerMachineCommand());
    command.append(String.format(" --tls-ca-cert=%s/ca.pem", getCertificateDir()));
    command.append(String.format(" --tls-ca-key=%s/ca-key.pem", getCertificateDir()));
    command.append(String.format(" --tls-client-cert=%s/cert.pem", getCertificateDir()));
    command.append(String.format(" --tls-client-key=%s/key.pem", getCertificateDir()));
    command.append(String.format(" create -d virtualbox"));
    //
    // As suggested by https://github.com/docker/machine/issues/1521
    // More can be found at https://www.virtualbox.org/ticket/14040
    //
    // command.append(String.format(" --virtualbox-hostonly-cidr 192.168.56.1/24"));
    command.append(String.format(" %s", machineName));
    return command.toString();
  }


}
