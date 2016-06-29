package com.ae.sat.servers.master.service.docker.machine;

import com.ae.sat.servers.master.service.docker.MyDockerClientConfigBuilder;
import com.github.dockerjava.core.DockerClientConfig;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by ae on 27-5-16.
 */
public final class Machine {

    private String name;

    private MyDockerClientConfigBuilder config;

    public Machine(String name, MyDockerClientConfigBuilder config) {
        this.name = name;
        this.config  = config;
    }

    public String getName() {
        return name;
    }

    public MyDockerClientConfigBuilder getConfig() {
        return config;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Machine machine = (Machine) o;

        if (name != null ? !name.equals(machine.name) : machine.name != null) return false;
        return config != null ? config.equals(machine.config) : machine.config == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (config != null ? config.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Machine{" +
                "name='" + name + '\'' +
                ", config=" + config +
                '}';
    }
}
