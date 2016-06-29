package com.ae.sat.servers.worker.service;

import com.ae.sat.message.Message;
import com.ae.sat.message.Stats;
import com.ae.sat.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bytedeco.javacpp.glucose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by ae on 22-5-16.
 */
public class GlucoseSolver extends Solver {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private glucose.Solver solver = new glucose.Solver();

    private glucose.LitVecPointer assumptions2LitVecPointer(List<Literal> assumptions) {
        glucose.LitVecPointer rvalue = new glucose.LitVecPointer();
        for (Literal lit : assumptions) {
            while (solver.nVars() <= lit.getVar()) {
                solver.newVar();
            }
            rvalue.push(lit2MinisatLit(lit));
        }
        return rvalue;
    }

    private glucose.LitVecPointer clause2LitVecPointer(Clause clause) {
        glucose.LitVecPointer rvalue = new glucose.LitVecPointer();
        for (Literal lit : clause.getLiterals()) {
            rvalue.push(lit2MinisatLit(lit));
        }
        return rvalue;
    }

    private glucose.Lit lit2MinisatLit(Literal lit) {
        return glucose.mkLit(lit.getVar(), lit.isSigned());
    }

    private Literal minisatLit2Lit(glucose.Lit lit) {
        return new Literal(glucose.var(lit), glucose.sign(lit));
    }

    private void assertFormula(Formula formula) {
        int nvars = formula.nVars();
        while (solver.nVars() <= nvars) {
            solver.newVar();
        }
        for (Clause clause : formula.getClauses()) {
            glucose.LitVecPointer lvp = clause2LitVecPointer(clause);
            solver.addClause(lvp);
        }
    }

    private Answer importUnitClauses() {
        synchronized (incomingUnitClauses) {
            log.info("Importing unit clauses ..");
            for (Literal s : incomingUnitClauses) {
                glucose.Lit lit = lit2MinisatLit(s);
                if (solver.value(lit) == glucose.l_Undef) {
                    log.info(String.format("Added %s as unit clause ..", lit));
                    solver.addClause(lit);
                } else if (solver.value(lit) == glucose.l_False) {
                    log.info(String.format("Found contradiction for %s ..", lit));
                    return Answer.UNSAT;
                } else {
                    log.info(String.format("%s was already satisfied ..", lit));
                }
            }
            log.info("Done ..");
            incomingUnitClauses.clear();
        }
        return Answer.UNKOWN;
    }

    @Override
    public void setConfig(byte[] rawConfigData) {

    }

    @Override
    public Answer doSolve(SolverAssignment assignment) {

        assertFormula(assignment.getFormula());
        glucose.LitVecPointer assumptions;
        assumptions = assumptions2LitVecPointer(assignment.getAssumptions());

        ObjectMapper objectMapper = new ObjectMapper();
        int assignmentPointer = solver.nAssigns();

        while (true) {
            importUnitClauses();
            log.info("Going through preliminary trail stuff after importing unit clauses ..");
            for (; assignmentPointer < solver.nAssigns(); assignmentPointer++) {}
            log.info("Done ..");

            log.info("Starting solving ..");
            solver.setConfBudget(5000);
            glucose.lbool res = solver.solveLimited(assumptions);
            log.info(String.format("The result was %s ..", res));
            if (!res.equals(glucose.l_Undef())) {
                return res.equals(glucose.l_True()) ? Answer.SAT : Answer.UNSAT;
            }

            try{
                Stats stats  = new Stats();
                stats.setProgress(solver.nAssigns());
                stats.setConflicts(solver.conflicts());
                //stats.setMessage("Need more time !!!");
                Message message = new Message(stats);
                String rawMessage = objectMapper.writeValueAsString(message);
                informationChannel.basicPublish("", informationChannelName, null, rawMessage.getBytes());
            }  catch (IOException e) {
                log.error("Could not publish stat update", e);
            }

            log.info("Publishing learnt assignments ..");
            // These are new assignements we have learnt
            for (; assignmentPointer < solver.nAssigns(); assignmentPointer++) {
                glucose.Lit s = solver.trail().get(assignmentPointer); 
                Literal learnt = minisatLit2Lit(s);
                publish(learnt);
            }
            log.info("Done ..");
        }
    }

}
