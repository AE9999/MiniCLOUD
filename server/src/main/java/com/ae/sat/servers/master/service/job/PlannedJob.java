package com.ae.sat.servers.master.service.job;

import com.ae.sat.model.SolverAssignment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by ae on 22-5-16.
 */
public class PlannedJob {
    private List<SolverAssignment> solverAssignments = new ArrayList<>();
    private String id = UUID.randomUUID().toString();
    public PlannedJob() {

    }

    public List<SolverAssignment> getSolverAssignments() {
        return solverAssignments;
    }

    public void setSolverAssignments(List<SolverAssignment> solverAssignments) {
        this.solverAssignments = solverAssignments;
    }

    public String getId() {
        return id;
    }
}
