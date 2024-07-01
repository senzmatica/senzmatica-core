package com.magma.core.data.repository;

import com.magma.core.data.entity.SubTestCase;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubTestCaseRepository extends MongoRepository<SubTestCase, String> {
    List<SubTestCase> findByBatchNumber(Integer batchNumber);

    SubTestCase findBySubTestTitle(String title);

    SubTestCase findBySubTestTitleAndBatchNumber(String name, Integer batchNumber);

    List<SubTestCase> findByBatchNumberAndSubTestTitleNotIn(Integer branch, List<String> titles);

    SubTestCase findBySubTestTitleAndBatchNumberAndDevices(String name, Integer batchNumber, List<String> devices);
}
