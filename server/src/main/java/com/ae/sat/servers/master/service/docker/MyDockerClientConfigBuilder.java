package com.ae.docker;

import com.github.dockerjava.core.DockerClientConfig;

/**
 * Created by ae on 29-5-16.
 */
public class MyDockerClientConfigBuilder {

    private DockerClientConfig.DockerClientConfigBuilder dockerClientConfigBuilder;

    public MyDockerClientConfigBuilder(DockerClientConfig.DockerClientConfigBuilder dockerClientConfigBuilder) {
        this.dockerClientConfigBuilder = dockerClientConfigBuilder;
    }

    private String ip;

    private int port;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public DockerClientConfig.DockerClientConfigBuilder getDockerClientConfigBuilder() {
        return dockerClientConfigBuilder;
    }

    public void setDockerClientConfigBuilder(DockerClientConfig.DockerClientConfigBuilder dockerClientConfigBuilder) {
        this.dockerClientConfigBuilder = dockerClientConfigBuilder;
    }
}
