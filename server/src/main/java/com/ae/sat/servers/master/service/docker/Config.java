package com.ae.docker;

import com.ae.docker.local.LocalConfig;
import com.ae.docker.machine.localvm.LocalVMConfig;
import com.ae.docker.machine.oceanvm.OceanVMConfig;
import com.ae.docker.mixed.MixedConfig;
import com.ae.sh.ShConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

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
            ShConfig.class
        })
@ComponentScan("com.ae.docker")
public class Config {

}
