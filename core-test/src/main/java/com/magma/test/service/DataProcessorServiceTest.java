package com.magma.test.service;

import com.magma.core.service.DataProcessorService;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class DataProcessorServiceTest {

    DataProcessorService dataProcessorService = new DataProcessorService();

    @Test
    public void getRLL() {
        String stringValue = "fb7a01e8feda/-41dBm/fd2a3bf4b9eb/-35dBm/cc1f085aec5b/-45dBm";

        String[] locTem = stringValue.split("/");
        Map<String, String> relativeLocation = new HashMap<>();

        System.out.println(dataProcessorService.getRLL(locTem, relativeLocation));
    }

    @Test
    public void testIRO() {
        System.out.println(calIRO("3424/0802/20.49"));
        System.out.println(calIRO("3434/0746/20.32"));
//        System.out.println(calIRO("3420/1041/28.54" ));
//        System.out.println(calIRO("3426/1018/28.54" ));
    }


    public double calIRO(String stringValue) {
        return dataProcessorService.calculateIRO(stringValue);
    }

}
