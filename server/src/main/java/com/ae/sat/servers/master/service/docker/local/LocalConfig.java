package com.ae.sat.servers.master.service.docker.local;

import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by ae on 3-7-16.
 */

@Profile("localDocker")
@PropertySource({
        "classpath:docker/dockerGeneral.properties",
        "classpath:docker/dockerLocal.properties"
                })
public class LocalConfig {

}
