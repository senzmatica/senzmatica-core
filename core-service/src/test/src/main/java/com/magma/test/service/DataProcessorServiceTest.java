package com.magma.test.service;

import com.magma.service.service.DataProcessorService;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
        System.out.println(calIRO("0070/4095/40.72", "SAB70SE0245"));
    }

    public double calIRO(String stringValue, String kitId) {
        String[] values = stringValue.split("/");
        Double ARead_A1 = Double.valueOf(values[0]);
        Double ARead_A2 = Double.valueOf(values[1]);
        Double tempVal = Double.valueOf(values[2]);

        int Rx = 8200; // fixed resistor attached in series to the sensor and ground...the same value
        // repeated for all WM and Temp Sensor.
        long open_resistance = 35000; // check the open resistance value by replacing sensor with an open and replace
        // the value here...this value might vary slightly with circuit components
        long short_resistance = 200; // similarly check short resistance by shorting the sensor terminals and replace
        // the value here.
        long short_CB = 240, open_CB = 255;
        double SupplyV = 3.3;
        double WM1_CB = 0;
        double SenV10K = 0;
        double SenVTempC = 0;
        double WM1_Resistance = 0;

        // Remove after devices fix
        List chr = Arrays.asList("AURACGW127", "AURACGW128", "AURACGW129", "AURACGW130");

        if (!chr.contains(kitId)) {
            ARead_A1 = ((ARead_A1 / 4096) * SupplyV); // get the average of the readings in the first direction and
            // convert to volts
            ARead_A2 = ((ARead_A2 / 4096) * SupplyV); // get the average of the readings in the second direction and
            // convert to volts
        }

        double WM1_ResistanceA = (Rx * (SupplyV - ARead_A1) / ARead_A1); // do the voltage divider math, using the Rx
        // variable representing the known resistor
        double WM1_ResistanceB = Rx * ARead_A2 / (SupplyV - ARead_A2); // reverse
        WM1_Resistance = ((WM1_ResistanceA + WM1_ResistanceB) / 2); // average the two directions and apply the
        // calibration factor

        if (WM1_Resistance > 550.00) {

            if (WM1_Resistance > 8000.00) {
                WM1_CB = -2.246 - 5.239 * (WM1_Resistance / 1000.00) * (1 + .018 * (tempVal - 24.00))
                        - .06756 * (WM1_Resistance / 1000.00) * (WM1_Resistance / 1000.00)
                        * ((1.00 + 0.018 * (tempVal - 24.00)) * (1.00 + 0.018 * (tempVal - 24.00)));
            } else if (WM1_Resistance > 1000.00) {
                WM1_CB = (-3.213 * (WM1_Resistance / 1000.00) - 4.093)
                        / (1 - 0.009733 * (WM1_Resistance / 1000.00) - 0.01205 * (tempVal));
            } else {
                WM1_CB = ((WM1_Resistance / 1000.00) * 23.156 - 12.736) * (1.00 + 0.018 * (tempVal - 24.00));
            }

        } else {
            if (WM1_Resistance > 300.00) {
                WM1_CB = 0.00;
            }

            if (WM1_Resistance < 300.00 && WM1_Resistance >= short_resistance) {
                WM1_CB = short_CB; // 240 is a fault code for sensor terminal short
            }
        }

        if (WM1_Resistance >= open_resistance) {
            WM1_CB = open_CB; // 255 is a fault code for open circuit or sensor not present
        }
        if (WM1_Resistance < short_resistance) {
            WM1_CB = 250; // 250 is a fault code not in the code
        }

        double valueIRO = (200 - Math.abs(WM1_CB)) / 4;

        if (valueIRO < 0) {
            return -9999.0;
        }
        return valueIRO;
    }

}
