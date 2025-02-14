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

}
