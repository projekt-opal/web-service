package org.dice_research.opal.webservice.services;

import org.dice_research.opal.webservice.config.ThemeConfiguration;
import org.dice_research.opal.webservice.model.entity.dto.*;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
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
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
        when(themeConfiguration.getRevMap()).thenReturn(
                ImmutableMap.of(
                        "Economy and finance", "http://publications.europa.eu/resource/authority/data-theme/ECON",
                        "Government and public sector", "http://publications.europa.eu/resource/authority/data-theme/GOVE",
                        "Agriculture, fisheries, forestry and food", "http://publications.europa.eu/resource/authority/data-theme/AGRI"
                )
        );
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
    void getSublistOfDataSets() throws IOException, JSONException {

        String query = "{" +
                "  \"from\": 2," +
                "  \"size\": 10," +
                "  \"query\": {" +
                "    \"bool\": {" +
                "      \"must\": [" +
                "        {" +
                "          \"multi_match\": {" +
                "            \"query\": \"paderborn\"," +
                "            \"fields\": [\"title^1.0\", \"title_de^1.0\", \"description^1.0\", \"description_de^1.0\", \"keywords^1.0\", \"keywords_de^1.0\"]" +
                "          }" +
                "        }," +
                "        {" +
                "          \"bool\": {" +
                "            \"should\": [" +
                "              {" +
                "                \"match\": {" +
                "                  \"themes\":{\"query\":\"http://publications.europa.eu/resource/authority/data-theme/GOVE\"}" +
                "                }" +
                "              }," +
                "              {" +
                "                \"match\": {" +
                "                  \"themes\":{\"query\":\"http://publications.europa.eu/resource/authority/data-theme/ECON\"}" +
                "                }" +
                "              }" +
                "            ]," +
                "            \"minimum_should_match\": \"1\"" +
                "          }" +
                "        }" +
                "      ], " +
                "      \"should\": [" +
                "        {" +
                "          \"match\": {" +
                "            \"title\": {\"query\":\"paderborn\"}" +
                "          }" +
                "        }," +
                "        {" +
                "          \"match\": {" +
                "            \"title_de\": {\"query\":\"paderborn\"}" +
                "          }" +
                "        }" +
                "      ]" +
                "    }" +
                "  }" +
                "}";

        SearchResponse searchResponse = mock(SearchResponse.class);
        when(restHighLevelClient.search(any(), any())).thenReturn(searchResponse);
        SearchHits searchHits = mock(SearchHits.class);
        when(searchResponse.getHits()).thenReturn(searchHits);
        SearchHit[] hits = new SearchHit[1];
        SearchHit searchHit = mock(SearchHit.class);
        hits[0] = searchHit;
        when(searchHits.getHits()).thenReturn(hits);
        when(searchHit.getSourceAsString()).thenReturn("{\"uri\" : \"http://projekt-opal.de/dataset/8c010ac8c5bc53569cbf659bc4265241\"}");


        SearchDTO searchDTO = new SearchDTO();
        searchDTO.setSearchIn(new String[]{"title", "description", "keywords"});
        searchDTO.setSearchKey("paderborn");
        FilterDTO[] filterDTOList = new FilterDTO[]{
                FilterDTO.builder().filterGroupTitle("Theme").searchField("themes").hasExternalLink(true).hasStaticValues(true)
                        .values(Arrays.asList(
                                new ValueDTO("Economy and finance", new CounterDTO(787, 64), true, null, null),
                                new ValueDTO("Agriculture, fisheries, forestry and food", new CounterDTO(1322, 45), false, null, null),
                                new ValueDTO("Government and public sector", new CounterDTO(1, 1), true, null, null)
                        )).build(),
                FilterDTO.builder().filterGroupTitle("License").searchField("distributions.license.uri.keyword").hasExternalLink(true).hasStaticValues(false)
                        .values(Arrays.asList(
                                new ValueDTO("http://reference.data.gov.uk/id/open-government-licence", new CounterDTO(530, 68), false, null, null),
                                new ValueDTO("http://dcat-ap.de/def/licenses/dl-by-de/2.0", new CounterDTO(500, 100), false, null, null)
                        )).build()
        };
        searchDTO.setFilters(filterDTOList);
        OrderByDTO orderByDTO = new OrderByDTO();
        orderByDTO.setSelectedOrderValue("title");
        searchDTO.setOrderBy(orderByDTO);
        List<DataSetDTO> sublistOfDataSets = elasticSearchProvider.getSublistOfDataSets(searchDTO, 2, 10);

        ArgumentCaptor<SearchRequest> searchRequestCapture = ArgumentCaptor.forClass(SearchRequest.class);
        verify(restHighLevelClient).search(searchRequestCapture.capture(), eq(RequestOptions.DEFAULT));

        System.out.println(searchRequestCapture.getValue().source().toString());

        JSONAssert.assertEquals(query, searchRequestCapture.getValue().source().toString(), false);

        Assertions.assertEquals(1, sublistOfDataSets.size(), "number of return dataset is not correct");


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