package com.ae.sat.servers.worker.service;

import com.ae.sat.message.Message;
import com.ae.sat.message.Stats;
import com.ae.sat.model.*;
import org.bytedeco.javacpp.minisat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by ae on 22-5-16.
 */
public class MiniSatSolver extends Solver {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private minisat.Solver solver = new minisat.Solver();

    private minisat.LitVecPointer assumptions2LitVecPointer(List<Literal> assumptions) {
        minisat.LitVecPointer rvalue = new minisat.LitVecPointer();
        for (Literal lit : assumptions) {
            while (solver.nVars() <= lit.getVar()) {
                solver.newVar();
            }
            rvalue.push(lit2MinisatLit(lit));
        }
        return rvalue;
    }

    private minisat.LitVecPointer clause2LitVecPointer(Clause clause) {
        minisat.LitVecPointer rvalue = new minisat.LitVecPointer();
        for (Literal lit : clause.getLiterals()) {
            rvalue.push(lit2MinisatLit(lit));
        }
        return rvalue;
    }

    private minisat.Lit lit2MinisatLit(Literal lit) {
        return minisat.mkLit(lit.getVar(), lit.isSigned());
    }

    private Literal minisatLit2Lit(minisat.Lit lit) {
        return new Literal(minisat.var(lit), minisat.sign(lit));
    }

    private void assertFormula(Formula formula) {
        int nvars = formula.nVars();
        while (solver.nVars() <= nvars) {
            solver.newVar();
        }
        for (Clause clause : formula.getClauses()) {
            minisat.LitVecPointer lvp = clause2LitVecPointer(clause);
            solver.addClause(lvp);
        }
    }

    private Answer importUnitClauses() {
        synchronized (incomingUnitClauses) {
            log.info("Importing unit clauses ..");
            for (Literal s : incomingUnitClauses) {
                minisat.Lit lit = lit2MinisatLit(s);
                if (solver.value(lit) == minisat.l_Undef) {
                    log.info(String.format("Added %s as unit clause ..", lit));
                    solver.addClause(lit);
                } else if (solver.value(lit) == minisat.l_False) {
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
        minisat.LitVecPointer assumptions;
        assumptions = assumptions2LitVecPointer(assignment.getAssumptions());

        minisat.TrailIterator trailIterator = solver.trailBegin();
        log.info("Going through preliminary trail stuff ..");
        while (trailIterator.notEquals(solver.trailEnd())) {
            trailIterator.increment();
        }
        log.info("Done ..");


        while (true) {
            importUnitClauses();
            log.info("Going through preliminary trail stuff after importing unit clauses ..");
            while (trailIterator.notEquals(solver.trailEnd())) {
                trailIterator.increment();
            }
            log.info("Done ..");

            log.info("Starting solving ..");
            solver.setConfBudget(5000);
            minisat.lbool res = solver.solveLimited(assumptions);
            log.info(String.format("The result was %s ..", res));
            if (!res.equals(minisat.l_Undef())) {
                return res.equals(minisat.l_True()) ? Answer.SAT : Answer.UNSAT;
            }

            try{
                Stats stats  = new Stats();
                stats.setProgress(solver.nAssigns());
                stats.setConflicts(solver.conflicts());
                stats.setMessage("Need more time !!!");
                Message message = new Message(stats);
                String rawMessage = objectMapper.writeValueAsString(message);
                informationChannel.basicPublish("", informationChannelName, null, rawMessage.getBytes());
            }  catch (IOException e) {
                log.error("Could not publish stat update", e);
            }

            log.info("Publishing learnt assignments ..");
            // These are new assignements we have learnt
            while (trailIterator.notEquals(solver.trailEnd())) {
                minisat.Lit s = trailIterator.multiply(); // Yeah shitty name;
                Literal learnt = minisatLit2Lit(s);
                publish(learnt);
                trailIterator.increment();
            }
            log.info("Done ..");
        }
    }
}
