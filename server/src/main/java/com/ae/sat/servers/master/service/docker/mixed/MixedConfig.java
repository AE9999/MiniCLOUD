package com.ae.docker.mixed;

import com.ae.docker.DockerClientServiceFactory;
import com.ae.docker.local.LocalDockerClientServiceFactory;
import com.ae.docker.machine.MachineDockerClientConfigBuilderService;
import com.ae.docker.machine.MachineDockerClientServiceFactory;
import com.ae.docker.machine.localvm.LocalVMMachineService;
import com.ae.docker.machine.oceanvm.OceanVMMachineService;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by ae on 7-9-16.
 */

@Profile("mixedDocker")
@PropertySource({
        "classpath:dockerGeneral.properties",
        "classpath:dockerLocal.properties",
        "classpath:dockerLocalvm.properties",
        "classpath:dockerOceanvm.properties",
        "classpath:dockerMixed.properties"
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
