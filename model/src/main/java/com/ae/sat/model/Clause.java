package com.ae.sat.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ae on 21-5-16.
 */
public class Clause implements Serializable {
    private List<Literal> literals;

    public Clause() {

    }

    public Clause(Literal... literals) {
        this(new ArrayList<>(Arrays.asList(literals)));
    }

    public Clause(List<Literal> literals) {
        this.literals = literals;
    }

    public List<Literal> getLiterals() {
        return literals;
    }

    public int nVars() {
        return literals.stream()
                       .sorted((o1, o2) -> -1 * Integer.compare(o1.getVar(), o2.getVar()))
                       .mapToInt(f -> f.getVar())
                       .findFirst().getAsInt();
    }

    @Override
    public String toString() {
        return "Clause{" +
                "literals=" + literals +
                '}';
    }
}

