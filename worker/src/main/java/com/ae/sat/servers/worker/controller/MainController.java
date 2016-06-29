package com.ae.sat.servers.worker.controller;


import com.ae.sat.message.Message;
import com.ae.sat.model.Answer;
import com.ae.sat.model.SolverAssignment;

import com.ae.sat.servers.worker.service.MiniSatSolver;
import com.ae.sat.servers.worker.service.SolverService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.nustaq.serialization.FSTObjectInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by ae on 21-5-16.
 */
@Component
public class MainController implements CommandLineRunner {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private SolverService solverService;

    @Value("${fileToSolve}")
    private String fileToSolve;


    @Override
    public void run(String... strings) throws Exception {

        log.info("Parsing input file ..");
        FileInputStream is = new FileInputStream(new File(fileToSolve));
        FSTObjectInput in = new FSTObjectInput(is);
        SolverAssignment assignment = (SolverAssignment) in.readObject(SolverAssignment.class);
        in.close();

        log.info("Starting solving ..");
        solverService.solve(assignment);
    }
}
