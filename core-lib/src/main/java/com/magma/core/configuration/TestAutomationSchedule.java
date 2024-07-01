package com.magma.core.configuration;

import com.magma.core.data.entity.*;
import com.magma.core.data.repository.*;
import com.magma.core.data.support.TestResult;
import com.magma.core.data.support.TestSummary;
import com.magma.core.util.SensorCode;
import com.magma.util.MagmaTime;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TestAutomationSchedule {

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    MongoOperations mongoOperations;

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    SensorRepository sensorRepository;

    @Autowired
    TestSummaryRepository testSummaryRepository;

    @Autowired
    TestCaseRepository testCaseRepository;

    @Scheduled(fixedRate = 3600000) // Executes every 5 seconds
    public void generateTestResultEveryOneHour() {
        List<Device> devicesOfBatch = deviceRepository.findAll();
        List<TestCase> requestedTestCases = testCaseRepository.findByIsDefault(Boolean.FALSE);

        Map<String, Map<String, Map<String, List<TestCard>>>> deviceMap = new HashMap<>();
        devicesOfBatch.forEach((device) -> {
            Map<String, Map<String, List<TestCard>>> mainMap = new HashMap<>();
            requestedTestCases.forEach((mainTest) -> {
                if (mainTest.getDevices().contains(device.getId())) {
                    Map<String, List<TestCard>> subTestMap = new HashMap<>();
                    mainTest.getSubTestCases().forEach((subTest) -> {
                        subTestMap.put(subTest.getSubTestTitle(), new ArrayList<>());
                        TestCard testCard = new TestCard();
                        if (mainTest.getEndDate().isBeforeNow() == false) {
                            //ongoing
                            testCard.setSubTestTitle(subTest.getSubTestTitle());
                            testCard.setTestResult(TestResult.ON_GOING);
                            testCard.setDeviceId(device.getId());
                            testCard.setSubTestCaseStartTime(mainTest.getStartDate());
                            testCard.setSubTestCaseEndTime(mainTest.getEndDate());
                            testCard.setBatchNumber(mainTest.getBatchNumber());
                            testCard.setMainTestTitle(mainTest.getMainTestTitle());
                            subTestMap.get(subTest.getSubTestTitle()).add(testCard);
                        } else {
                            //need to test
                            if (subTest.getSubTestTitle().equals("No Of Data")) {
                                TestCard result = NoOfDataTest(mainTest.getBatchNumber(), device, subTest, mainTest, mainTest.getStartDate(), mainTest.getEndDate());
                                subTestMap.get(subTest.getSubTestTitle()).add(result);
                            }
                            if (subTest.getSubTestTitle().equals("External Temperature")
                                    || subTest.getSubTestTitle().equals("Electrical Conductivity")
                                    || subTest.getSubTestTitle().equals("Internal Temperature")
                                    || subTest.getSubTestTitle().equals("Humidity")
                            ) {
                                TestCard result = ExternalInternalHumidityTest(subTest, device, mainTest.getBatchNumber(), mainTest, mainTest.getStartDate(), mainTest.getEndDate());
                                subTestMap.get(subTest.getSubTestTitle()).add(result);
                            }
                            if (subTest.getSubTestTitle().equals("Battery")) {
                                TestCard result = BatteryTest(subTest, mainTest.getBatchNumber(), device, mainTest, mainTest.getStartDate(), mainTest.getEndDate());
                                subTestMap.get(subTest.getSubTestTitle()).add(result);
                            }
                            if (subTest.getSubTestTitle().equals("High Coverage")
                                    || subTest.getSubTestTitle().equals("Medium Coverage")
                                    || subTest.getSubTestTitle().equals("Low Coverage")
                                    || subTest.getSubTestTitle().equals("Very Low Coverage")
                                    || subTest.getSubTestTitle().equals("No Coverage")) {
                                TestCard result = CoverageTest(subTest, mainTest.getBatchNumber(), device, mainTest, mainTest.getStartDate(), mainTest.getEndDate());
                                subTestMap.get(subTest.getSubTestTitle()).add(result);
                            }
                            if (subTest.getSubTestTitle().equals("Flash Saving Test Day")) {
                                TestCard result = FlashSavingTestDay(subTest, mainTest.getBatchNumber(), device, mainTest, mainTest.getStartDate(), mainTest.getEndDate());
                                subTestMap.get(subTest.getSubTestTitle()).add(result);
                            }
                            if (subTest.getSubTestTitle().equals("Flash Saving Test Week")) {
                                TestCard result = FlashSavingTestWeek(subTest, mainTest.getBatchNumber(), device, mainTest, mainTest.getStartDate(), mainTest.getEndDate());
                                subTestMap.get(subTest.getSubTestTitle()).add(result);
                            }

                        }
                    });
                    mainMap.put(mainTest.getId(), subTestMap);
                }
            });
            if (!mainMap.isEmpty()) {
                deviceMap.put(device.getId(), mainMap);
            }
        });
        List<Map<String, Map<String, Map<String, List<TestCard>>>>> res = new ArrayList<>();
        res.add(deviceMap);
        List<TestSummary> existingTestSummery = testSummaryRepository.findAll();
        if (existingTestSummery.size() == 0) {
            existingTestSummery.add(new TestSummary());
        }
        existingTestSummery.get(0).setDeviceTestResult(res);
        testSummaryRepository.save(existingTestSummery.get(0));

        System.out.println("Test Successful at " + new DateTime());
    }


    //Test types
    public TestCard NoOfDataTest(Integer batchNumber, Device device, SubTestCase subTestCase, TestCase mainTestCase, DateTime START_TIME, DateTime END_TIME) {
        int minDataCount = Integer.parseInt(subTestCase.getMinVal());
        Query query = new Query();
        query.addCriteria(Criteria.where("kitId").is(device.getId())
                .and("time").gte(START_TIME).lte(END_TIME)
                .and("value").gte(minDataCount).lte(Integer.parseInt(subTestCase.getMaxVal())));

        boolean checkNoOfData = mongoOperations.exists(query, Property.class);

        if (checkNoOfData) {
            return new TestCard(TestResult.SUCCESS, device.getId(), batchNumber, START_TIME, END_TIME, mainTestCase.getMainTestTitle(), subTestCase.getSubTestTitle(), true);
        }
        return new TestCard(TestResult.FAILURE, device.getId(), batchNumber, START_TIME, END_TIME, mainTestCase.getMainTestTitle(), subTestCase.getSubTestTitle(), true);
    }

    public TestCard ExternalInternalHumidityTest(SubTestCase subTestCase, Device device, Integer batchNumber, TestCase mainTestCase, DateTime START_TIME, DateTime END_TIME) {
        boolean checkExternalInternalHumidity = false;
        String subTestTitle = subTestCase.getSubTestTitle();
        if ("External Temperature".equals(subTestTitle)) {
            Boolean resultT = propertyRepository.existsByTimeIntervalAndCodeAndValue(START_TIME, END_TIME, SensorCode.T.getValue(), Integer.parseInt(subTestCase.getMinVal()), Integer.parseInt(subTestCase.getMaxVal()), device.getId());
            checkExternalInternalHumidity = resultT != null ? resultT : false;
        } else if ("Electrical Conductivity".equals(subTestTitle)) {
            Boolean resultE = propertyRepository.existsByTimeIntervalAndCodeAndValue(START_TIME, END_TIME, SensorCode.E.getValue(), Integer.parseInt(subTestCase.getMinVal()), Integer.parseInt(subTestCase.getMaxVal()), device.getId());
            checkExternalInternalHumidity = resultE != null ? resultE : false;
        } else if ("Internal Temperature".equals(subTestTitle)) {
            Boolean resultIT = propertyRepository.existsByTimeIntervalAndCodeAndValue(START_TIME, END_TIME, SensorCode.IT.getValue(), Integer.parseInt(subTestCase.getMinVal()), Integer.parseInt(subTestCase.getMaxVal()), device.getId());
            checkExternalInternalHumidity = resultIT != null ? resultIT : false;
        } else if ("Humidity".equals(subTestTitle)) {
            Boolean resultH = propertyRepository.existsByTimeIntervalAndCodeAndValue(START_TIME, END_TIME, SensorCode.H.getValue(), Integer.parseInt(subTestCase.getMinVal()), Integer.parseInt(subTestCase.getMaxVal()), device.getId());
            checkExternalInternalHumidity = resultH != null ? resultH : false;
        }
        if (checkExternalInternalHumidity) {
            return new TestCard(TestResult.SUCCESS, device.getId(), batchNumber, START_TIME, END_TIME, mainTestCase.getMainTestTitle(), subTestCase.getSubTestTitle(), true);
        }
        return new TestCard(TestResult.FAILURE, device.getId(), batchNumber, START_TIME, END_TIME, mainTestCase.getMainTestTitle(), subTestCase.getSubTestTitle(), true);
    }

    public TestCard BatteryTest(SubTestCase subTestCase, Integer batchNumber, Device device, TestCase mainTestCase, DateTime START_TIME, DateTime END_TIME) {
        List<Property> propertyList = sensorRepository.findByTimeIntervalAndCodeAndKitId(START_TIME, END_TIME, SensorCode.B.getValue(), device.getId());
        boolean checkBattery = true;
        for (int i = 1; i < propertyList.size(); i++) {
            Property currentProperty = propertyList.get(i);
            Property previousProperty = propertyList.get(i - 1);
            if (currentProperty.getValue() <= previousProperty.getValue()) {
                checkBattery = false;
            }
        }
        if (checkBattery) {
            return new TestCard(TestResult.SUCCESS, device.getId(), batchNumber, START_TIME, END_TIME, mainTestCase.getMainTestTitle(), subTestCase.getSubTestTitle(), true);
        }
        return new TestCard(TestResult.FAILURE, device.getId(), batchNumber, START_TIME, END_TIME, mainTestCase.getMainTestTitle(), subTestCase.getSubTestTitle(), true);
    }

    public TestCard CoverageTest(SubTestCase subTestCase, Integer batchNumber, Device device, TestCase mainTestCase, DateTime START_TIME, DateTime END_TIME) {
        DateTime dateTimeNow = MagmaTime.now();
        List<Sensor> allSignalSensors = sensorRepository.findByDeviceIdAndTimeBetween(device.getId(), dateTimeNow.minusWeeks(2), dateTimeNow);
        double maxSignalStrength = 0;
        double minSignalStrength = 0;
        //Define min & max signal strength
        if (subTestCase.getSubTestTitle().equals("High Coverage")) {
            maxSignalStrength = 20;
            minSignalStrength = 20;
        }
        if (subTestCase.getSubTestTitle().equals("Medium Coverage")) {
            maxSignalStrength = 19;
            minSignalStrength = 15;
        }
        if (subTestCase.getSubTestTitle().equals("Low Coverage")) {
            maxSignalStrength = 14;
            minSignalStrength = 10;
        }
        if (subTestCase.getSubTestTitle().equals("Very Low Coverage")) {
            maxSignalStrength = 9;
            minSignalStrength = 2;
        }
        if (subTestCase.getSubTestTitle().equals("No Coverage")) {
            maxSignalStrength = 0;
            minSignalStrength = 0;
        }
        double finalMinSignalStrength = minSignalStrength;
        double finalMaxSignalStrength = maxSignalStrength;
        ArrayList<TestResult> resultList = new ArrayList<>();
        int minDataCount = Integer.parseInt(subTestCase.getMinVal());
        double percentageOfDataWithOutFlashMod = Double.parseDouble(subTestCase.getMinVal());
        List<Sensor> filteredSensorsByStrengthAndInFlashMod = allSignalSensors.stream().filter(sensor -> (
                (START_TIME != null && END_TIME != null &&
                        START_TIME.isAfter(sensor.getTime()) && END_TIME.isBefore(sensor.getTime())) &&
                        (Double.parseDouble(sensor.getValue()) > finalMinSignalStrength) && (Double.parseDouble(sensor.getValue()) < finalMaxSignalStrength))
        ).collect(Collectors.toList());

        if (!subTestCase.getSubTestTitle().equals("No Coverage")) {
            if (filteredSensorsByStrengthAndInFlashMod.size() > minDataCount * (percentageOfDataWithOutFlashMod / 100)) {
                resultList.add(TestResult.SUCCESS);
            } else {
                resultList.add(TestResult.FAILURE);
            }
        }
        boolean check = allSameResult(resultList, TestResult.SUCCESS);
        if (check) {
            return new TestCard(TestResult.SUCCESS, device.getId(), batchNumber, START_TIME, END_TIME, mainTestCase.getMainTestTitle(), subTestCase.getSubTestTitle(), true);
        }
        return new TestCard(TestResult.FAILURE, device.getId(), batchNumber, START_TIME, END_TIME, mainTestCase.getMainTestTitle(), subTestCase.getSubTestTitle(), true);
    }

    public TestCard FlashSavingTestDay(SubTestCase subTestCase, Integer batchNumber, Device device, TestCase mainTestCase, DateTime START_TIME, DateTime END_TIME) {
        DateTime dateTimeNow = MagmaTime.now();
        List<Sensor> totalSensorsOfDevice = sensorRepository.findByDeviceIdAndTimeBetween(device.getId(), dateTimeNow.minusWeeks(2), dateTimeNow);
        Map<Integer, List<Sensor>> dayWiseSensor = new HashMap<>();
        for (Sensor sensor : totalSensorsOfDevice) {
            int dayInWeek = sensor.getCreationDate().getDayOfMonth();
            dayWiseSensor.putIfAbsent(dayInWeek, new ArrayList<>());
            dayWiseSensor.get(dayInWeek).add(sensor);
        }

        List<TestResult> resultList = new ArrayList<>();
        for (List<Sensor> sensorsForDay : dayWiseSensor.values()) {
            if (sensorsForDay.size() < Integer.parseInt(subTestCase.getMinVal())) {
                resultList.add(TestResult.FAILURE);

            } else {
                resultList.add(TestResult.SUCCESS);
            }
        }
        boolean check = allSameResult(resultList, TestResult.SUCCESS);
        if (check) {
            return new TestCard(TestResult.SUCCESS, device.getId(), batchNumber, START_TIME, END_TIME, mainTestCase.getMainTestTitle(), subTestCase.getSubTestTitle(), true);
        }
        return new TestCard(TestResult.FAILURE, device.getId(), batchNumber, START_TIME, END_TIME, mainTestCase.getMainTestTitle(), subTestCase.getSubTestTitle(), true);
    }

    public TestCard FlashSavingTestWeek(SubTestCase subTestCase, Integer batchNumber, Device device, TestCase mainTestCase, DateTime START_TIME, DateTime END_TIME) {
        DateTime dateTimeNow = MagmaTime.now();
        List<Sensor> totalSensorsOfDevice = sensorRepository.findByDeviceIdAndTimeBetween(device.getId(), dateTimeNow.minusWeeks(2), dateTimeNow);
        Map<String, List<Sensor>> weekWiseSensors = new HashMap<>();
        weekWiseSensors.put("week1", new ArrayList<>());
        weekWiseSensors.put("week2", new ArrayList<>());
        for (Sensor sensor : totalSensorsOfDevice) {
            if (sensor.getCreationDate().isAfter(dateTimeNow.minusWeeks(1))) {
                weekWiseSensors.get("week2").add(sensor);
            }
            if (sensor.getCreationDate().isBefore(dateTimeNow.minusWeeks(1))) {
                weekWiseSensors.get("week1").add(sensor);
            }
        }
        List<TestResult> resultList = new ArrayList<>();
        for (List<Sensor> sensorsForWeek : weekWiseSensors.values()) {
            if (sensorsForWeek.size() < Integer.parseInt(subTestCase.getMinVal())) {
                resultList.add(TestResult.FAILURE);
            } else {
                resultList.add(TestResult.SUCCESS);
            }
        }
        boolean check = allSameResult(resultList, TestResult.SUCCESS);
        if (check) {
            return new TestCard(TestResult.SUCCESS, device.getId(), batchNumber, START_TIME, END_TIME, mainTestCase.getMainTestTitle(), subTestCase.getSubTestTitle(), true);
        }
        return new TestCard(TestResult.FAILURE, device.getId(), batchNumber, START_TIME, END_TIME, mainTestCase.getMainTestTitle(), subTestCase.getSubTestTitle(), true);
    }

    private static boolean allSameResult(List<TestResult> testResults, TestResult expected) {
        for (TestResult result : testResults) {
            if (result != expected) {
                return false; // If any result is different from the expected result, return false
            }
        }
        return true; // All test results match the expected result
    }
}
