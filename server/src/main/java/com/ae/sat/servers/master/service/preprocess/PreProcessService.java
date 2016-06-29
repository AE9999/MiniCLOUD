package com.ae.sat.servers.master.service.preprocess;

import com.ae.sat.model.Formula;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by ae on 11-6-16.
 */

public interface PreProcessService {
    Formula preprocess(Formula formula);
}
