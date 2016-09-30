package com.ae.sat.servers.master.service.docker.mixed;

import com.ae.sat.servers.master.service.docker.DockerClientServiceFactory;
import com.ae.sat.servers.master.service.docker.local.LocalDockerClientServiceFactory;
import com.ae.sat.servers.master.service.docker.machine.MachineDockerClientConfigBuilderService;
import com.ae.sat.servers.master.service.docker.machine.MachineDockerClientServiceFactory;
import com.ae.sat.servers.master.service.docker.machine.localvm.LocalVMMachineService;
import com.ae.sat.servers.master.service.docker.machine.oceanvm.OceanVMMachineService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by ae on 7-9-16.
 */

@Profile("mixedDocker")
@PropertySource({
        "classpath:docker/dockerGeneral.properties",
        "classpath:docker/dockerLocal.properties",
        "classpath:docker/dockerLocalvm.properties",
        "classpath:docker/dockerOceanvm.properties",
        "classpath:docker/dockerMixed.properties"
})
public class MixedConfig {

    @Value("${useOcean}")
    private boolean useOcean;

    @Bean(name = "localDockerClientServiceFactory")
    public DockerClientServiceFactory localDockerClientServiceFactory() {
        return new LocalDockerClientServiceFactory();
    }

    @Bean
    public MachineDockerClientConfigBuilderService machineService() {
        return useOcean ? new OceanVMMachineService()
                        : new LocalVMMachineService();
    }

    @Bean(name = "machineDockerClientServiceFactory")
    public DockerClientServiceFactory machineDockerClientServiceFactory() {
        return new MachineDockerClientServiceFactory();
    }

}
