package com.magma.core.service;

import com.magma.core.data.entity.*;
import com.magma.core.data.repository.*;
import com.magma.core.data.support.DeviceConfiguration;
import com.magma.core.data.support.TestResult;
import com.magma.core.util.MagmaException;
import com.magma.core.util.MagmaStatus;
import com.magma.core.util.SensorCode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class DeviceTestService {

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    TestCaseRepository testCaseRepository;

    @Autowired
    SubTestCaseRepository subTestCaseRepository;

    @Autowired
    SensorRepository sensorRepository;

    @Autowired
    PropertyRepository propertyRepository;

    @Autowired
    MongoOperations mongoOperations;

    @Autowired
    TestSummaryRepository testSummaryRepository;


    private static final Logger logger = LoggerFactory.getLogger(DeviceTestService.class);

    //Get all existing batch numbers
    public List getAllDevicesBatchNumbers() {
        logger.debug("Get All Batch Numbers available in the system");
        List deviceListDistinctBatchNumbers = mongoTemplate.getCollection("device").distinct("batchNumber");
        return deviceListDistinctBatchNumbers;
    }

    //Get Devices of specific batch
    public List<Device> getDevicesOfBatch(Integer batchNumber) {
        logger.debug("Get Request for devices Of batch : {} ", batchNumber);
        List<Device> devicesOfBatch = deviceRepository.findByBatchNumber(batchNumber);
        if (devicesOfBatch == null || devicesOfBatch.size() == 0) {
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        }
        return devicesOfBatch;
    }

    //Get Test cases of the batch
    public List<TestCase> getTestcasesOfBatch(Integer batchNumber) {
        logger.debug("Get Test Cases Of batch : {} devices", batchNumber);

        //Return default test cases if batch number is 0
//        if (batchNumber == 0) {
//            return testCaseRepository.findByBatchNumber(0);
//        }
//
//        //Validation of Batch number
//        List<Device> devicesOfBatch = getDevicesOfBatch(batchNumber);
//
//        //Get-all modified  testCases of specific batch
//        List<TestCase> testCasesOfBatch = testCaseRepository.findByBatchNumber(batchNumber);
//
//        //Get-all non Modified Test cases and merge with modified test cases
//        List<String> testCaseTitles = new ArrayList<>();
//        testCasesOfBatch.forEach(testCase -> testCaseTitles.add(testCase.getMainTestTitle()));
//        List<TestCase> defaultTestCases = testCaseRepository.findByBatchNumberAndMainTestTitleNotIn(0, testCaseTitles);
//        testCasesOfBatch.addAll(defaultTestCases);
//        return testCasesOfBatch;
        return testCaseRepository.findByBatchNumber(batchNumber);
    }

    public List<TestCase> getAllDefaultTestCases() {
        if (testCaseRepository.findByBatchNumber(0) == null) {
            throw new MagmaException(MagmaStatus.TEST_CASE_NOT_FOUND);
        }
        return testCaseRepository.findByBatchNumber(0);
    }

    public List<Map<String, Map<String, Map<String, ArrayList<TestCard>>>>> newDeviceAddInTestSummary(
            List<Map<String, Map<String, Map<String, ArrayList<TestCard>>>>> deviceReportList,
            ArrayList<String> testSummaryDevices, Integer batchNumber) {

        List<Device> devices = deviceRepository.findByBatchNumber(batchNumber);
        devices.removeIf(device -> testSummaryDevices.contains(device.getId()));


        List<TestCase> defaultTestCases = getAllDefaultTestCases();
        for (Device device : devices) {
            Map<String, Map<String, Map<String, ArrayList<TestCard>>>> deviceMap = new HashMap<>();
            Map<String, Map<String, ArrayList<TestCard>>> mainMap = new HashMap<>();
            for (TestCase mainTestCase : defaultTestCases) {
                Map<String, ArrayList<TestCard>> subMap = new HashMap<>();
                if (mainTestCase.getSubTestCases() != null) {
                    for (SubTestCase subTestCase : mainTestCase.getSubTestCases()) {
                        ArrayList<TestCard> testCards = new ArrayList<>();
                        subMap.put(subTestCase.getSubTestTitle(), testCards);
                    }
                }
                mainMap.put(mainTestCase.getMainTestTitle(), subMap);
            }
            deviceMap.put(device.getId(), mainMap);
            deviceReportList.add(deviceMap);
        }
        return deviceReportList;
    }

    //Get Test-case by I'd
    public TestCase getTestCase(String testcaseId) {
        logger.debug("Get a specific testcase by testcaseId :{}", testcaseId);
        TestCase tcDB = testCaseRepository.findOne(testcaseId);
        if (tcDB == null) {
            throw new MagmaException(MagmaStatus.TEST_CASE_NOT_FOUND);
        }
        return tcDB;
    }

    //Get subTestcase of a batch using subTestcaseId
    public SubTestCase getSubTestcaseOfBatch(Integer batchNumber, String subTestcaseId) {
        logger.debug("Get Sub Case - {} Of batch : {} devices", subTestcaseId, batchNumber);

        //Validation of Batch number
        List<Device> deviceList = getDevicesOfBatch(batchNumber);

        SubTestCase subTestCase = subTestCaseRepository.findOne(subTestcaseId);
        if (subTestCase == null) {
            throw new MagmaException(MagmaStatus.TEST_CASE_NOT_FOUND);
        }
        return subTestCase;
    }

    //Get all subTestcases of a batch
    public List<SubTestCase> getSubTestcasesOfBatch(Integer batchNumber) {
        logger.debug("Get subTestcases of Batch :{}", batchNumber);

        //If bach number is 0 , return default-TestCases
        if (batchNumber == 0) {
            return subTestCaseRepository.findByBatchNumber(0);
        }

        //Validation of batchNumber
        List<Device> devicesOfBatch = getDevicesOfBatch(batchNumber);

        //Get-all  modified  sub testCases of specific batch
        List<SubTestCase> subTestCasesOfBatch = subTestCaseRepository.findByBatchNumber(batchNumber);

        //Get-all non modified sub testcases of a batch and merge with modified testcases
        List<String> subTestCaseTitles = new ArrayList<>();
        subTestCasesOfBatch.forEach(testCase -> subTestCaseTitles.add(testCase.getSubTestTitle()));
        List<SubTestCase> defaultTestCases = subTestCaseRepository.findByBatchNumberAndSubTestTitleNotIn(0, subTestCaseTitles);
        subTestCasesOfBatch.addAll(defaultTestCases);
        return subTestCasesOfBatch;
    }


    private static boolean allSameResult(List<TestResult> testResults, TestResult expected) {
        for (TestResult result : testResults) {
            if (result != expected) {
                return false; // If any result is different from the expected result, return false
            }
        }
        return true; // All test results match the expected result
    }


    public TestCard NoOfDataTest(Integer batchNumber, Device device, SubTestCase subTestCase, List<Sensor> totalSensor, TestCase mainTestCase, DateTime START_TIME, DateTime END_TIME) {
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

    public TestCard CoverageTest(SubTestCase subTestCase, Integer batchNumber, Device device, double finalMinSignalStrength, double finalMaxSignalStrength, List<Sensor> allSignalSensors, TestCase mainTestCase, DateTime START_TIME, DateTime END_TIME) {
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


    public TestCard FlashSavingTestDay(SubTestCase subTestCase, Integer batchNumber, Device device, Map<Integer, List<Sensor>> dayWiseSensor, TestCase mainTestCase, DateTime START_TIME, DateTime END_TIME) {
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


    public TestCard FlashSavingTestWeek(SubTestCase subTestCase, Integer batchNumber, Device device, Map<String, List<Sensor>> weekWiseSensor, TestCase mainTestCase, DateTime START_TIME, DateTime END_TIME) {
        List<TestResult> resultList = new ArrayList<>();
        for (List<Sensor> sensorsForWeek : weekWiseSensor.values()) {
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


    public TestCard BatteryTest(SubTestCase subTestCase, Integer batchNumber, List<Sensor> batterySensors, Device device, TestCase mainTestCase, DateTime START_TIME, DateTime END_TIME) {
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


    public TestCard ExternalInternalHumidityTest(SubTestCase subTestCase, List<Double> sensorValues, Device device, Integer batchNumber, TestCase mainTestCase, DateTime START_TIME, DateTime END_TIME) {
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


    public void checkAllTheTypesofTestCases(
            SubTestCase subTestCase, TestCase mainTestCase,
            Integer batchNumber, Device device,
            List<Sensor> totalSensorsOfDevice,
            DateTime startTime, DateTime endTime,
            List<Sensor> allSignalSensors,
            Map<Integer, List<Sensor>> dayWiseSensor,
            ArrayList<TestCard> testList,
            List<TestCard> testCardList,
            Map<String, List<Sensor>> weekWiseSensors,
            List<Sensor> batterySensors,
            List<Sensor> externalTempSensors,
            List<Sensor> internalTempSensors,
            List<Sensor> ecSensors,
            List<Sensor> humiditySensors
    ) {
        if (subTestCase.getSubTestTitle().equals("No Of Data")) {
            TestCard noOfDataList = NoOfDataTest(batchNumber, device, subTestCase, totalSensorsOfDevice, mainTestCase, startTime, endTime);
            testList.add(noOfDataList);
            testCardList.add(noOfDataList);
        } else if (mainTestCase.getMainTestTitle().equals("Coverage Test")) {
            String subTestTitle = subTestCase.getSubTestTitle();
            double maxSignalStrength = 0;
            double minSignalStrength = 0;
            //Define min & max signal strength
            if (subTestTitle.equals("High Coverage")) {
                maxSignalStrength = 20;
                minSignalStrength = 20;
            }
            if (subTestTitle.equals("Medium Coverage")) {
                maxSignalStrength = 19;
                minSignalStrength = 15;
            }
            if (subTestTitle.equals("Low Coverage")) {
                maxSignalStrength = 14;
                minSignalStrength = 10;
            }
            if (subTestTitle.equals("Very Low Coverage")) {
                maxSignalStrength = 9;
                minSignalStrength = 2;
            }
            if (subTestTitle.equals("No Coverage")) {
                maxSignalStrength = 0;
                minSignalStrength = 0;
            }
            double finalMinSignalStrength = minSignalStrength;
            double finalMaxSignalStrength = maxSignalStrength;
            TestCard coverageTestCards = CoverageTest(subTestCase, batchNumber, device, finalMinSignalStrength, finalMaxSignalStrength, allSignalSensors, mainTestCase, startTime, endTime);
            testCardList.add(coverageTestCards);
            testList.add(coverageTestCards);
        } else if (mainTestCase.getMainTestTitle().equals("Flash Saving Test")) {
            String subTestTitle = subTestCase.getSubTestTitle();
            if (subTestTitle.equals("Flash Saving Test Day")) {
                TestCard flashSavingTestDayTestCards = FlashSavingTestDay(subTestCase, batchNumber, device, dayWiseSensor, mainTestCase, startTime, endTime);
                testCardList.add(flashSavingTestDayTestCards);
                testList.add(flashSavingTestDayTestCards);
            } else if (subTestTitle.equals("Flash Saving Test Week")) {
                TestCard flashSavingTestWeekTestCards = FlashSavingTestWeek(subTestCase, batchNumber, device, weekWiseSensors, mainTestCase, startTime, endTime);
                testCardList.add(flashSavingTestWeekTestCards);
                testList.add(flashSavingTestWeekTestCards);
            }
        } else if (subTestCase.getSubTestTitle().equals("Battery")) {
            TestCard batteryTestCards = BatteryTest(subTestCase, batchNumber, batterySensors, device, mainTestCase, startTime, endTime);
            testCardList.add(batteryTestCards);
            testList.add(batteryTestCards);
        } else if (subTestCase.getSubTestTitle().equals("External Temperature") ||
                subTestCase.getSubTestTitle().equals("Electrical Conductivity") ||
                subTestCase.getSubTestTitle().equals("Internal Temperature") ||
                subTestCase.getSubTestTitle().equals("Humidity")) {
            String subTestType = subTestCase.getSubTestTitle();
            List<Sensor> sensorsForCode = new ArrayList<>();
            if (subTestType.equals("External Temperature")) {
                sensorsForCode = externalTempSensors;
            }
            if (subTestType.equals("Electrical Conductivity")) {
                sensorsForCode = ecSensors;
            } //TODO NeedTo Change code Of EC
            if (subTestType.equals("Internal Temperature")) {
                sensorsForCode = internalTempSensors;
            }
            if (subTestType.equals("Humidity")) {
                sensorsForCode = humiditySensors;
            }
            if (sensorsForCode.size() > 0) {
                List<Double> sensorValues = new ArrayList<>();
                for (Sensor testSensor : sensorsForCode) {
                    if (isNumericCheck(testSensor.getValue())) {
                        sensorValues.add(Double.valueOf(testSensor.getValue()));
                    }
                }
                TestCard ExternalInternalHumidityTestCards = ExternalInternalHumidityTest(subTestCase, sensorValues, device, batchNumber, mainTestCase, startTime, endTime);
                testCardList.add(ExternalInternalHumidityTestCards);
                testList.add(ExternalInternalHumidityTestCards);
            }
        }
    }

    public void checkDateAndTimeInBetweenTestTime(
            SubTestCase subTestCase,
            TestCase mainTestCase,
            Integer batchNumber,
            Device device,
            DateTime startTime,
            DateTime endTime,
            ArrayList<TestCard> testList,
            List<TestCard> testCardList

    ) {
        testCardList.add(new TestCard(TestResult.ON_GOING, device.getId(), batchNumber, startTime, endTime, mainTestCase.getMainTestTitle(), subTestCase.getSubTestTitle(), false));
        testList.add(new TestCard(TestResult.ON_GOING, device.getId(), batchNumber, startTime, endTime, mainTestCase.getMainTestTitle(), subTestCase.getSubTestTitle(), false));
    }

    public void checkUpdateTheTypesofTestCases(
            SubTestCase subTestCase, TestCase mainTestCase,
            Integer batchNumber, Device device,
            List<Sensor> totalSensorsOfDevice,
            DateTime startTime, DateTime endTime,
            List<Sensor> allSignalSensors,
            Map<Integer, List<Sensor>> dayWiseSensor,
            List<TestCard> testCardList,
            Map<String, List<Sensor>> weekWiseSensors,
            List<Sensor> batterySensors,
            List<Sensor> externalTempSensors,
            List<Sensor> internalTempSensors,
            List<Sensor> ecSensors,
            List<Sensor> humiditySensors,
            TestCard test
    ) {
        if (subTestCase.getSubTestTitle().equals("No Of Data")) {
            TestCard noOfDataList = NoOfDataTest(batchNumber, device, subTestCase, totalSensorsOfDevice, mainTestCase, startTime, endTime);
            testCardList.add(noOfDataList);
            test.setTestResult(noOfDataList.getTestResult());
            test.setCheck(true);
        } else if (mainTestCase.getMainTestTitle().equals("Coverage Test")) {
            String subTestTitle = subTestCase.getSubTestTitle();
            double maxSignalStrength = 0;
            double minSignalStrength = 0;
            //Define min & max signal strength
            if (subTestTitle.equals("High Coverage")) {
                maxSignalStrength = 20;
                minSignalStrength = 20;
            }
            if (subTestTitle.equals("Medium Coverage")) {
                maxSignalStrength = 19;
                minSignalStrength = 15;
            }
            if (subTestTitle.equals("Low Coverage")) {
                maxSignalStrength = 14;
                minSignalStrength = 10;
            }
            if (subTestTitle.equals("Very Low Coverage")) {
                maxSignalStrength = 9;
                minSignalStrength = 2;
            }
            if (subTestTitle.equals("No Coverage")) {
                maxSignalStrength = 0;
                minSignalStrength = 0;
            }
            double finalMinSignalStrength = minSignalStrength;
            double finalMaxSignalStrength = maxSignalStrength;
            TestCard coverageTestCards = CoverageTest(subTestCase, batchNumber, device, finalMinSignalStrength, finalMaxSignalStrength, allSignalSensors, mainTestCase, startTime, endTime);
            testCardList.add(coverageTestCards);
            test.setTestResult(coverageTestCards.getTestResult());
            test.setCheck(true);
        } else if (mainTestCase.getMainTestTitle().equals("Flash Saving Test")) {
            String subTestTitle = subTestCase.getSubTestTitle();
            if (subTestTitle.equals("Flash Saving Test Day")) {
                TestCard flashSavingTestDayTestCards = FlashSavingTestDay(subTestCase, batchNumber, device, dayWiseSensor, mainTestCase, startTime, endTime);
                testCardList.add(flashSavingTestDayTestCards);
                test.setTestResult(flashSavingTestDayTestCards.getTestResult());
                test.setCheck(true);
            } else if (subTestTitle.equals("Flash Saving Test Week")) {
                TestCard flashSavingTestWeekTestCards = FlashSavingTestWeek(subTestCase, batchNumber, device, weekWiseSensors, mainTestCase, startTime, endTime);
                testCardList.add(flashSavingTestWeekTestCards);
                test.setTestResult(flashSavingTestWeekTestCards.getTestResult());
                test.setCheck(true);
            }
        } else if (subTestCase.getSubTestTitle().equals("Battery")) {
            TestCard batteryTestCards = BatteryTest(subTestCase, batchNumber, batterySensors, device, mainTestCase, startTime, endTime);
            testCardList.add(batteryTestCards);
            test.setTestResult(batteryTestCards.getTestResult());
            test.setCheck(true);
        } else if (subTestCase.getSubTestTitle().equals("External Temperature") ||
                subTestCase.getSubTestTitle().equals("Electrical Conductivity") ||
                subTestCase.getSubTestTitle().equals("Internal Temperature") ||
                subTestCase.getSubTestTitle().equals("Humidity")) {
            String subTestType = subTestCase.getSubTestTitle();
            List<Sensor> sensorsForCode = new ArrayList<>();
            if (subTestType.equals("External Temperature")) {
                sensorsForCode = externalTempSensors;
            }
            if (subTestType.equals("Electrical Conductivity")) {
                sensorsForCode = ecSensors;
            } //TODO NeedTo Change code Of EC
            if (subTestType.equals("Internal Temperature")) {
                sensorsForCode = internalTempSensors;
            }
            if (subTestType.equals("Humidity")) {
                sensorsForCode = humiditySensors;
            }
            if (sensorsForCode.size() >= 0) {
                List<Double> sensorValues = new ArrayList<>();
                for (Sensor testSensor : sensorsForCode) {
                    if (isNumericCheck(testSensor.getValue())) {
                        sensorValues.add(Double.valueOf(testSensor.getValue()));
                    }
                }
                TestCard ExternalInternalHumidityTestCards = ExternalInternalHumidityTest(subTestCase, sensorValues, device, batchNumber, mainTestCase, startTime, endTime);
                testCardList.add(ExternalInternalHumidityTestCards);
                test.setTestResult(ExternalInternalHumidityTestCards.getTestResult());
                test.setCheck(true);
            }
        }
    }


    public List<Map<String, Map<String, Map<String, ArrayList<TestCard>>>>> generateDeviceTestMap(Integer batchNumber) {
        List<TestCase> mainTestCases = getAllDefaultTestCases();
        List<Device> deviceList = deviceRepository.findByBatchNumber(batchNumber);
        ArrayList<Map<String, Map<String, Map<String, ArrayList<TestCard>>>>> result = new ArrayList<>();
        for (Device device : deviceList) {
            Map<String, Map<String, Map<String, ArrayList<TestCard>>>> deviceMap = new HashMap<>();
            Map<String, Map<String, ArrayList<TestCard>>> mainMap = new HashMap<>();
            for (TestCase mainTestCase : mainTestCases) {
                Map<String, ArrayList<TestCard>> subMap = new HashMap<>();
                for (SubTestCase subTestCase : mainTestCase.getSubTestCases()) {
                    subMap.put(subTestCase.getSubTestTitle(), new ArrayList<>());
                }
                mainMap.put(mainTestCase.getMainTestTitle(), subMap);
            }
            deviceMap.put(device.getId(), mainMap);
            result.add(deviceMap);
        }
        return result;
    }

    //Get Test Summary Details of devices in a  batch
//    public TestSummary getTestSummaryForBatchOfDevices(Integer batchNumber) {
//        List<Map<String, Map<String, Map<String, ArrayList<TestCard>>>>> devicesTestList = new ArrayList<>();
//
//        TestSummary testSummary = testSummaryRepository.findByBatchNumber(batchNumber);
//        if (testSummary == null) {
//            testSummary = new TestSummary();
//        } else {
//            devicesTestList = testSummary.getDeviceTestResult();
//        }
//
//        DateTime currentDateTime = DateTime.now().withZone(DateTimeZone.forID("Asia/Colombo"));
//
//        // if new devices are registered then we have to add those devices in Table before test
//        if (testSummary.getDeviceTestResult() != null && !testSummary.getDeviceTestResult().isEmpty()) {
//
//            ArrayList<String> testSummaryDevices = new ArrayList<>();
//
//            for (Map<String, Map<String, Map<String, ArrayList<TestCard>>>> deviceMap : testSummary.getDeviceTestResult()) {
//                testSummaryDevices.addAll(deviceMap.keySet());
//            }
//
//            List<Device> allDevices = deviceRepository.findAll();
//            if (allDevices != null) {
//                if (allDevices.size() != testSummaryDevices.size()) {
//                    devicesTestList = newDeviceAddInTestSummary(testSummary.getDeviceTestResult(), testSummaryDevices, batchNumber);
//                }
//            }
//        }
//
//        if (devicesTestList.isEmpty()) {
//            devicesTestList = generateDeviceTestMap(batchNumber);
//        }
//
//
//        //Collect devices & testCases of a Batch
//        ArrayList<Device> devicesOfBatch = (ArrayList<Device>) getDevicesOfBatch(batchNumber);
//        ArrayList<TestCase> testCasesOfBatch = (ArrayList<TestCase>) getTestcasesOfBatch(batchNumber);
//
//        if (devicesOfBatch == null || testCasesOfBatch == null || devicesOfBatch.isEmpty()) {
//            return new TestSummary();
//        }
//
//        //Create Return Object and structure
//
//        List<TestCard> testCardList = new ArrayList<>();
//        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//
//
//        //Process Testcases against each device
//        for (Device device : devicesOfBatch) {
//            DateTime dateTimeNow = MagmaTime.now();
//            Map<String, Map<String, Map<String, ArrayList<TestCard>>>> deviceTestMap = new HashMap<>();
//            boolean checkListNotExists = false;
//            boolean deviceIdFound = false;
//
//            //Get & collect sensors for a device
//            List<Sensor> totalSensorsOfDevice = sensorRepository.findByDeviceIdAndTimeBetween(device.getId(), dateTimeNow.minusWeeks(2), dateTimeNow); //2 weeks sensors
//            List<Sensor> allSignalSensors = new ArrayList<>();
//            List<Sensor> batterySensors = new ArrayList<>();
//            List<Sensor> externalTempSensors = new ArrayList<>();
//            List<Sensor> internalTempSensors = new ArrayList<>();
//            List<Sensor> ecSensors = new ArrayList<>();
//            List<Sensor> humiditySensors = new ArrayList<>();
//
//            Map<String, List<Sensor>> weekWiseSensors = new HashMap<>();
//            weekWiseSensors.put("week1", new ArrayList<>());
//            weekWiseSensors.put("week2", new ArrayList<>());
//            Map<Integer, List<Sensor>> dayWiseSensor = new HashMap<>();
//
//            for (Sensor sensor : totalSensorsOfDevice) {
//                if (sensor.getCode().equals(SensorCode.IT)) {
//                    internalTempSensors.add(sensor);
//                }
//                if (sensor.getCode().equals(SensorCode.T)) {
//                    externalTempSensors.add(sensor);
//                }
//                if (sensor.getCode().equals(SensorCode.B)) {
//                    batterySensors.add(sensor);
//                }
//                if (sensor.getCode().equals(SensorCode.SS)) {
//                    allSignalSensors.add(sensor);
//                }
//                if (sensor.getCode().equals(SensorCode.E)) {
//                    ecSensors.add(sensor);
//                }
//                if (sensor.getCode().equals(SensorCode.H)) {
//                    humiditySensors.add(sensor);
//                }
//                if (sensor.getCreationDate().isAfter(dateTimeNow.minusWeeks(1))) {
//                    weekWiseSensors.get("week2").add(sensor);
//                }
//                if (sensor.getCreationDate().isBefore(dateTimeNow.minusWeeks(1))) {
//                    weekWiseSensors.get("week1").add(sensor);
//                }
//                int dayInWeek = sensor.getCreationDate().getDayOfMonth();
//                dayWiseSensor.putIfAbsent(dayInWeek, new ArrayList<>());
//                dayWiseSensor.get(dayInWeek).add(sensor);
//            }
//
//            Map<String, Map<String, ArrayList<TestCard>>> mainTestMap = new HashMap<>();
//            //Process Test Cases
//            if (testCasesOfBatch != null || !testCasesOfBatch.isEmpty()) {
//                for (TestCase mainTestCase : testCasesOfBatch) {
//                    if (mainTestCase.getDevices().contains(device.getId())) {
//                        Map<String, ArrayList<TestCard>> subTestMap = new HashMap<>();
//                        for (SubTestCase subTestCase : mainTestCase.getSubTestCases()) {
//                            if (subTestCase.getDevices().contains(device.getId()) || mainTestCase.getBatchNumber() == 0) {
//                                ArrayList<TestCard> valueMap = new ArrayList<>();
//                                Map<String, Map<String, DateTime>> subTestCaseTimeBetween = subTestCase.getSubTestCaseTimeBetween();
//
//                                if (devicesTestList.size() > 0) {
//                                    checkListNotExists = false;
//
//                                    for (String key : subTestCaseTimeBetween.keySet()) {
//                                        boolean timeFoundTemp = false;
//                                        DateTime startTime = subTestCaseTimeBetween.get(key).get("startTime").withZoneRetainFields(DateTimeZone.forID("Asia/Colombo"));
//                                        DateTime endTime = subTestCaseTimeBetween.get(key).get("endTime").withZoneRetainFields(DateTimeZone.forID("Asia/Colombo"));
//                                        for (Map<String, Map<String, Map<String, ArrayList<TestCard>>>> deviceMap : devicesTestList) {
//                                            if (deviceMap.containsKey(device.getId())) {
//                                                Map<String, Map<String, ArrayList<TestCard>>> mainMap = deviceMap.get(device.getId());
//                                                if (mainMap != null) {
//                                                    Map<String, ArrayList<TestCard>> subMap = mainMap.get(mainTestCase.getMainTestTitle());
//                                                    if (subMap != null) {
//                                                        ArrayList<TestCard> testList = subMap.get(subTestCase.getSubTestTitle());
//
//                                                        if (testList != null) {
//                                                            for (TestCard test : testList) {
//
//                                                                if (test.getSubTestCaseStartTime().toString().equals(startTime.toString()) && test.getSubTestCaseEndTime().toString().equals(endTime.toString()) && test.getDeviceId().equals(device.getId())) {
//                                                                    timeFoundTemp = true;
//                                                                }
//
//                                                                if (DateCheck(startTime, endTime, currentDateTime) && !test.isCheck()) {
//                                                                    checkUpdateTheTypesofTestCases(subTestCase, mainTestCase, batchNumber, device, totalSensorsOfDevice, startTime, endTime, allSignalSensors, dayWiseSensor, testCardList, weekWiseSensors, batterySensors, externalTempSensors, internalTempSensors, ecSensors, humiditySensors, test);
//                                                                } else if (TimeBetweenStartAndEnd(startTime, endTime, currentDateTime) && !test.isCheck()) {
//                                                                    testCardList.add(new TestCard(TestResult.ON_GOING, device.getId(), batchNumber, startTime, endTime, mainTestCase.getMainTestTitle(), subTestCase.getSubTestTitle()));
//                                                                    test.setTestResult(TestResult.ON_GOING);
//                                                                    test.setCheck(false);
//                                                                }
//
//                                                            }
//                                                            if (!timeFoundTemp) {
//                                                                //test and add
//                                                                if (DateCheck(startTime, endTime, currentDateTime)) {
//                                                                    checkAllTheTypesofTestCases(subTestCase, mainTestCase, batchNumber, device, totalSensorsOfDevice, startTime, endTime, allSignalSensors, dayWiseSensor, testList, testCardList, weekWiseSensors, batterySensors, externalTempSensors, internalTempSensors, ecSensors, humiditySensors);
//                                                                } else if (TimeBetweenStartAndEnd(startTime, endTime, currentDateTime)) {
//                                                                    checkDateAndTimeInBetweenTestTime(subTestCase, mainTestCase, batchNumber, device, startTime, endTime, testList, testCardList);
//                                                                }
//                                                            }
//
//
//                                                            subMap.replace(subTestCase.getSubTestTitle(), testList);
//                                                        } else {
//                                                            // testList is null
//                                                            testList = new ArrayList<>();
//                                                            if (DateCheck(startTime, endTime, currentDateTime)) {
//                                                                // test
//                                                                checkAllTheTypesofTestCases(subTestCase, mainTestCase, batchNumber, device, totalSensorsOfDevice, startTime, endTime, allSignalSensors, dayWiseSensor, testList, testCardList, weekWiseSensors, batterySensors, externalTempSensors, internalTempSensors, ecSensors, humiditySensors);
//                                                            } else if (TimeBetweenStartAndEnd(startTime, endTime, currentDateTime)) {
//                                                                checkDateAndTimeInBetweenTestTime(subTestCase, mainTestCase, batchNumber, device, startTime, endTime, testList, testCardList);
//                                                            }
//                                                            subMap.put(subTestCase.getSubTestTitle(), testList);
//                                                        }
//                                                    } else {
//                                                        subMap = new HashMap<String, ArrayList<TestCard>>();
//                                                        ArrayList<TestCard> list = new ArrayList<>();
//                                                        if (DateCheck(startTime, endTime, currentDateTime)) {
//                                                            // test
//                                                            checkAllTheTypesofTestCases(subTestCase, mainTestCase, batchNumber, device, totalSensorsOfDevice, startTime, endTime, allSignalSensors, dayWiseSensor, list, testCardList, weekWiseSensors, batterySensors, externalTempSensors, internalTempSensors, ecSensors, humiditySensors);
//                                                        } else if (TimeBetweenStartAndEnd(startTime, endTime, currentDateTime)) {
//                                                            checkDateAndTimeInBetweenTestTime(subTestCase, mainTestCase, batchNumber, device, startTime, endTime, list, testCardList);
//                                                        }
//                                                        subMap.put(subTestCase.getSubTestTitle(), list);
//                                                        mainMap.replace(mainTestCase.getMainTestTitle(), subMap);
//                                                    }
//                                                } else {
//                                                    // test
//                                                    mainMap = new HashMap<>();
//                                                    Map<String, ArrayList<TestCard>> subMap = new HashMap<>();
//                                                    ArrayList<TestCard> testList = new ArrayList<>();
//
//                                                    if (DateCheck(startTime, endTime, currentDateTime)) {
//                                                        // test
//                                                        checkAllTheTypesofTestCases(subTestCase, mainTestCase, batchNumber, device, totalSensorsOfDevice, startTime, endTime, allSignalSensors, dayWiseSensor, testList, testCardList, weekWiseSensors, batterySensors, externalTempSensors, internalTempSensors, ecSensors, humiditySensors);
//                                                    } else if (TimeBetweenStartAndEnd(startTime, endTime, currentDateTime)) {
//                                                        checkDateAndTimeInBetweenTestTime(subTestCase, mainTestCase, batchNumber, device, startTime, endTime, testList, testCardList);
//                                                    }
//                                                    subMap.put(subTestCase.getSubTestTitle(), testList);
//                                                    mainMap.put(mainTestCase.getMainTestTitle(), subMap);
//                                                    deviceMap.replace(device.getId(), mainMap);
//                                                }
//
//                                            }
//                                        }
//                                    }
//                                }
//                            } //check device in subtestcaselist
//                        }// subTestCases Iterate
//
//
//                        if (checkListNotExists) {
//                            mainTestMap.put(mainTestCase.getMainTestTitle(), subTestMap);
//                            deviceTestMap.put(device.getId(), mainTestMap);
//                        }
//
//                    } // need to change this one
//                }
//            }
//            if (checkListNotExists) {
//                devicesTestList.add(deviceTestMap);
//            }
//
//        }
//
//        // MainTestcase -> SubTestcase -> TestResult -> Count
//        Map<String, Map<String, Map<TestResult, Integer>>> reportMap = testSummary.getNumericalResult();
//        if (reportMap == null) {
//            reportMap = new HashMap<>();
//        }
//
//        if (!testCardList.isEmpty()) {
//            for (TestCard testCard : testCardList) {
//                if (reportMap != null) {
//                    if (reportMap.containsKey(testCard.getMainTestTitle())) {
//                        Map<String, Map<TestResult, Integer>> subTestMap = reportMap.get(testCard.getMainTestTitle());
//                        if (subTestMap != null) {
//                            if (subTestMap.containsKey(testCard.getSubTestTitle())) {
//                                Map<TestResult, Integer> test = subTestMap.get(testCard.getSubTestTitle());
//                                if (test != null) {
//                                    if (test.containsKey(testCard.getTestResult())) {
//                                        Integer count = test.get(testCard.getTestResult());
//                                        count++;
//                                        test.replace(testCard.getTestResult(), count);
//                                        subTestMap.replace(testCard.getSubTestTitle(), test);
//                                        reportMap.replace(testCard.getMainTestTitle(), subTestMap);
//                                    } else {
//                                        test.put(testCard.getTestResult(), 1);
//                                        subTestMap.replace(testCard.getSubTestTitle(), test);
//                                        reportMap.replace(testCard.getMainTestTitle(), subTestMap);
//                                    }
//                                } else {
//                                    Map<TestResult, Integer> testMap = new HashMap<>();
//                                    testMap.put(testCard.getTestResult(), 1);
//                                    subTestMap.put(testCard.getSubTestTitle(), testMap);
//                                    reportMap.replace(testCard.getMainTestTitle(), subTestMap);
//                                }
//                            } else {
//                                Map<TestResult, Integer> temp = new HashMap<>();
//                                temp.put(testCard.getTestResult(), 1);
//                                subTestMap.put(testCard.getSubTestTitle(), temp);
//                                reportMap.replace(testCard.getMainTestTitle(), subTestMap);
//                            }
//                        } else {
//                            Map<String, Map<TestResult, Integer>> subTemp = new HashMap<>();
//                            Map<TestResult, Integer> testTemp = new HashMap<>();
//                            testTemp.put(testCard.getTestResult(), 1);
//                            subTemp.put(testCard.getSubTestTitle(), testTemp);
//                            reportMap.put(testCard.getMainTestTitle(), subTemp);
//                        }
//                    } else {
//                        Map<String, Map<TestResult, Integer>> hash = new HashMap<>();
//                        Map<TestResult, Integer> test = new HashMap<>();
//                        test.put(testCard.getTestResult(), 1);
//                        hash.put(testCard.getSubTestTitle(), test);
//                        reportMap.put(testCard.getMainTestTitle(), hash);
//                    }
//                } else {
//                    reportMap = new HashMap<>();
//                    Map<String, Map<TestResult, Integer>> subMap = new HashMap<>();
//                    Map<TestResult, Integer> testMap = new HashMap<>();
//                    testMap.put(testCard.getTestResult(), 1);
//                    subMap.put(testCard.getSubTestTitle(), testMap);
//                    reportMap.put(testCard.getMainTestTitle(), subMap);
//                }
//            }
//        }
//
//        Map<String, String> productChart = produceChartData(reportMap);
//
//        if (productChart == null) {
//            productChart = new HashMap<>();
//        }
//
//        testSummary.setBatchNumber(batchNumber);
//        testSummary.setTestSuccessRate(productChart);
//        testSummary.setNumericalResult(reportMap);
//        testSummary.setDeviceTestResult(devicesTestList);
//        testSummaryRepository.save(testSummary);
//
//        return testSummary;
//    }


    public Map<String, String> produceChartData(Map<String, Map<String, Map<TestResult, Integer>>> numericalResult) {
        Map<String, String> testSuccessRate = new HashMap<>();
        Map<String, Integer> testSuccessCount = new HashMap<>();
        testSuccessCount.put("0-20", 0);
        testSuccessCount.put("21-40", 0);
        testSuccessCount.put("41-60", 0);
        testSuccessCount.put("61-80", 0);
        testSuccessCount.put("81-100", 0);

        testSuccessRate.put("0-20", "0");
        testSuccessRate.put("21-40", "0");
        testSuccessRate.put("41-60", "0");
        testSuccessRate.put("61-80", "0");
        testSuccessRate.put("81-100", "0");

        //0-20 indicates TestCases Which have 0-20 devices , 20-success ratio compared with other categories
        //testSuccessRate.put("0-20","20");

        //Get subTestCase Success Map
        Map<String, Integer> subTestCaseSuccessMap = new HashMap<>();
        for (Map<String, Map<TestResult, Integer>> subTestCases : numericalResult.values()) {
            for (String subTestCase : subTestCases.keySet()) {
                Map<TestResult, Integer> resultOfSubTestCase = subTestCases.get(subTestCase);
                subTestCaseSuccessMap.putIfAbsent(subTestCase, 0);
                if (resultOfSubTestCase.containsKey(TestResult.SUCCESS) && resultOfSubTestCase.get(TestResult.SUCCESS) != null) {
                    subTestCaseSuccessMap.replace(subTestCase, subTestCaseSuccessMap.get(subTestCase) + resultOfSubTestCase.get(TestResult.SUCCESS));
                }
            }
        }


        //Get test Success Count
        int totalCount = 0;
        if (!subTestCaseSuccessMap.isEmpty()) {
            for (Integer successDeviceCases : subTestCaseSuccessMap.values()) {
                totalCount++;
                if (successDeviceCases > 0 && successDeviceCases <= 20) {
                    testSuccessCount.replace("0-20", testSuccessCount.get("0-20") + 1);
                } else if (successDeviceCases > 21 && successDeviceCases <= 40) {
                    testSuccessCount.replace("21-40", testSuccessCount.get("21-40") + 1);
                } else if (successDeviceCases > 41 && successDeviceCases <= 60) {
                    testSuccessCount.replace("41-60", testSuccessCount.get("41-60") + 1);
                } else if (successDeviceCases > 61 && successDeviceCases <= 80) {
                    testSuccessCount.replace("61-80", testSuccessCount.get("61-80") + 1);
                } else if (successDeviceCases > 81 && successDeviceCases <= 100) {
                    testSuccessCount.replace("81-100", testSuccessCount.get("81-100") + 1);
                }
            }


            if (totalCount == 0) {
                testSuccessRate.replace("0-20", "0");
                testSuccessRate.replace("21-40", "0");
                testSuccessRate.replace("41-60", "0");
                testSuccessRate.replace("61-80", "0");
                testSuccessRate.replace("81-100", "0");
                return testSuccessRate;
            }


        }


        //PrepareOutputMap
        if (totalCount != 0) {
            for (String key : testSuccessCount.keySet()) {
                if (key.equals("0-20")) {
                    testSuccessRate.replace("0-20", String.valueOf(testSuccessCount.get("0-20") / totalCount * 100));
                } else if (key.equals("21-40")) {
                    testSuccessRate.replace("21-40", String.valueOf(testSuccessCount.get("21-40") / totalCount * 100));
                } else if (key.equals("41-60")) {
                    testSuccessRate.replace("41-60", String.valueOf(testSuccessCount.get("41-60") / totalCount * 100));
                } else if (key.equals("61-80")) {
                    testSuccessRate.replace("61-80", String.valueOf(testSuccessCount.get("61-80") / totalCount * 100));
                } else if (key.equals("81-100")) {
                    testSuccessRate.replace("81-100", String.valueOf(testSuccessCount.get("81-100") / totalCount * 100));
                }
            }

        }


        return testSuccessRate;
    }

    //Assign batch number to a device
    public Device configureBatchNumber(String deviceId, Integer batchNumber) {
        Device deviceDB = deviceRepository.findOne(deviceId);
        if (batchNumber == null || batchNumber == 0) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        if (deviceDB == null) {
            throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
        }
        deviceDB.setBatchNumber(batchNumber);
        return deviceRepository.save(deviceDB);
    }

    //Modify default values of sub-test cases
    public TestCase configureSubTestcaseOfBatch(Integer batch, String testcaseId, String subTestcaseId, SubTestCase subTestCase) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        SubTestCase subTestcaseDB = getSubTestcaseOfBatch(batch, subTestcaseId);
        TestCase testCaseDB = getTestCase(testcaseId);

        //Validate Range of Integer Input [No Of Data must be in Integer]
        if (subTestcaseDB.getSubTestTitle().equals("No Of Data")) {
            isIntegerValidate(subTestCase.getMaxVal());
            isIntegerValidate(subTestCase.getMinVal());
        }

        //Validate Other Inputs - must be in Numeric
        isNumericValidate(subTestCase.getMaxVal());
        isNumericValidate(subTestCase.getMinVal());

        //If Main test case is default -> create a Test case for that batch and save
        if (testCaseDB.getBatchNumber() == 0) {
            TestCase modifiedTestcase = new TestCase();
            BeanUtils.copyProperties(testCaseDB, modifiedTestcase);
            modifiedTestcase.setBatchNumber(batch);
            modifiedTestcase.setId(null);
            testCaseDB = testCaseRepository.save(modifiedTestcase);
        }

        //Modify subTestCase
        List<String> allExistingSubTestcaseIds = testCaseDB.getSubTestCases().stream().map(SubTestCase::getId).collect(Collectors.toList());

        int indexOfExistingSubTestcase = allExistingSubTestcaseIds.indexOf(subTestcaseId);

        if (indexOfExistingSubTestcase == -1) {
            throw new MagmaException(MagmaStatus.TEST_CASE_NOT_FOUND);
        }

        //If Sub test case is default -> create a sub Test case for that batch and save
        if (subTestcaseDB.getBatchNumber() == 0) {
            SubTestCase modifiedSubTestcase = new SubTestCase();
            BeanUtils.copyProperties(subTestcaseDB, modifiedSubTestcase);
            modifiedSubTestcase.setBatchNumber(batch);
            modifiedSubTestcase.setId(null);
            subTestcaseDB = subTestCaseRepository.save(modifiedSubTestcase);
        }

        //Edit default vales of subTest and add modified subTest cases to Main test case
        Map<String, Map<String, DateTime>> subtestCaseTimeBetween = subTestcaseDB.getSubTestCaseTimeBetween();

        Map<String, DateTime> timeSchedule = new HashMap<>();
        timeSchedule.put("startTime", subTestCase.getStartTime());
        timeSchedule.put("endTime", subTestCase.getEndTime());
        subtestCaseTimeBetween.put(String.valueOf(subtestCaseTimeBetween.size()), timeSchedule);


        subTestcaseDB.setSubTestCaseTimeBetween(subtestCaseTimeBetween);
        subTestcaseDB.setMinVal(subTestCase.getMinVal());
        subTestcaseDB.setMaxVal(subTestCase.getMaxVal());
        subTestcaseDB.setStartTime(subTestCase.getStartTime());
        subTestcaseDB.setEndTime(subTestCase.getEndTime());
        subTestcaseDB.setDefault(Boolean.FALSE);
        SubTestCase subTestCaseDB = subTestCaseRepository.save(subTestcaseDB);
        testCaseDB.getSubTestCases().remove(indexOfExistingSubTestcase);
        testCaseDB.getSubTestCases().add(indexOfExistingSubTestcase, subTestCaseDB);
        testCaseDB.setDefault(Boolean.FALSE);
        return testCaseRepository.save(testCaseDB);

    }


    // TestCase finished completely
    public boolean DateCheck(DateTime startTime, DateTime endTime, DateTime currentDateTime) {
        return ((endTime.isBefore(currentDateTime) || endTime.isEqual(currentDateTime)) && startTime.isBefore(currentDateTime));
    }

    //Testing time is in between startTime and endTime
    public boolean TimeBetweenStartAndEnd(DateTime startTime, DateTime endTime, DateTime currentTime) {
        return (currentTime.isBefore(endTime) || currentTime.isEqual(endTime)) && (currentTime.isAfter(startTime) || currentTime.isEqual(startTime));
    }


    //Add test cases to Bulk of devices
    public String deviceConfiguration(Integer batchNumber, DeviceConfiguration configuration) {
        logger.debug("Add Testcases to Bulk of devices OR configure devices Request for batch {}", batchNumber);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        // Basic Validation of Batch Number , Devices , Testcases
        if (batchNumber == null || batchNumber == 0 || configuration.getDevices() == null || configuration.getTestConfigurations() == null) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }

        //Configure Batch number of provided devices
        for (String deviceId : configuration.getDevices()) {
            configureBatchNumber(deviceId, batchNumber);
        }

        List<TestCase> testConfigurations = configuration.getTestConfigurations();

        for (TestCase testCase : testConfigurations) {
            //validate & configure-main testcases
            TestCase defaultTestcase = testCaseRepository.findByMainTestTitleAndBatchNumber(testCase.getMainTestTitle(), 0);
            if (defaultTestcase == null) {
                throw new MagmaException(MagmaStatus.TEST_CASE_NOT_FOUND);
            }
            TestCase testCaseDB = testCaseRepository.findByMainTestTitleAndBatchNumber(testCase.getMainTestTitle(), batchNumber);
            if (testCaseDB == null) {
                testCaseDB = new TestCase();
            }


            Integer sYear = Integer.valueOf(configuration.getStartDate().substring(0, 4));
            Integer sMonth = Integer.valueOf(configuration.getStartDate().substring(5, 7));
            Integer sDay = Integer.valueOf(configuration.getStartDate().substring(8, 10));
            Integer sHour = Integer.valueOf(configuration.getStartDate().substring(11, 13));
            Integer sMin = Integer.valueOf(configuration.getStartDate().substring(14, 16));

            Integer eYear = Integer.valueOf(configuration.getEndDate().substring(0, 4));
            Integer eMonth = Integer.valueOf(configuration.getEndDate().substring(5, 7));
            Integer eDay = Integer.valueOf(configuration.getEndDate().substring(8, 10));
            Integer eHour = Integer.valueOf(configuration.getEndDate().substring(11, 13));
            Integer eMin = Integer.valueOf(configuration.getEndDate().substring(14, 16));


            testCaseDB.setBatchNumber(batchNumber);
            testCaseDB.setStartDate(new DateTime(sYear, sMonth, sDay, sHour, sMin).withZone(DateTimeZone.forID("Asia/Colombo")));
            testCaseDB.setEndDate(new DateTime(eYear, eMonth, eDay, eHour, eMin).withZone(DateTimeZone.forID("Asia/Colombo")));
            testCaseDB.setDefault(Boolean.FALSE);
            testCaseDB.setStatus(TestResult.OFFLINE);
            testCaseDB.setMainTestTitle(testCase.getMainTestTitle());
            if (testCaseDB.getDevices().isEmpty()) {
                testCaseDB.setDevices(configuration.getDevices());
            } else {
                List<String> devices = testCaseDB.getDevices();
                for (String device : configuration.getDevices()) {
                    if (!devices.contains(device)) {
                        devices.add(device);
                    }
                }
                testCaseDB.setDevices(devices);
            }
            List<SubTestCase> subTestCasesOfBatch = testCase.getSubTestCases();

            for (SubTestCase subTestCase : subTestCasesOfBatch) {
                boolean subTestAlreadyExists = true;
                SubTestCase defaultSubTestcase = subTestCaseRepository.findBySubTestTitleAndBatchNumber(subTestCase.getSubTestTitle(), 0);

                if (defaultSubTestcase == null) {
                    throw new MagmaException(MagmaStatus.TEST_CASE_NOT_FOUND);
                }

                if (configuration.getDevices() == null) {
                    throw new MagmaException(MagmaStatus.DEVICE_NOT_FOUND);
                }

                SubTestCase subTestCaseDB = subTestCaseRepository.findBySubTestTitleAndBatchNumberAndDevices(subTestCase.getSubTestTitle(), batchNumber, configuration.getDevices());
                if (subTestCaseDB == null) {
                    subTestCaseDB = new SubTestCase();
                    subTestAlreadyExists = false;
                }
                Map<String, Map<String, DateTime>> tempMap = subTestCaseDB.getSubTestCaseTimeBetween();
                Map<String, DateTime> timeMap = new HashMap<>();
                timeMap.put("startTime", new DateTime(sYear, sMonth, sDay, sHour, sMin).withZone(DateTimeZone.forID("Asia/Colombo")));
                timeMap.put("endTime", new DateTime(eYear, eMonth, eDay, eHour, eMin).withZone(DateTimeZone.forID("Asia/Colombo")));


                tempMap.put(String.valueOf(tempMap.size()), timeMap);
                subTestCaseDB.setSubTestTitle(subTestCase.getSubTestTitle());
                subTestCaseDB.setDefault(Boolean.FALSE);
                subTestCaseDB.setBatchNumber(batchNumber);
                subTestCaseDB.setStartTime(new DateTime(sYear, sMonth, sDay, sHour, sMin).withZone(DateTimeZone.forID("Asia/Colombo")));
                subTestCaseDB.setEndTime(new DateTime(eYear, eMonth, eDay, eHour, eMin).withZone(DateTimeZone.forID("Asia/Colombo")));
                subTestCaseDB.setSubTestCaseTimeBetween(tempMap);
                //validation Of Max & min
                if (subTestCase.getSubTestTitle().equals("No Of Data")) {
                    isIntegerValidate(subTestCase.getMaxVal());
                    isIntegerValidate(subTestCase.getMinVal());
                }
                isNumericValidate(subTestCase.getMaxVal());
                isNumericValidate(subTestCase.getMinVal());
                subTestCaseDB.setMaxVal(subTestCase.getMaxVal());
                subTestCaseDB.setMinVal(subTestCase.getMinVal());
                subTestCaseDB.setStatus(TestResult.OFFLINE);
                subTestCaseDB.setDevices(configuration.getDevices());
                //Add subTestcase to main
                if (subTestAlreadyExists) {
                    SubTestCase saved = subTestCaseRepository.save(subTestCaseDB);
                    List<SubTestCase> subTestCaseList = testCaseDB.getSubTestCases();
                    if (subTestCaseList != null) {
                        boolean subExists = false;
                        for (int i = 0; i < subTestCaseList.size(); i++) {
                            SubTestCase sub = subTestCaseList.get(i);
                            if (sub.getId().equals(subTestCaseDB.getId())) {
                                subExists = true;
                                subTestCaseList.set(i, subTestCaseDB);
                                break;
                            }
                        }

                        if (!subExists) {
                            subTestCaseList.add(subTestCaseDB);
                            testCaseDB.setSubTestCases(subTestCaseList);
                        }
                    } else {
                        subTestCaseList = new ArrayList<>();
                        subTestCaseList.add(saved);
                    }
                } else {
                    SubTestCase saved = subTestCaseRepository.save(subTestCaseDB);
                    testCaseDB.addSubTestCases(saved);
                }
            }
            testCaseRepository.save(testCaseDB);
        }
        return "Configured";
    }

    //Remove sensors of a batch [sensors will be removed from DB]
    public String clearTestSensorsOfBatch(Integer batchNumber) {
        List<Device> devicesOfBatch = deviceRepository.findByBatchNumber(batchNumber);
        List<String> deviceIdsOfBatch = new ArrayList<>();
        devicesOfBatch.forEach(device -> deviceIdsOfBatch.add(device.getId()));
        sensorRepository.deleteByDeviceIdIn(deviceIdsOfBatch);
        return "TestSensors Removed";
    }

    public List<String> getAllTestConditions() {
        return new ArrayList<String>(Arrays.asList("Battery can be any level", "The devices can be left outside for this testing", "The sensors must be connected while testing"));
    }

    //Check a String is Integer or Not
    public boolean isIntegerCheck(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    //Check a String is Number or Not
    public boolean isNumericCheck(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    //Throw error if a string is not  a integer
    public void isIntegerValidate(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
    }

    //Throw error if a string is not a number
    public void isNumericValidate(String str) {
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
    }

    public HashMap<String, Integer> getProtocolSummary() {

        List<Device> MQTTdevice = deviceRepository.findByProtocol("MQTT");
        List<Device> HTTPdevice = deviceRepository.findByProtocol("HTTP");
        List<Device> HTTPSdevice = deviceRepository.findByProtocol("HTTPS");
        List<Device> TCPdevice = deviceRepository.findByProtocol("TCP");

        HashMap<String, Integer> prtocolCount = new HashMap<>();

        prtocolCount.put("MQTT", MQTTdevice.size());
        prtocolCount.put("HTTP", HTTPdevice.size());
        prtocolCount.put("HTTPS", HTTPSdevice.size());
        prtocolCount.put("TCP", TCPdevice.size());

        return prtocolCount;

    }


    public Map<String, ArrayList<String>> getDefaultTestMap() {
        List<TestCase> testCases = testCaseRepository.findByBatchNumber(0);

        if (testCases == null) {
            throw new MagmaException(MagmaStatus.TEST_CASE_NOT_FOUND);
        }

        Map<String, ArrayList<String>> result = new HashMap<>();

        for (TestCase testCase : testCases) {
            ArrayList<String> subTestList = new ArrayList<>();
            for (SubTestCase subTestCase : testCase.getSubTestCases()) {
                subTestList.add(subTestCase.getSubTestTitle());
            }
            result.put(testCase.getMainTestTitle(), subTestList);
        }
        return result;
    }
}
