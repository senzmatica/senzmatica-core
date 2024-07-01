package com.magma.test.codec;

public class DPv3 {
    private static final String[] CODES = {"T", "H", "MEA4", "IRO", "LIA1", "B", "IT", "SS", "PS"};

    public String convert(String input) {
        StringBuilder formattedMessage = new StringBuilder();

        String[] values = input.split(",\\s*");

        for (int i = 0; i < values.length; i++) {
            String code = CODES[i];
            String value = values[i];

            String messagePart = String.format("%s-%s:%s;", i, code, value);
            formattedMessage.append(messagePart);
        }

        return formattedMessage.toString().trim();
    }

    public static void main(String[] args) {
        DPv3 dataDecoder = new DPv3();

        String input = "-46.85, -6.00, 528/230, 0066/4095/-46.85, 000000, 304, 67, 25, 1";

        System.out.println(dataDecoder.convert(input));
    }
}
