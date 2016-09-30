package com.ae.sat.servers.master.service.docker.machine;

import com.ae.sat.servers.master.service.docker.MyDockerClientConfigBuilder;
import com.ae.sat.servers.master.service.sh.ShRunner;
import com.github.dockerjava.core.DockerClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by ae on 14-2-16.
 */

// Example output
//NAME     ACTIVE   DRIVER       STATE     URL                         SWARM   DOCKER   ERRORS
//agent1   -        virtualbox   Running   tcp://192.168.99.100:2376           v1.9.1

public abstract class DockerMachineService implements MachineDockerClientConfigBuilderService {

  private final Logger log = LoggerFactory.getLogger(getClass());
  protected String home;
  private String certificateDir = null;

  @Autowired
  private CertificateService certificateService;

  @Autowired
  private ShRunner shRunner;

  @Value("${machineDockerPort}")
  protected int machineDockerPort;

  @PostConstruct
  protected void init() {
    try {
      this.home = setupHome();
      log.info(String.format("Initialized %s as home ..", this.home));
      certificateDir = certificateService.getCertificatesDir(this.home);
      log.info(String.format("Initialized %s as certificate dir ..", this.home));
    } catch (IOException e) {
      log.error("Error during setup", e);
    }
  }

  @PreDestroy
  protected void destroy() {
    try {
      getRunningDockerMachinesNames().parallelStream()
                                     .forEach(f -> {
                                          try {
                                            destroyMachineDockerClientConfigBuilderByName(f);
                                          } catch(IOException e)  {
                                            log.error(String.format("Could not delete %s", f), e);
                                          }
                                     });
    } catch (IOException e) {
      log.error("Could not clean up machines ..");
    }
  }

  protected String setupHome() throws IOException {
    File stuff = File.createTempFile("machineDir", "machineDir");
    stuff.delete();
    stuff.mkdir();
    log.info(String.format("Setting %s as home ..", stuff.getAbsolutePath()));
    if (stuff.exists() && !stuff.isDirectory()) {
      throw new IllegalStateException("Expected home to be empty or be directory");
    }
    if (!stuff.canWrite()) {
      throw new IllegalStateException("Expected home to be writable");
    }
    return stuff.getAbsolutePath();
  }

  protected List<String> getRunningDockerMachinesNames() throws IOException {
    String command = String.format("%s ls -q", getDockerMachineCommand());
    List<String> output = shRunner.executeCommandAsShArgument(command);
    return output;
  }

  protected String machine2ip(String machineName) throws IOException {
    String command = String.format("%s ip %s", getDockerMachineCommand(),  machineName);
    List<String> output = shRunner.executeCommandAsShArgument(command);
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

    List<String> results = shRunner.executeCommandAsShArgument(certCommand.toString());
    String certhPath = results.get(0);
    certhPath = certhPath.replace("export DOCKER_CERT_PATH=\"", "");
    certhPath = certhPath.replace("\"", "");
    return certhPath;
  }

  protected String getDockerMachineCommand() {
    return String.format("docker-machine -s %s", home);
  }

  protected String getCertificateDir() {
    return certificateDir;
  }

  protected abstract String getMachineCreationCommand(String machineName);

  private MyDockerClientConfigBuilder doGetDockerClientConfigBuilder(String name) throws IOException {
    log.info(String.format("Creating a new machine: %s", name));

    try {
      shRunner.executeCommandAsShArgument(getMachineCreationCommand(name));
    } catch (IOException e) {
      log.warn("Machine Creation Failed, deleting the remainders ..");
      try {
        shRunner.executeCommandAsShArgument(getMachineCreationCommand(name));
      } catch (IOException e2) {
        log.warn("Deletion failed ..", e2);
        destroyMachineDockerClientConfigBuilderByName(name);
      }
      throw e;
    }

    final String ip = machine2ip(name);
    final String dockerUrl = String.format("tcp://%s:%s", ip, machineDockerPort);
    final String certhPath = machine2certPath(name);

    DockerClientConfig.DockerClientConfigBuilder dockerConfig;
    dockerConfig = DockerClientConfig.createDefaultConfigBuilder()
            .withDockerCertPath(certhPath)
            .withDockerTlsVerify(true)
            .withDockerHost(dockerUrl);

    MyDockerClientConfigBuilder myConfig;
    myConfig = new MyDockerClientConfigBuilder(dockerConfig);
    myConfig.setIp(ip);
    myConfig.setPort(machineDockerPort);
    return myConfig;
  }

  @Async
  @Override
  public Future<MyDockerClientConfigBuilder> getDockerClientConfigBuilder(String name, Runnable cleanup) {
      MyDockerClientConfigBuilder myConfig = null;
      try {
        myConfig = doGetDockerClientConfigBuilder(name);
      } catch (IOException e) {
        log.error("Could not obtain machine ..", e);
        cleanup.run();
      }

      AsyncResult<MyDockerClientConfigBuilder> rvalue;
      rvalue = new AsyncResult<>(myConfig);
      return rvalue;
  }

  @Override
  public void destroyMachineDockerClientConfigBuilderByName(String name) throws IOException {
    String command;
    try {
      // Try to do it correctly ..
      command = String.format("%s stop %s",  getDockerMachineCommand(), name);
      shRunner.executeCommandAsShArgument(command);
      command = String.format("%s rm -y %s",  getDockerMachineCommand(), name);
      shRunner.executeCommandAsShArgument(command);
    } catch (IOException e) {
      // Try to do it by force ..
      command = String.format("%s rm -y -f %s",  getDockerMachineCommand(), name);
      shRunner.executeCommandAsShArgument(command);
    }
  }

  @Override
  public abstract int getMaxInpactForSingeMachine();

}
