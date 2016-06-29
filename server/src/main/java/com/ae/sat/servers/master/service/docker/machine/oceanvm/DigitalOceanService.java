package com.ae.sat.servers.master.service.docker.machine.oceanvm;

import com.ae.sat.servers.master.service.docker.machine.Machine;
import com.ae.sat.servers.master.service.docker.machine.MachineDockerClientServiceFactory;
import com.ae.sat.servers.master.service.docker.machine.utils.Utils;
import com.ae.sat.servers.master.service.docker.machine.MachineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Created by ae on 13-2-16.
 */
public class DigitalOceanService extends MachineService {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Value("${oceanToken}")
  private String oceanToken;

  @Value("${oceanImage}")
  private String oceanImage;

  @Value("${oceanImageSize}")
  private String oceanImageSize;

  public DigitalOceanService() {
    setHome(System.getProperty("user.home") + "/.docker/ocean");
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
    command.append(String.format(" --digitalocean-image=%s", oceanImage));
    command.append(String.format(" --digitalocean-size=%s", oceanImageSize));
    command.append(String.format(" --digitalocean-access-token=%s", oceanToken));
    command.append(String.format(" %s", machineName));
    return command.toString();
  }
}
