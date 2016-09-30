package com.ae.sat.servers.master.service.docker.machine.localvm;

import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by ae on 3-7-16.
 */

@Profile("localvmDocker")
@PropertySource({
        "classpath:docker/dockerGeneral.properties",
        "classpath:docker/dockerLocalvm.properties"
})
public class LocalVMConfig {}
