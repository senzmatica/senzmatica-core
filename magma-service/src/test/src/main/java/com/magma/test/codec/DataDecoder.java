package com.magma.test.codec; //need to remove when create codec file

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataDecoder {

    public String convert(Object messageObject) {

        StringBuilder result = new StringBuilder();

        if (messageObject instanceof Map) {
            Map<String, Object> data = (Map<String, Object>) messageObject;

            @SuppressWarnings("unchecked") // Suppressing warning for unchecked cast
            List<Map<String, String>> sensorMessages = (List<Map<String, String>>) data.get("sensorMessages");

            int count = 0;

            List<String> sensorNamesToCheck = Arrays.asList(
                    "SA Temperature",
                    "RA Temp and Humidity",
                    "ThreePhaseCurrentMeter",
                    "Supply Fan Current Sensor",
                    "OA Temperature"
            );
            List<String> sensorCodes = Arrays.asList("T", "H", "CS", "CS", "T");

            for (int i = 0; i < sensorMessages.size(); i++) {
                Map<String, String> sensorMessage = sensorMessages.get(i);

                String index = String.valueOf(count);

                if (sensorMessage.get("sensorName").startsWith(sensorNamesToCheck.get(count))) {
                    String sensorCode = sensorCodes.get(count);
                    String sensorValue = "";
                    if (sensorCode == "CS") {
                        sensorValue = sensorMessage.get("state");
                    } else {
                        sensorValue = sensorMessage.get("dataValue").split("\\|")[0];
                    }


                    String formattedMessage = String.format("%s-%s:%s;", index, sensorCode, sensorValue);

                    result.append(formattedMessage);
                    count++;
                }

            }
        } else {
            throw new IllegalArgumentException("Invalid message format for this decoder");
        }

        return result.toString();
    }

    public static void main(String[] args) {
        DataDecoder dataDecoder = new DataDecoder();

        String input = "{\"sensorMessages\":[{\"dataMessageGUID\":\"9463e5aa-30d0-4623-87f8-cbefdd89130b\",\"plotValues\":\"58.1\",\"sensorName\":\"SA Temperature - 448618\",\"signalStrength\":\"70\",\"dataType\":\"TemperatureData\",\"dataValue\":\"14.5\",\"pendingChange\":\"True\",\"plotLabels\":\"Fahrenheit\",\"rawData\":\"14.5\",\"sensorID\":\"448618\",\"voltage\":\"3.51\",\"networkID\":\"74950\",\"state\":\"16\",\"messageDate\":\"2021-08-06 00:43:58\",\"applicationID\":\"84\",\"batteryLevel\":\"100\"},{\"dataMessageGUID\":\"16422bfd-65c6-4d88-a148-a2ae9d37bfa6\",\"plotValues\":\"432|420\",\"sensorName\":\"Space CO2 Meter - 730437\",\"signalStrength\":\"87\",\"dataType\":\"PPM|PPM\",\"dataValue\":\"432|420\",\"pendingChange\":\"True\",\"plotLabels\":\"Instantaneous|TWA\",\"rawData\":\"432%7c420%7c0%7c393\",\"sensorID\":\"730437\",\"voltage\":\"3.44\",\"networkID\":\"74950\",\"state\":\"0\",\"messageDate\":\"2021-08-06 00:44:08\",\"applicationID\":\"106\",\"batteryLevel\":\"100\"},{\"dataMessageGUID\":\"18d5cce3-762a-4ad7-a121-a136f339f64e\",\"plotValues\":\"57.63|69.764|54.14|63.1|69.08\",\"sensorName\":\"RA Temp and Humidity - 448173\",\"signalStrength\":\"73\",\"dataType\":\"Percentage|TemperatureData|TemperatureData|MoistureWeight|TemperatureData\",\"dataValue\":\"57.63|20.98|12.3|63.1|20.6\",\"pendingChange\":\"True\",\"plotLabels\":\"Humidity|Fahrenheit|DewPoint|GrainsPerPound|HeatIndex_Fahrenheit\",\"rawData\":\"57.63%2c20.98%2c12.3%2c63.1%2c20.6\",\"sensorID\":\"448173\",\"voltage\":\"3.5\",\"networkID\":\"74950\",\"state\":\"0\",\"messageDate\":\"2021-08-06 00:47:41\",\"applicationID\":\"43\",\"batteryLevel\":\"100\"},{\"dataMessageGUID\":\"4e275bb1-96bf-48f0-bbf5-44dcf073d589\",\"plotValues\":\"468111.5|98.05|99|96|100|92.11|93|90|100|89.16|90|87|100\",\"sensorName\":\"ThreePhaseCurrentMeter500 - 779522\",\"signalStrength\":\"67\",\"dataType\":\"Amps|Amps|Amps|Percentage|Amps|Amps|Amps|Percentage|Amps|Amps|Amps|Percentage|AmpHours|WattHours\",\"dataValue\":\"98.05|99|96|1|92.11|93|90|1|89.16|90|87|1|468111.5|0\",\"pendingChange\":\"True\",\"plotLabels\":\"Amp Hours|Phase1Average|Phase1Max|Phase1Min|Phase1Duty|Phase2Average|Phase2Max|Phase2Min|Phase2Duty|Phase3Average|Phase3Max|Phase3Min|Phase3Duty\",\"rawData\":\"98.05%7c99%7c96%7c100%7c92.11%7c93%7c90%7c100%7c89.16%7c90%7c87%7c100%7c468111.5%7c0\",\"sensorID\":\"779522\",\"voltage\":\"3.44\",\"networkID\":\"74950\",\"state\":\"0\",\"messageDate\":\"2021-08-06 00:48:42\",\"applicationID\":\"129\",\"batteryLevel\":\"100\"},{\"dataMessageGUID\":\"7442ad51-7b01-48d9-ba04-ea22ab481203\",\"plotValues\":\"242228.4|41.06|44|38|100|42.46|45|39|100|41.9|44|39|100\",\"sensorName\":\"Supply Fan Current Sensor - 730959\",\"signalStrength\":\"74\",\"dataType\":\"Amps|Amps|Amps|Percentage|Amps|Amps|Amps|Percentage|Amps|Amps|Amps|Percentage|AmpHours|WattHours\",\"dataValue\":\"41.06|44|38|1|42.46|45|39|1|41.9|44|39|1|242228.4|0\",\"pendingChange\":\"True\",\"plotLabels\":\"Amp Hours|Phase1Average|Phase1Max|Phase1Min|Phase1Duty|Phase2Average|Phase2Max|Phase2Min|Phase2Duty|Phase3Average|Phase3Max|Phase3Min|Phase3Duty\",\"rawData\":\"41.06%7c44%7c38%7c100%7c42.46%7c45%7c39%7c100%7c41.9%7c44%7c39%7c100%7c242228.4%7c0\",\"sensorID\":\"730959\",\"voltage\":\"3.39\",\"networkID\":\"74950\",\"state\":\"0\",\"messageDate\":\"2021-08-06 00:50:06\",\"applicationID\":\"109\",\"batteryLevel\":\"100\"},{\"dataMessageGUID\":\"9a6b20cf-3b1b-4321-b3df-8714eda18df1\",\"plotValues\":\"62.6\",\"sensorName\":\"OA Temperature - 447721\",\"signalStrength\":\"29\",\"dataType\":\"TemperatureData\",\"dataValue\":\"17\",\"pendingChange\":\"True\",\"plotLabels\":\"Fahrenheit\",\"rawData\":\"17\",\"sensorID\":\"447721\",\"voltage\":\"3.31\",\"networkID\":\"74950\",\"state\":\"2\",\"messageDate\":\"2021-08-06 00:51:34\",\"applicationID\":\"2\",\"batteryLevel\":\"100\"}],\"gatewayMessage\":{\"date\":\"2021-08-06 00:51:38\",\"accountID\":\"45622\",\"messageType\":\"0\",\"gatewayName\":\"CGW3 3g North America - 936049\",\"signalStrength\":\"7\",\"count\":\"6\",\"pendingChange\":\"False\",\"networkID\":\"74950\",\"power\":\"0\",\"gatewayID\":\"936049\",\"batteryLevel\":\"101\"}}";

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(input);

            Map<String, Object> result = new HashMap<>();

            result = objectMapper.convertValue(jsonNode, Map.class);

            System.out.println(dataDecoder.convert(result));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

