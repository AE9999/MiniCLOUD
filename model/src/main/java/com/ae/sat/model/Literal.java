package com.ae.sat.model;

import java.io.Serializable;

/**
 * Created by ae on 21-5-16.
 */
public class Literal implements Serializable {
    private int var;
    private boolean signed;

    public Literal() {

    }

    public Literal(int var, boolean signed) {
        if (var <= 0) {
            String m = "This constructor expects a positive integer argument";
            throw new IllegalArgumentException(m);
        }
        this.var = var;
        this.signed = signed;
    }

    public Literal(int var) {
        if (var == 0) {
            String m = "This constructor expects a non zero integer argument";
            throw new IllegalArgumentException(m);
        }
        this.var = var < 0 ? -1 * var : var;
        this.signed = var < 0;
    }

    public int getVar() {
        return var;
    }

    public void setVar(int var) {
        this.var = var;
    }

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }
}