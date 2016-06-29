package com.ae.sat.servers.master.service.docker.machine;

import com.ae.sat.servers.master.service.docker.DockerClientServiceFactory;
import com.ae.sat.servers.master.service.docker.MyDockerClientConfigBuilder;
import com.ae.sat.servers.master.service.docker.machine.utils.Utils;
import com.github.dockerjava.core.DockerClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;

/**
 * Created by ae on 14-2-16.
 */

// Example output
//NAME     ACTIVE   DRIVER       STATE     URL                         SWARM   DOCKER   ERRORS
//agent1   -        virtualbox   Running   tcp://192.168.99.100:2376           v1.9.1

public abstract class MachineService  {

  private final Logger log = LoggerFactory.getLogger(getClass());
  protected String home;
  private String certificateDir = null;

  @Value("${machineDockerPort}")
  protected int machineDockerPort;

  @PostConstruct
  protected void init() {
    try {
      setupHome(home);
      setupCertificateDir();
    } catch (IOException e) {
      log.error("Error during setup", e);
    }
  }

  protected void setHome(String home) {
    this.home = home;
  }

  private void setupHome(String myHome) {
    myHome = myHome.replace("~", System.getProperty("user.home"));
    log.info(String.format("Setting %s as home (%s)",
            myHome,
            System.getProperty("user.home")));
    File stuff = new File(myHome);
    if (!stuff.exists()) {
      stuff.mkdir();
    }
    if (stuff.exists() && !stuff.isDirectory()) {
      throw new IllegalStateException("Expected home to be empty or be directory");
    }
    if (!stuff.canWrite()) {
      throw new IllegalStateException("Expected home to be writable");
    }
    this.home = myHome;
  }

  private void setupCertificateDir() throws IOException {
    log.warn("This is a hack it should not go into production");
    File tmpDir = File.createTempFile("certificate", "hack");
    tmpDir.delete();
    tmpDir.mkdir();
    //     String fname = path.getAbsolutePath() + File.separatorChar + name;

    Utils.exportFileFromJar("/machine/ca.pem",
            tmpDir.getAbsolutePath() + File.separatorChar + "ca.pem");
    Utils.exportFileFromJar("/machine/ca-key.pem",
            tmpDir.getAbsolutePath() + File.separatorChar + "ca-key.pem");
    Utils.exportFileFromJar("/machine/cert.pem",
            tmpDir.getAbsolutePath() + File.separatorChar + "cert.pem");
    Utils.exportFileFromJar("/machine/key.pem", tmpDir.getAbsolutePath() + File.separatorChar + "key.pem");
    Utils.exportFileFromJar("/machine/server-cert.pem",
            tmpDir.getAbsolutePath() + File.separatorChar + "server-cert.pem");
    Utils.exportFileFromJar("/machine/server-key.pem",
            tmpDir.getAbsolutePath() + File.separatorChar + "server-key.pem");

    certificateDir = tmpDir.getAbsolutePath();
  }

  public void deleteMachine(String machineName) throws IOException {
    String command = String.format("%s rm -y -f %s", getDockerMachineCommand(), machineName);
    Utils.executeCommandAsShArgument(command);
  }

  protected String machine2ip(String machineName) throws IOException {
    String command = String.format("%s ip %s", getDockerMachineCommand(),  machineName);
    List<String> output = Utils.executeCommandAsShArgument(command);
    if (output.size() == 1) { return output.get(0); }
    String m = String.format("Expected to find as single result for machine %s ..", machineName);
    throw new IllegalStateException(m);
  }

  protected String machine2certPath(String machineName) throws IOException {
    StringBuilder certCommand = new StringBuilder();
    certCommand.append(String.format("%s env --shell bash %s",
            getDockerMachineCommand(),
            machineName));
    certCommand.append(" | grep DOCKER_CERT_PATH");

    List<String> results = Utils.executeCommandAsShArgument(certCommand.toString());
    String certhPath = results.get(0);
    certhPath = certhPath.replace("export DOCKER_CERT_PATH=\"", "");
    certhPath = certhPath.replace("\"", "");
    return certhPath;
  }

  public String getDockerMachineCommand() {
    return String.format("docker-machine -s %s", home);
  }

  protected String getCertificateDir() {
    return certificateDir;
  }

  protected Machine createMachine(final String name) throws IOException {

    final String ip = machine2ip(name);
    final String dockerUrl = String.format("tcp://%s:%s", ip, machineDockerPort);
    final String certhPath = machine2certPath(name);

    DockerClientConfig.DockerClientConfigBuilder dockerConfig;
    dockerConfig = DockerClientConfig.createDefaultConfigBuilder()
                                     .withDockerCertPath(certhPath)
                                     .withDockerHost(dockerUrl);

    MyDockerClientConfigBuilder myConfig;
    myConfig = new MyDockerClientConfigBuilder(dockerConfig);
    myConfig.setIp(ip);
    myConfig.setPort(machineDockerPort);

    return new Machine(name, myConfig);
  }

  protected abstract String getMachineCreationCommand(String machineName);

  @Async
  public Future<Machine> getMachine() throws IOException {
    String machineName = UUID.randomUUID().toString().replace("-", "");
    log.info(String.format("Creating a new machine for %s", machineName));
    Utils.executeCommandAsShArgument(getMachineCreationCommand(machineName));
    return new AsyncResult<>(createMachine(machineName));
  }

}
