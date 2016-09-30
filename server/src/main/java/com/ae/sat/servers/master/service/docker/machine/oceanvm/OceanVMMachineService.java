package com.ae.docker.machine.oceanvm;

import com.ae.docker.machine.DockerMachineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Created by ae on 13-2-16.
 */
@Profile("oceanvmDocker")
@Component
public class OceanVMMachineService extends DockerMachineService {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Value("${oceanToken}")
  private String oceanToken;

//  @Value("${oceanImage}")
//  private String oceanImage;

  @Value("${oceanImageSize}")
  private String oceanImageSize;

  @Value("${oceanVMMaximumNrOfOrderableMachines}")
  private int oceanVMMaximumNrOfOrderableMachines;

  @Value("${oceanVMCapacity}")
  private int oceanVMCapacity;

  @Override
  public int getMaxInpactForSingeMachine() {
    return oceanVMCapacity;
  }

  @Override
  public int getMaximumNrOfOrderableMachines() {
    return oceanVMMaximumNrOfOrderableMachines;
  }

  @Override
  protected String getMachineCreationCommand(String machineName) {
    StringBuilder command = new StringBuilder();
    command.append(getDockerMachineCommand());
    command.append(String.format(" --tls-ca-cert=%s/ca.pem", getCertificateDir()));
    command.append(String.format(" --tls-ca-key=%s/ca-key.pem", getCertificateDir()));
    command.append(String.format(" --tls-client-cert=%s/cert.pem", getCertificateDir()));
    command.append(String.format(" --tls-client-key=%s/key.pem", getCertificateDir()));
    command.append(" create");
    command.append(" --driver digitalocean");
    command.append(String.format(" --digitalocean-size=%s", oceanImageSize));
    command.append(String.format(" --digitalocean-access-token=%s", oceanToken));
    command.append(String.format(" %s", machineName));
    return command.toString();
  }
}
