package org.dice_research.opal.webservice.services;

import org.dice_research.opal.webservice.config.ThemeConfiguration;
import org.dice_research.opal.webservice.model.entity.dto.SearchDTO;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.Cardinality;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ElasticSearchProviderTest {

    @InjectMocks
    private ElasticSearchProvider elasticSearchProvider;

    @Mock
    private RestHighLevelClient restHighLevelClient;

    @Mock
    private ThemeConfiguration themeConfiguration;

    @BeforeEach
    public void setUp() throws NoSuchFieldException {
        FieldSetter.setField(elasticSearchProvider,
                elasticSearchProvider.getClass().getDeclaredField("es_index"), "opal_test");
    }

    @Test
    void getNumberOfDataSets() throws IOException, JSONException {

        String query = "{" +
                "  \"size\": 0, " +
                "  \"query\": {" +
                "    \"bool\": {" +
                "      \"must\": [" +
                "        {\"match_all\": {}}" +
                "      ]" +
                "    }" +
                "  }, " +
                "  \"aggregations\": {" +
                "    \"aggs_number_of_datasets\": {" +
                "      \"cardinality\": {" +
                "        \"field\": \"uri\"," +
                "        \"precision_threshold\": 100" +
                "      }" +
                "    }" +
                "  } " +
                "}";

        SearchResponse searchResponse = mock(SearchResponse.class);
        when(restHighLevelClient.search(any(), any())).thenReturn(searchResponse);
        Aggregations aggregations = mock(Aggregations.class);
        when(searchResponse.getAggregations()).thenReturn(aggregations);
        Cardinality cardinality = mock(Cardinality.class);
        when(aggregations.get(anyString())).thenReturn(cardinality);
        long expectedNumberOfDataSets = 10L;
        when(cardinality.getValue()).thenReturn(expectedNumberOfDataSets);

        long numberOfDataSets = elasticSearchProvider.getNumberOfDataSets(new SearchDTO());

        ArgumentCaptor<SearchRequest> searchRequestCapture = ArgumentCaptor.forClass(SearchRequest.class);
        verify(restHighLevelClient).search(searchRequestCapture.capture(), eq(RequestOptions.DEFAULT));
        JSONAssert.assertEquals(query, searchRequestCapture.getValue().source().toString(), false);

        Assertions.assertEquals(expectedNumberOfDataSets, numberOfDataSets, "number of return dataset is not correct");

    }

    @Test
    void getSublistOfDataSets() {
    }

    @Test
    void getFilters() {
    }

    @Test
    void getTopFiltersThatContain() {
    }

    @Test
    void getDataSet() {
    }

    @Test
    void getNumberOfRelatedDataSets() {
    }

    @Test
    void getSubListOfRelatedDataSets() {
    }
}