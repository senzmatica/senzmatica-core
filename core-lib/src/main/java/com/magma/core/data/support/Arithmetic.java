package com.magma.core.data.support;

public enum Arithmetic {
    ADD(0), SUB(1), MUL(2), DIV(3), AXaB(4);    //AXaB = A*X+B


    private int value;

    Arithmetic(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}
