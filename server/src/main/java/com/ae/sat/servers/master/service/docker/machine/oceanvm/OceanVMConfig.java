package com.ae.sat.servers.master.service.docker.machine.oceanvm;

import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by ae on 3-7-16.
 */

@Profile("oceanvmDocker")
@PropertySource({
        "classpath:docker/dockerGeneral.properties",
        "classpath:docker/dockerOceanvm.properties"
})
public class OceanVMConfig {}
