package com.ae.sat.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by ae on 22-5-16.
 */
public class SolverAssignment implements Serializable {

    private Formula formula;

    private List<Literal> assumptions;

    private int verbosity;

    private SolverNodeType solverNodeType;

    private byte[] configuration;

    public Formula getFormula() {
        return formula;
    }

    public void setFormula(Formula formula) {
        this.formula = formula;
    }

    public List<Literal> getAssumptions() {
        return assumptions;
    }

    public void setAssumptions(List<Literal> assumptions) {
        this.assumptions = assumptions;
    }

    public int getVerbosity() {
        return verbosity;
    }

    public void setVerbosity(int verbosity) {
        this.verbosity = verbosity;
    }

    public SolverNodeType getSolverNodeType() {
        return solverNodeType;
    }

    public void setSolverNodeType(SolverNodeType solverNodeType) {
        this.solverNodeType = solverNodeType;
    }

    public byte[] getConfiguration() {
        return configuration;
    }

    public void setConfiguration(byte[] configuration) {
        this.configuration = configuration;
    }
}
