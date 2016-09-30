package com.ae.docker.local;

import com.ae.docker.Config;
import com.ae.sh.ShConfig;
import org.springframework.context.annotation.*;

/**
 * Created by ae on 3-7-16.
 */

@Profile("localDocker")
@PropertySource({
                 "classpath:dockerGeneral.properties",
                 "classpath:dockerLocal.properties"
                })
public class LocalConfig {

}
