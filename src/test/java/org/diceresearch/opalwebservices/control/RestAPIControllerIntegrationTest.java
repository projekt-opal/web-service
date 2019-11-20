package org.diceresearch.opalwebservices.control;

import org.diceresearch.opalwebservices.OpalWebservicesApplication;
import org.diceresearch.opalwebservices.model.dto.DataSetLongViewDTO;
import org.diceresearch.opalwebservices.triplestore.EmbeddedTripleStore;
import org.diceresearch.opalwebservices.triplestore.QueryExecutionFactoryDataSetProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles(value = {"test-triplestore"})
@SpringBootTest(classes = {OpalWebservicesApplication.class, EmbeddedTripleStore.class, QueryExecutionFactoryDataSetProvider.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RestAPIControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    public void getNumberOfDataSetsIntegrationTest() {
        Long num = this.restTemplate.postForObject("http://localhost:" + port + "/dataSets/getNumberOfDataSets",
                null, Long.class);
        assertEquals(2L, (long) num);
    }

    @Test
    public void getSubListOFDataSetsIntegrationTest() {
        DataSetLongViewDTO[] listDataSets =
                this.restTemplate.postForObject("http://localhost:" + port + "/dataSets/getSubList",
                        null, DataSetLongViewDTO[].class);

        assertEquals(2, listDataSets.length);

        List<String> uris = Arrays.stream(listDataSets).map(DataSetLongViewDTO::getUri).collect(Collectors.toList());

        assertThat(uris, containsInAnyOrder(
                "http://projekt-opal.de/dataset/https___ckan_govdata_de_00719fae_d3cb_5c76_853b_54a5e137924e",
                "http://projekt-opal.de/dataset/https___ckan_govdata_de_00827f99_3b27_525b_9ded_7436579b9dec")
        );
    }

}