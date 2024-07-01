package com.magma.core.data.repository;

import com.magma.core.data.entity.TestCase;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends MongoRepository<TestCase, String> {

    List<TestCase> findByBatchNumber(Integer batchNumber);

    TestCase findByMainTestTitle(String title);

    TestCase findByMainTestTitleAndBatchNumber(String name, Integer batchNumber);

    List<TestCase> findByBatchNumberAndMainTestTitle(Integer batchNumber, String mainTestTitle);

    List<TestCase> findByIsDefault(Boolean isDefault);

    List<TestCase> findByBatchNumberAndMainTestTitleNotIn(Integer name, List<String> titles);
}
