package com.ae.sat.servers.master.service.preprocess;

import com.ae.sat.model.Clause;
import com.ae.sat.model.Formula;
import com.ae.sat.model.Literal;
import org.bytedeco.javacpp.minisat;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ae on 11-6-16.
 */
public class MinisatPreprocessService implements PreProcessService {

    minisat.SimpSolver simpSolver;

    private minisat.Lit lit2MinisatLit(Literal lit) {
        return minisat.mkLit(lit.getVar(), lit.isSigned());
    }

    private minisat.LitVecPointer clause2LitVecPointer(Clause clause) {
        minisat.LitVecPointer rvalue = new minisat.LitVecPointer();
        for (Literal lit : clause.getLiterals()) {
            rvalue.push(lit2MinisatLit(lit));
        }
        return rvalue;
    }

    private void assertFormula(Formula formula) {
        int nvars = formula.nVars();
        while (simpSolver.nVars() <= nvars) {
            simpSolver.newVar();
        }
        for (Clause clause : formula.getClauses()) {
            minisat.LitVecPointer lvp = clause2LitVecPointer(clause);
            simpSolver.addClause(lvp);
        }
    }

    private Literal minisatLit2Literal(minisat.Lit minisatLiteral) {
        return new Literal(minisat.var(minisatLiteral), minisat.sign(minisatLiteral));
    }

    private Clause minisatClause2Clause(minisat.Clause minisatClause) {
        List<Literal> literals = new ArrayList<>();
        for (int i = 0; i < minisatClause.size(); i++) {
            literals.add(minisatLit2Literal(minisatClause.get(i)));
        }
        return new Clause(literals);
    }

    @Override
    public Formula preprocess(Formula formula) {
        simpSolver = new minisat.SimpSolver();
        assertFormula(formula);
        simpSolver.eliminate();
        minisat.ClauseIterator it = simpSolver.clausesBegin();

        List<Clause> clauses = new ArrayList<>();
        while (it.notEquals(simpSolver.clausesEnd())) {
            minisat.Clause clause = it.multiply(); // Yeah shitty name;
            it.increment();
            clauses.add(minisatClause2Clause(clause));
        }
        return new Formula(clauses);
    }
}
