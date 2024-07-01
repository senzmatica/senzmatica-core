package com.magma.core.service;

import com.magma.core.data.entity.Device;
import com.magma.core.data.entity.SubTestCase;
import com.magma.core.data.entity.TestCard;
import com.magma.core.data.entity.TestCase;
import com.magma.core.data.repository.*;
import com.magma.core.data.support.TestResult;
import com.magma.core.data.support.TestSummary;
import com.magma.core.util.MagmaException;
import com.magma.core.util.MagmaStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeviceTestAutomation {

    @Autowired
    TestCaseRepository testCaseRepository;

    @Autowired
    SubTestCaseRepository subTestCaseRepository;

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

    public TestCase createNewTest(TestCase testCase) {
        if (testCase.getBatchNumber() == null
                || testCase.getBatchNumber() == 0
                || testCase.getMainTestTitle() == null
                || testCase.getSubTestCasesIdList().size() == 0
                || testCase.getStartDate() == null
                || testCase.getEndDate() == null
                || testCase.getDevices() == null
                || testCase.getDevices().size() == 0) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
        TestCase existTestCase = testCaseRepository.findByMainTestTitleAndBatchNumber(testCase.getMainTestTitle(), 0);
        if (existTestCase == null) {
            throw new MagmaException(MagmaStatus.TEST_CASE_NOT_FOUND);
        }
        List<SubTestCase> subTestCaseList = new ArrayList<>();
        testCase.getSubTestCasesIdList().forEach((st) -> {
            SubTestCase subTestCase = subTestCaseRepository.findOne(st);
            if (subTestCase != null) {
                subTestCaseList.add(subTestCase);
            }
        });
        testCase.setDefault(false);
        testCase.setSubTestCases(subTestCaseList);
        TestCase savedTestCase = testCaseRepository.save(testCase);

        //Update the test summery with new test
        List<TestSummary> testSummary = testSummaryRepository.findAll();
        if (testSummary.size() == 0) {
            testSummary.add(new TestSummary());
        }


        List<Device> devicesOfBatch = deviceRepository.findByIdIn(testCase.getDevices());
        List<TestCase> requestedTestCases = new ArrayList<>();
        requestedTestCases.add(savedTestCase);

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
                        }
                    });
                    mainMap.put(mainTest.getId(), subTestMap);
                }
            });


            if (testSummary.get(0).getDeviceTestResult().get(0).get(device.getId()) == null) {
                deviceMap.put(device.getId(), mainMap);
                testSummary.get(0).getDeviceTestResult().get(0).put(device.getId(), mainMap);
            } else {
                Map<String, Map<String, List<TestCard>>> existingMap = testSummary.get(0).getDeviceTestResult().get(0).get(device.getId());
                if (existingMap == null) {
                    existingMap = new HashMap<>();
                }
                existingMap.putAll(mainMap);
                testSummary.get(0).getDeviceTestResult().get(0).put(device.getId(), existingMap);
            }
            testSummaryRepository.save(testSummary.get(0));
        });
        return savedTestCase;
    }

    public TestCase editSubTestCaseParameters(String testCaseId, SubTestCase updatedSubTestCases) {
        TestCase requestedTestCase = testCaseRepository.findOne(testCaseId);
        if (requestedTestCase == null) {
            throw new MagmaException(MagmaStatus.TEST_CASE_NOT_FOUND);
        }
        if (updatedSubTestCases.getSubTestTitle().equals("No Of Data")) {
            isIntegerValidate(updatedSubTestCases.getMaxVal());
            isIntegerValidate(updatedSubTestCases.getMinVal());
        } else {
            isNumericValidate(updatedSubTestCases.getMaxVal());
            isNumericValidate(updatedSubTestCases.getMinVal());
        }

        requestedTestCase.getSubTestCases().forEach((st) -> {
            if (st.getId().equals(updatedSubTestCases.getId())) {
                st.setMinVal(updatedSubTestCases.getMinVal());
                st.setMaxVal(updatedSubTestCases.getMaxVal());
            }
        });
        return testCaseRepository.save(requestedTestCase);
    }

    public List<Map<String, Map<String, Map<String, List<TestCard>>>>> getSummaryTable(Integer batchNo, String testCaseTitle) {
        List<TestCase> testOfBatchAndTitle = testCaseRepository.findByBatchNumberAndMainTestTitle(batchNo, testCaseTitle);
        List<String> testIds = new ArrayList<>();
        testOfBatchAndTitle.forEach((t) -> {
            testIds.add(t.getId());
        });

        List<Map<String, Map<String, Map<String, List<TestCard>>>>> allResults = testSummaryRepository.findAll().get(0).getDeviceTestResult();

        List<Map<String, Map<String, Map<String, List<TestCard>>>>> filteredResults = new ArrayList<>();

        for (Map<String, Map<String, Map<String, List<TestCard>>>> deviceResult : allResults) {
            Map<String, Map<String, Map<String, List<TestCard>>>> filteredDeviceResult = new HashMap<>();
            for (Map.Entry<String, Map<String, Map<String, List<TestCard>>>> deviceEntry : deviceResult.entrySet()) {
                Map<String, Map<String, List<TestCard>>> filteredTestIdResult = new HashMap<>();
                for (Map.Entry<String, Map<String, List<TestCard>>> testIdEntry : deviceEntry.getValue().entrySet()) {
                    if (testIds.contains(testIdEntry.getKey())) {
                        filteredTestIdResult.put(testIdEntry.getKey(), testIdEntry.getValue());
                    }
                }
                if (!filteredTestIdResult.isEmpty()) {
                    filteredDeviceResult.put(deviceEntry.getKey(), filteredTestIdResult);
                }
            }
            if (!filteredDeviceResult.isEmpty()) {
                filteredResults.add(filteredDeviceResult);
            }
        }

        return filteredResults;
    }

    public void isIntegerValidate(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
    }

    public void isNumericValidate(String str) {
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw new MagmaException(MagmaStatus.INVALID_INPUT);
        }
    }
}
