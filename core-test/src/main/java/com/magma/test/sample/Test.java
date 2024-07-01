package com.magma.test.sample;

public class Test {

    public static void main(String args[]) {
//        System.out.println(Base64.getEncoder().encodeToString(String.format("%s:%s", "vancouveradmin", "vancouveradmin").getBytes()));
//        System.out.println(Base64.getEncoder().encodeToString(String.format("%s:%s", "ottawaadmin", "ottawaadmin").getBytes()));

        testDec("0-B:319;1-CS:0;2-CS:0;3-CS:0*0-S:0;1-S:0;2-S:0");
    }

    public static void testDec(String txt) {
        if (txt.contains("*")) {
            String[] els = txt.split("\\*");
            txt = els[0];
            System.out.println(els[1]);
        }
    }


}