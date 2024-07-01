package com.magma.core.data.repository;

import com.magma.core.data.support.TestSummary;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TestSummaryRepository extends MongoRepository<TestSummary, String> {

    TestSummary findByBatchNumber(Integer batchNumber);

}
