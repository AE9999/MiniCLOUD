package com.ae.sat.servers.master.service.docker;

import com.ae.sat.servers.master.service.docker.local.LocalConfig;
import com.ae.sat.servers.master.service.docker.machine.localvm.LocalVMConfig;
import com.ae.sat.servers.master.service.docker.machine.oceanvm.OceanVMConfig;
import com.ae.sat.servers.master.service.docker.mixed.MixedConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by ae on 3-7-16.
 */

@Configuration
@Import(
        {
            LocalConfig.class,
            LocalVMConfig.class,
            OceanVMConfig.class,
            MixedConfig.class,
        })
@ComponentScan("com.ae.docker")
public class Config {

}
