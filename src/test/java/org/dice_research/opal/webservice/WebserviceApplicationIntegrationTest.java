package org.dice_research.opal.webservice;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.dice_research.opal.webservice.model.entity.dto.SearchDTO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Profile("integration-test")
@ExtendWith(SpringExtension.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "ES_INDEX:opal_test"
        })
public class WebserviceApplicationIntegrationTest {

    // will be shared between test methods
    @Container
    private static final GenericContainer ELASTICSEARCH_CONTAINER =
            new FixedHostPortGenericContainer("elasticsearch:7.3.2")
                    .withFixedExposedPort(9200, 9200)
                    .waitingFor(new HostPortWaitStrategy());

    static {
        ELASTICSEARCH_CONTAINER.addEnv("discovery.type", "single-node");
        ELASTICSEARCH_CONTAINER.start();
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    public static void populateElasticSearch() throws IOException {
        waitUntilIsReady();
        createIndex();
        insertData();
    }

    @AfterAll
    //It is optional, just for speeding up killing ES container
    public static void stopElasticSearch() {
        ELASTICSEARCH_CONTAINER.stop();
    }

    private static void waitUntilIsReady() {
        HttpClient httpclient = HttpClients.createDefault();
        HttpHead httpHead = new HttpHead("http://localhost:9200/");
        int cnt = 0;
        while (cnt++ < 100) { //maximum bound to prevent infinite loop
            try {
                Thread.sleep(1000); //reduce wait time in each iteration
                httpclient.execute(httpHead);
                break;
            } catch (Exception ignore) {
            }
        }
    }

    private static void createIndex() throws IOException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut("http://localhost:9200/opal_test");

        httpPut.setHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
        httpPut.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        File file = new ClassPathResource("mappings.json").getFile();
        byte[] bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        httpPut.setEntity(new ByteArrayEntity(bytes));

        httpclient.execute(httpPut);
    }

    private static void insertData() throws IOException {


        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost("http://localhost:9200/opal_test/_bulk");

        httppost.setHeader("Content-Type", "application/x-ndjson");

        File file = new ClassPathResource("data.ndjson").getFile();
        byte[] bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
        httppost.setEntity(new ByteArrayEntity(bytes));

        httpclient.execute(httppost);
    }


    @Test
    public void testGetNumberOFDataSets() {
        SearchDTO searchDTO = new SearchDTO();
        Long numberOfDataSets = restTemplate.postForObject("/dataSets/getNumberOfDataSets", searchDTO, Long.class);
        Assertions.assertEquals(16L, numberOfDataSets, "number of return dataset is not correct");
    }

}