package com.magma.core.configuration;

import com.magma.core.data.entity.Device;
import com.magma.core.data.entity.SubTestCase;
import com.magma.core.data.entity.TestCase;
import com.magma.core.data.repository.DeviceRepository;
import com.magma.core.data.repository.SubTestCaseRepository;
import com.magma.core.data.repository.TestCaseRepository;
import com.magma.core.data.support.TestResult;
import com.magma.util.MagmaTime;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
public class DeviceTestConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(DeviceTestConfiguration.class);

    @Autowired
    DeviceRepository deviceRepository;

    @Bean
    public CommandLineRunner initializeDefaultTestCases(TestCaseRepository testCaseRepository, SubTestCaseRepository subTestCaseRepository) {
        logger.debug("DMS - Create Testcases if not exists !!!");

        List<String> deviceList = new ArrayList<>();
        Map<String, Map<String, DateTime>> timeList = new HashMap<>();
        Map<String, DateTime> subTestTime = new HashMap<>();
        DateTime startTime = MagmaTime.now();
        DateTime endTime = startTime.plusMinutes(5);
        subTestTime.put("startTime", startTime);
        subTestTime.put("endTime", endTime);
        timeList.put("0", subTestTime);
        Map<String, List<SubTestCase>> subTestCaseMap = new HashMap<>();
        List<Device> devices = deviceRepository.findAll();
        ArrayList<String> devicesList = new ArrayList<>();
        for (Device device : devices) {
            devicesList.add(device.getId());
        }
        TestCase continuousTransmissionTest = new TestCase(0, TestResult.OFFLINE, "Continuous Transmission Test", Boolean.TRUE, devicesList);
        SubTestCase dataCount = new SubTestCase(0, TestResult.OFFLINE, "No Of Data", Boolean.TRUE, "1995", "2016", "number", deviceList, startTime, endTime, timeList);
        SubTestCase externalTemperature = new SubTestCase(0, TestResult.OFFLINE, "External Temperature", Boolean.TRUE, "-20", "60", "number", deviceList, startTime, endTime, timeList);
        SubTestCase humidity = new SubTestCase(0, TestResult.OFFLINE, "Humidity", Boolean.TRUE, "0", "100", "percentage", deviceList, startTime, endTime, timeList);
        SubTestCase electricalConductivity = new SubTestCase(0, TestResult.OFFLINE, "Electrical Conductivity", Boolean.TRUE, "22", "40", "number", deviceList, startTime, endTime, timeList);
        SubTestCase battery = new SubTestCase(0, TestResult.OFFLINE, "Battery", Boolean.TRUE, "240", "300", "number", deviceList, startTime, endTime, timeList);
        SubTestCase internalTemperature = new SubTestCase(0, TestResult.OFFLINE, "Internal Temperature", Boolean.TRUE, "26", "47", "number", deviceList, startTime, endTime, timeList);
        List<SubTestCase> continuousTransmissionSubTests = new ArrayList<SubTestCase>(Arrays.asList(dataCount, externalTemperature, humidity, electricalConductivity, battery, internalTemperature));
        subTestCaseMap.put("Continuous Transmission Test", continuousTransmissionSubTests);

        TestCase coverageTest = new TestCase(0, TestResult.OFFLINE, "Coverage Test", Boolean.TRUE, devicesList);
        SubTestCase dataCount1 = new SubTestCase(0, TestResult.OFFLINE, "No Of Data", Boolean.TRUE, "1995", "2016", "number", deviceList, startTime, endTime, timeList);
        SubTestCase highCoverage = new SubTestCase(0, TestResult.OFFLINE, "High Coverage", Boolean.TRUE, "99", "100", "number", deviceList, startTime, endTime, timeList);
        SubTestCase mediumCoverage = new SubTestCase(0, TestResult.OFFLINE, "Medium Coverage", Boolean.TRUE, "90", "100", "number", deviceList, startTime, endTime, timeList);
        SubTestCase lowCoverage = new SubTestCase(0, TestResult.OFFLINE, "Low Coverage", Boolean.TRUE, "70", "100", "number", deviceList, startTime, endTime, timeList);
        SubTestCase veryLowCoverage = new SubTestCase(0, TestResult.OFFLINE, "Very Low Coverage", Boolean.TRUE, "0", "100", "number", deviceList, startTime, endTime, timeList);
        SubTestCase noCoverage = new SubTestCase(0, TestResult.OFFLINE, "No Coverage", Boolean.TRUE, "0", "100", "number", deviceList, startTime, endTime, timeList);
        List<SubTestCase> coverageTestSubTests = new ArrayList<SubTestCase>(Arrays.asList(dataCount1, highCoverage, mediumCoverage, lowCoverage, veryLowCoverage, noCoverage));
        subTestCaseMap.put("Coverage Test", coverageTestSubTests);

        TestCase flashSavingTest = new TestCase(0, TestResult.OFFLINE, "Flash Saving Test", Boolean.TRUE, devicesList);
        SubTestCase dataCount2 = new SubTestCase(0, TestResult.OFFLINE, "No Of Data", Boolean.TRUE, "1995", "2016", "number", deviceList, startTime, endTime, timeList);
        SubTestCase flashSavingTestOfDay = new SubTestCase(0, TestResult.OFFLINE, "Flash Saving Test Day", Boolean.TRUE, "144", dataCount.getMaxVal(), "number", deviceList, startTime, endTime, timeList);
        SubTestCase flashSavingTestOfWeek = new SubTestCase(0, TestResult.OFFLINE, "Flash Saving Test Week", Boolean.TRUE, "1008", dataCount.getMaxVal(), "number", deviceList, startTime, endTime, timeList);
        List<SubTestCase> flashSavingSubTests = new ArrayList<SubTestCase>(Arrays.asList(dataCount2, flashSavingTestOfDay, flashSavingTestOfWeek));
        subTestCaseMap.put("Flash Saving Test", flashSavingSubTests);
//
//        TestCase gsmConnectivityTest = new TestCase(0, TestResult.OFFLINE, "GSM Connectivity Test", Boolean.TRUE, devicesList);
//        SubTestCase dataCount3 = new SubTestCase(0, TestResult.OFFLINE, "No Of Data", Boolean.TRUE, "1995", "2016", "number", deviceList, startTime, endTime, timeList);
//        SubTestCase highCoverageLowVoltage = new SubTestCase(0, TestResult.OFFLINE, "High Coverage & Low Voltage", Boolean.TRUE, "1995", "2016", "number", deviceList, startTime, endTime, timeList);
//        SubTestCase highCoverageHighVoltage = new SubTestCase(0, TestResult.OFFLINE, "High Coverage & High Voltage", Boolean.TRUE, "1500", "2016", "number", deviceList, startTime, endTime, timeList);
//        SubTestCase mediumCoverageHighVoltage = new SubTestCase(0, TestResult.OFFLINE, "Medium Coverage & High Voltage", Boolean.TRUE, "1000", "2016", "number", deviceList, startTime, endTime, timeList);
//        SubTestCase mediumCoverageLowVoltage = new SubTestCase(0, TestResult.OFFLINE, "Medium Coverage & Low Voltage", Boolean.TRUE, "250", "2016", "number", deviceList, startTime, endTime, timeList);
//        SubTestCase lowCoverageHighVoltage = new SubTestCase(0, TestResult.OFFLINE, "Low Coverage & High Voltage", Boolean.TRUE, "1000", "2016", "number", deviceList, startTime, endTime, timeList);
//        SubTestCase lowCoverageLowVoltage = new SubTestCase(0, TestResult.OFFLINE, "Low Coverage & Low Voltage", Boolean.TRUE, "1000", "2016", "number", deviceList, startTime, endTime, timeList);
//        List<SubTestCase> gsmConnectivitySubTests = new ArrayList<SubTestCase>(Arrays.asList(dataCount3,highCoverageLowVoltage, highCoverageHighVoltage, mediumCoverageHighVoltage, mediumCoverageLowVoltage, lowCoverageHighVoltage, lowCoverageLowVoltage));
//        subTestCaseMap.put("GSM Connectivity Test", gsmConnectivitySubTests);

        List<TestCase> testCases = new ArrayList<TestCase>(Arrays.asList(continuousTransmissionTest, coverageTest, flashSavingTest));

        return args -> {
            for (TestCase testCase : testCases) {
                for (SubTestCase subTestCase : subTestCaseMap.get(testCase.getMainTestTitle())) {
                    SubTestCase subTestCaseDB = subTestCaseRepository.findBySubTestTitleAndBatchNumber(subTestCase.getSubTestTitle(), 0);
                    SubTestCase storedSubTest;
                    if (subTestCaseDB == null) {
                        storedSubTest = subTestCaseRepository.save(subTestCase);
                    } else {
                        subTestCase.setId(subTestCaseDB.getId());
                        BeanUtils.copyProperties(subTestCase, subTestCaseDB);
                        storedSubTest = subTestCaseRepository.save(subTestCaseDB);
                    }
                    testCase.addSubTestCases(storedSubTest);
                }
                TestCase testCaseDB = testCaseRepository.findByMainTestTitleAndBatchNumber(testCase.getMainTestTitle(), 0);
                if (testCaseDB == null) {
                    testCaseRepository.save(testCase);
                } else {
                    testCase.setId(testCaseDB.getId());
                    BeanUtils.copyProperties(testCase, testCaseDB);
                    testCaseRepository.save(testCaseDB);
                }
            }
        };
    }
}
