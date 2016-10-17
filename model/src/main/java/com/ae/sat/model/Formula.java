package com.ae.sat.model;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ae on 21-5-16.
 */
public class Formula implements Serializable {

    private String name;

    private List<Clause> clauses;

    public Formula() {}

    public Formula(List<Clause> clauses) {
        setClauses(clauses);
    }

    public Formula(Clause... clauses) {
        this(new ArrayList<>(Arrays.asList(clauses)));
    }

    public void setClauses(List<Clause> clauses)  {
        this.clauses = clauses;
    }

    public List<Clause> getClauses() {
        return clauses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Integer, Long> amountOfOccurences() {
        return this.clauses.stream()
                           .flatMap(c -> c.getLiterals().stream())
                           .mapToInt(l -> l.getVar())
                           .boxed()
                           .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    public int nVars() {
        // Yeah fuck the json stuff, anyway don't call this too often :-)
        return this.clauses.stream()
                            .map(f -> f.nVars())
                            .sorted(Collections.reverseOrder())
                            .findFirst()
                            .get();
    }

    public static Clause toClause(int[] values) {
        List<Literal> literals = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            literals.add(new Literal(values[i]));
        }
        return new Clause(literals);
    }

    public static Clause toClause(List<Integer> values) {
        List<Literal> literals = new ArrayList<>();
        for (int i = 0; i < values.size(); i++) {
            literals.add(new Literal(values.get(i)));
        }
        return new Clause(literals);
    }

    public static List<Literal> toList(int[] values) {
        List<Literal> literals = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            literals.add(new Literal(values[i]));
        }
        return literals;
    }

    public static Formula toFormula(int[][] values) {
        List<Clause> clauses = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            clauses.add(toClause(values[i]));
        }
        return new Formula(clauses);
    }

    public static Formula fromCNFStream(InputStream is) throws IOException {

        Logger log = LoggerFactory.getLogger(Formula.class);

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        List<Clause> clauses = new ArrayList<>();
        boolean started= false;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("p cnf ")) {
                started = true;
            }
            if (!started) { continue; }
            if (line.startsWith("c") || line.startsWith("p")) { continue; }
            String[] data = line.split("\\s+");
            if (data.length == 0) {
                throw new IllegalStateException("Could not parse CNF"); }
            else if (data.length == 1) {
                clauses.add(new Clause(new ArrayList<>()));
            } else if (!"0".equals(data[data.length -1])) {
                throw new IllegalStateException("Could not parse CNF");
            }
            List<Integer> lits = new ArrayList<>();
            for (int i = 0; i < data.length - 1; i++) {
                try {
                    lits.add(Integer.parseInt(data[i]));
                } catch (NumberFormatException e) {
                    // yeay fuck bad java stuff ..
                    log.error("Fix this stuff %s ..");
                }
            }
            clauses.add(toClause(lits));
        }
        return new Formula(clauses);
    }
}
