package com.ae.sat.servers.worker.service;

import com.ae.sat.model.SolverAssignment;
import com.ae.sat.model.SolverNodeType;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by ae on 6-6-16.
 */
@Component
public class SolverService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${unitClauseChannelName}")
    private String unitClauseChannelName;

    @Value("${informationChannelName}")
    private String informationChannelName;

    @Value("${rabbitMQHost}")
    private String rabbitMQHost;

    @Value("${rabbitMQPort}")
    private int rabbitMQPort;

    private Solver getSolver(SolverNodeType solverNodeType) {
        log.info(String.format("Getting the solver for type %s ..", solverNodeType));
        Solver solver;
        if (solverNodeType == SolverNodeType.MINISAT) {
            solver = new MiniSatSolver();
        } else if (solverNodeType == SolverNodeType.GLUCOSE) {
            solver = new GlucoseSolver();
        } else {
            throw  new RuntimeException("Unsuported solver type requested ..");
        }
        return solver;
    }

    public void solve(SolverAssignment assignment) throws IOException, TimeoutException {
        log.info(String.format("Solving %s ..", assignment));
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitMQHost);
        connectionFactory.setPort(rabbitMQPort);

        Solver solver = getSolver(assignment.getSolverNodeType());
        solver.setUnitClauseChannelName(unitClauseChannelName);
        solver.setInformationChannelName(informationChannelName);
        solver.setConfig(assignment.getConfiguration());
        solver.setConnectionFactory(connectionFactory);
        solver.solve(assignment);
    }
}
