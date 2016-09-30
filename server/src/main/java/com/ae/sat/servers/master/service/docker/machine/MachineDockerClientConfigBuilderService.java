package com.ae.sat.servers.master.service.docker.machine;


import com.ae.sat.servers.master.service.docker.MyDockerClientConfigBuilder;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Created by ae on 9-9-16.
 */
public interface MachineDockerClientConfigBuilderService {

    Future<MyDockerClientConfigBuilder> getDockerClientConfigBuilder(String name, Runnable r);

    void destroyMachineDockerClientConfigBuilderByName(String name) throws IOException;

    int getMaxInpactForSingeMachine();

    int getMaximumNrOfOrderableMachines();
}
