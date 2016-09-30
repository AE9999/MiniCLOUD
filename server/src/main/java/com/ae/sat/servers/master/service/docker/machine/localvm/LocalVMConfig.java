package com.ae.docker.machine.localvm;

import com.ae.docker.machine.CertificateService;
import com.ae.sh.ShConfig;
import org.springframework.context.annotation.*;

/**
 * Created by ae on 3-7-16.
 */

@Profile("localvmDocker")
@PropertySource({
        "classpath:dockerGeneral.properties",
        "classpath:dockerLocalvm.properties"
})
public class LocalVMConfig {}
