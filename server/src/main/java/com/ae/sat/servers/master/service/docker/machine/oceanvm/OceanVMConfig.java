package com.ae.docker.machine.oceanvm;

import com.ae.docker.Config;
import com.ae.docker.machine.CertificateService;
import com.ae.sh.ShConfig;
import org.springframework.context.annotation.*;

/**
 * Created by ae on 3-7-16.
 */

@Profile("oceanvmDocker")
@PropertySource({
        "classpath:dockerGeneral.properties",
        "classpath:dockerOceanvm.properties"
})
public class OceanVMConfig {}
