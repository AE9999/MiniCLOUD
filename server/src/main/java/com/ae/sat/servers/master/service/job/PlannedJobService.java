package com.ae.sat.servers.master.service.job;

import com.ae.sat.model.Formula;
import com.ae.sat.model.Literal;
import com.ae.sat.model.SolverAssignment;
import com.ae.sat.model.SolverNodeType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by ae on 22-5-16.
 */

@Component
public class PlannedJobService {

    @Value("${solverVerbosity}")
    private int solverVerbosity;

    private boolean[] bitSetToArray(BitSet bs, int width) {
        boolean[] result = new boolean[width]; // all false
        bs.stream().forEach(i -> result[i] = true);
        return result;
    }

    List<boolean[]> bool(int n) {
        return IntStream.range(0, (int)Math.pow(2, n))
                .mapToObj(i -> bitSetToArray(BitSet.valueOf(new long[] { i }), n))
                .collect(toList());
    }

    public PlannedJob singleConfig(Formula formula) {
        PlannedJob plannedJob = new PlannedJob();
        List<SolverAssignment> solverAssignments = new ArrayList<>();
        SolverAssignment solverAssignment = new SolverAssignment();
        solverAssignment.setFormula(formula);
        solverAssignment.setAssumptions(new ArrayList<>());
        solverAssignments.add(solverAssignment);
        solverAssignment.setSolverNodeType(SolverNodeType.GLUCOSE);
        plannedJob.setSolverAssignments(solverAssignments);
        return plannedJob;
    }

    public PlannedJob simpleConfig(Formula formula, int amountOfSolvers) {
        if (amountOfSolvers <= 0) {
            throw new IllegalArgumentException("Need at least one solver.");
        }
        if (amountOfSolvers == 1) {
            return singleConfig(formula);
        }
        if ((amountOfSolvers & (amountOfSolvers - 1)) != 0) {
            String m = "Expected amount of Solvers to be a power of 2";
            throw new IllegalArgumentException(m);
        }

        int amountOfIntsToFix = new Double(Math.log(amountOfSolvers) / Math.log(2)).intValue();
        Stream<Map.Entry<Integer, Long>> stream;
        stream = formula.amountOfOccurences().entrySet().stream();
        List<Integer> vars = stream.sorted(Comparator.comparing(Map.Entry::getValue))
                                   .map(Map.Entry::getKey)
                                   .collect(toList());
        List<SolverAssignment> solverAssignments = new ArrayList<>();
        for (boolean[] config : bool(amountOfIntsToFix)) {
            SolverAssignment solverAssignment = new SolverAssignment();
            solverAssignment.setFormula(formula);
            List<Literal> assumptions = new ArrayList<>();
            for (int i = 0; i < config.length; i++) {
                assumptions.add(new Literal(vars.get(i), config[i]));
            }
            solverAssignment.setAssumptions(assumptions);
            solverAssignment.setSolverNodeType(SolverNodeType.GLUCOSE);
            solverAssignments.add(solverAssignment);
        }

        PlannedJob plannedJob = new PlannedJob();
        plannedJob.setSolverAssignments(solverAssignments);
        return plannedJob;
    }
}
