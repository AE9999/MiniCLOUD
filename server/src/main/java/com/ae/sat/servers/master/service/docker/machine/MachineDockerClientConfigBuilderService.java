package com.ae.docker.machine;

import com.ae.docker.MyDockerClientConfigBuilder;

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
