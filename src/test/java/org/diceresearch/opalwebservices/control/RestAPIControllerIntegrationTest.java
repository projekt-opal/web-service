package org.diceresearch.opalwebservices.control;

import org.diceresearch.opalwebservices.OpalWebservicesApplication;
import org.diceresearch.opalwebservices.triplestore.EmbeddedTripleStore;
import org.diceresearch.opalwebservices.triplestore.QueryExecutionFactoryDataSetProvider;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles(value = {"test-triplestore"})
@SpringBootTest(classes = {OpalWebservicesApplication.class, EmbeddedTripleStore.class, QueryExecutionFactoryDataSetProvider.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RestAPIControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void initQueryExecutionFactoryHelper() {
        
    }

    @Test
    public void getNumberOfDataSetsIntegrationTest()
    {
        Long num = this.restTemplate.postForObject("http://localhost:" + port + "/dataSets/getNumberOfDataSets",
                null, Long.class);
        assertEquals(2L, (long) num);
    }


}