package org.dice_research.opal.webservice.utility.elasticsearch;

import org.apache.http.HttpHost;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.lucene.search.join.ScoreMode;
import org.dice_research.opal.webservice.model.dto.*;
import org.dice_research.opal.webservice.model.mapper.JsonObjecttoDataSetMapper;
import org.dice_research.opal.webservice.utility.DataProvider;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.DisMaxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;

@Profile("elasticsearch")
@Component
public class ElasticSearchProvider implements DataProvider {

    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchProvider.class);
    private final JsonObjecttoDataSetMapper jsonObjecttoDataSetMapper;
    private RestHighLevelClient restClient;

    @PostConstruct
    public void init() {
        restClient = new RestHighLevelClient(RestClient.builder
                (new HttpHost("opaldata.cs.upb.de", 9200, "http")));
    }

    @PreDestroy
    public void destroy() {

        try {
            restClient.close();
        } catch (IOException e) {
            logger.error("Error while closing Elasticsearch connection: " + e);
        }
    }

    @Autowired
    public ElasticSearchProvider(JsonObjecttoDataSetMapper jsonObjecttoDataSetMapper) {
        this.jsonObjecttoDataSetMapper = jsonObjecttoDataSetMapper;
    }

    @Override
    public long getNumberOfDataSets(String searchKey, String[] searchIn, OrderByDTO orderBy, FilterDTO[] filters) {
        Long num = 0L;
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            if (searchKey.isEmpty() && filters.length == 0) {
                searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            } else {
                BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
                if (!searchKey.isEmpty()) {
                    DisMaxQueryBuilder searchConstraintQuery = getSearchConstraint(searchKey, searchIn);
                    boolQueryBuilder.must(searchConstraintQuery);
                }
                if (filters.length > 0) {
                    getFilterConstraints(filters, boolQueryBuilder);
                }
                searchSourceBuilder.query(boolQueryBuilder);
            }

            CountRequest countRequest = new CountRequest("opal");
            countRequest.source(searchSourceBuilder);

            CountResponse countResponse = restClient.count(countRequest, RequestOptions.DEFAULT);
            num = countResponse.getCount();
        } catch (IOException e) {
            logger.error("An error occurred in getting the results", e);
        }

        return num;
    }

    private BoolQueryBuilder getFilterConstraints(FilterDTO[] filters, BoolQueryBuilder searchConstraintQuery) {

        for (int k = 0; k < filters.length; k++) {
            DisMaxQueryBuilder disMaxQueryBuilder = new DisMaxQueryBuilder();
            Resource filterField = ResourceFactory.createResource(filters[k].getUri());
            String filterFiledName = filterField.getLocalName();
            if (filterField.getLocalName().equals("theme")) {
                filterFiledName = "prefLabel";
            } else if (filterField.getLocalName().equals("publisher")) {
                filterFiledName = "name";
            }
            HashMap<String, String> fieldDetails = returnFieldDetails(filterFiledName);
            List<FilterValueDTO> filterValues = filters[k].getValues();
            for (FilterValueDTO filterValue : filterValues) {
                getDisMaxQuery(fieldDetails, disMaxQueryBuilder, filterFiledName, filterValue.getUri());
            }
            searchConstraintQuery.must(disMaxQueryBuilder);
        }

        return searchConstraintQuery;
    }

    private DisMaxQueryBuilder getSearchConstraint(String searchKey, String[] searchIn) {
        DisMaxQueryBuilder disMaxQueryBuilder = new DisMaxQueryBuilder();
        for (int i = 0; i < searchIn.length; i++) {
            String fieldToSearch = searchIn[i];
            HashMap<String, String> fieldDetails = returnFieldDetails(fieldToSearch);
            getDisMaxQuery(fieldDetails, disMaxQueryBuilder, fieldToSearch, searchKey);
        }
        return disMaxQueryBuilder;
    }

    private DisMaxQueryBuilder getDisMaxQuery(HashMap<String, String> fieldDetails, DisMaxQueryBuilder disMaxQueryBuilder, String fieldToSearch, String searchKey) {

        if (Boolean.valueOf(fieldDetails.get("nested"))) {
            if (fieldDetails.get("type").equals("keyword")) {
                disMaxQueryBuilder.add(QueryBuilders.nestedQuery(fieldDetails.get("parent"), QueryBuilders.termQuery(fieldDetails.get("parent") + "." +
                        fieldToSearch + ".raw", searchKey), ScoreMode.None));
            } else {
                disMaxQueryBuilder.add(QueryBuilders.nestedQuery(fieldDetails.get("parent"), QueryBuilders.matchQuery(fieldDetails.get("parent") + "." +
                        fieldToSearch + ".raw", searchKey), ScoreMode.None));
            }
        } else {
            if (fieldDetails.get("type").equals("keyword")) {
                disMaxQueryBuilder.add(QueryBuilders.termQuery(fieldToSearch + ".raw", searchKey));
            } else {
                disMaxQueryBuilder.add(QueryBuilders.matchQuery(fieldToSearch, searchKey));
            }
        }
        return disMaxQueryBuilder;
    }

    @Override
    public List<DataSetLongViewDTO> getSubListOfDataSets(String searchKey, Long low, Long limit, String[] searchIn, OrderByDTO orderBy, FilterDTO[] filters) {
        List<DataSetLongViewDTO> ret = new ArrayList<>();

        try {

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            if (searchKey.isEmpty() && filters.length == 0) {
                searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            } else {
                BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
                if (!searchKey.isEmpty()) {
                    DisMaxQueryBuilder searchConstraintQuery = getSearchConstraint(searchKey, searchIn);
                    boolQueryBuilder.must(searchConstraintQuery);
                }
                if (filters.length > 0) {
                    getFilterConstraints(filters, boolQueryBuilder);
                }
                searchSourceBuilder.query(boolQueryBuilder);
            }
            searchSourceBuilder.from(Math.toIntExact(low));
            searchSourceBuilder.size(Math.toIntExact(limit));
            SearchRequest searchRequest = new SearchRequest("opal");
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits) {
                String sourceAsString = hit.getSourceAsString();
                JSONObject jsonObject = new JSONObject(sourceAsString);
                ret.add(jsonObjecttoDataSetMapper.toDataSetLongViewDTO(jsonObject));
            }
        } catch (IOException e) {
            logger.error("An error occurred in getting the results", e);
        }

        return ret;
    }

    @Override
    public List<FilterDTO> getFilters(String searchKey, String[] searchIn) {
        List<FilterDTO> ret = new ArrayList<>();

        DisMaxQueryBuilder searchConstraintQuery = null;
        if (searchKey.isEmpty()) {
            searchConstraintQuery = new DisMaxQueryBuilder().add(QueryBuilders.matchAllQuery());
        } else {
            if (!searchKey.isEmpty()) {
                searchConstraintQuery = getSearchConstraint(searchKey, searchIn);
            }
        }
        ret.add(getFilterValues("http://www.w3.org/ns/dcat#theme", "Theme", "prefLabel", "themes", searchConstraintQuery, true));
        ret.add(getFilterValues("http://purl.org/dc/terms/license", "License", "license", "distributions", searchConstraintQuery, true));
        ret.add(getFilterValues("http://purl.org/dc/terms/publisher", "Publisher", "name", "publisherInfo", searchConstraintQuery, true));
        return ret;
    }

    private FilterDTO getFilterValues(String uri, String title, String fieldname, String path, DisMaxQueryBuilder searchConstraintQuery, boolean nested) {
        FilterDTO filterDTO = FilterDTO.builder()
                .uri(uri)
                .title(title)
                .externalLink(true)
                .isTypeStatic(title.equals("Theme"))
                .values(new ArrayList<>()).build();
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(searchConstraintQuery);
            SearchRequest searchRequest = new SearchRequest("opal");
            searchSourceBuilder.size(0);
            searchRequest.source(searchSourceBuilder);
            Terms terms = null;
            SearchResponse searchResponse = null;

            if (nested) {
                searchSourceBuilder.aggregation(AggregationBuilders.nested("nested", path)
                        .subAggregation(AggregationBuilders.terms("field").field(path + "." + fieldname + ".raw")));
                searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
                ParsedNested parsedNested = searchResponse.getAggregations().get("nested");
                terms = (Terms) parsedNested.getAggregations().get("field");
            } else {
                searchSourceBuilder.aggregation(AggregationBuilders.terms("field").field(fieldname + ".raw"));
                searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
                terms = searchResponse.getAggregations().get("field");
            }

            Collection<Terms.Bucket> buckets = (Collection<Terms.Bucket>) terms.getBuckets();
            Iterator<Terms.Bucket> buckIterator = buckets.iterator();
            while (buckIterator.hasNext()) {
                Terms.Bucket bucket = buckIterator.next();
                String filterUri = bucket.getKey().toString();
                filterDTO.getValues().add(new FilterValueDTO(filterUri, filterUri, filterUri, (int) bucket.getDocCount()));
            }
        } catch (Exception e) {
            logger.error("An error occurred while counting license counts", e);
        }

        return filterDTO;
    }

    @Override
    public Long getCountOfFilterValue(String filterUri, String valueUri, String searchKey, String[] searchIn) {
        FilterValueDTO filterValueDTO = new FilterValueDTO();
        filterValueDTO.setUri(valueUri);
        List<FilterValueDTO> filterValueDTOList = new ArrayList<FilterValueDTO>();
        filterValueDTOList.add(filterValueDTO);
        FilterDTO filterDTO = FilterDTO.builder().uri(filterUri).values(filterValueDTOList).build();
        FilterDTO[] filterDTOS = new FilterDTO[]{filterDTO};
        return getNumberOfDataSets(searchKey, searchIn, null, filterDTOS);
    }

    @Override
    public DataSetDTO getDataSet(String uri) {
        return new DataSetDTO();
    }

    @Override
    public FilterDTO getTopFilterOptions(String filterType, String searchKey, String[] searchIn, String filterText) {
        // TODO: 12/19/19 It is not complete
        return FilterDTO.builder()
                .uri("http://www.w3.org/ns/dcat#theme")
                .title("Theme")
                .values(Arrays.asList(
                        new FilterValueDTO("http://publications.europa.eu/resource/authority/data-theme/AGRI", "Agriculture, fisheries, forestry and food", "Agriculture, fisheries, forestry and food", -1),
                        new FilterValueDTO("http://publications.europa.eu/resource/authority/data-theme/EDUC", "Education, culture and sport", "Education, culture and sport", -1),
                        new FilterValueDTO("http://publications.europa.eu/resource/authority/data-theme/ENVI", "Environment", "Environment", -1),
                        new FilterValueDTO("http://publications.europa.eu/resource/authority/data-theme/ENER", "Energy", "Energy", -1),
                        new FilterValueDTO("http://publications.europa.eu/resource/authority/data-theme/TRAN", "Transport", "Transport", -1),
                        new FilterValueDTO("http://publications.europa.eu/resource/authority/data-theme/TECH", "Science and technology", "Science and technology", -1),
                        new FilterValueDTO("http://publications.europa.eu/resource/authority/data-theme/ECON", "Economy and finance", "Economy and finance", -1),
                        new FilterValueDTO("http://publications.europa.eu/resource/authority/data-theme/SOCI", "Population and society", "Population and society", -1),
                        new FilterValueDTO("http://publications.europa.eu/resource/authority/data-theme/HEAL", "Health", "Health", -1),
                        new FilterValueDTO("http://publications.europa.eu/resource/authority/data-theme/GOVE", "Government and public sector", "Government and public sector", -1),
                        new FilterValueDTO("http://publications.europa.eu/resource/authority/data-theme/REGI", "Regions and cities", "Regions and cities", -1),
                        new FilterValueDTO("http://publications.europa.eu/resource/authority/data-theme/JUST", "Justice, legal system and public safety", "Justice, legal system and public safety", -1),
                        new FilterValueDTO("http://publications.europa.eu/resource/authority/data-theme/INTR", "International issues", "International issues", -1),
                        new FilterValueDTO("http://publications.europa.eu/resource/authority/data-theme/OP_DATPRO", "Provisional data", "Provisional data", -1)
                )).build();
    }

    @Override
    public List<DataSetLongViewDTO> getSubRelatedListOfDataSets(String uri, Long low, Long limit, OrderByDTO orderByDTO, FilterDTO[] filterDTOS) {
        return getSubListOfDataSets("", low, limit, new String[0], orderByDTO, filterDTOS);// TODO: 12/19/19 implement the function
    }

    @Override
    public Long getNumberOfRelatedDataSets(String uri, OrderByDTO orderByDTO, FilterDTO[] filterDTOS) {
        return getNumberOfDataSets("", new String[0], orderByDTO, filterDTOS); // TODO: 12/19/19 implement the function
    }

    private HashMap<String, String> returnFieldDetails(String fieldToSearch) {

        HashMap<String, String> fieldDetails = new HashMap<String, String>();

        JSONObject jsonObject = new JSONObject("{\n" +
                "   \"properties\":{\n" +
                "      \"title\":{\n" +
                "         \"type\":\"text\"\n" +
                "      },\n" +
                "      \"description\":{\n" +
                "         \"type\":\"text\"\n" +
                "      },\n" +
                "      \"URL\":{\n" +
                "         \"type\":\"keyword\"\n" +
                "      },\n" +
                "      \"landingPage\":{\n" +
                "         \"type\":\"text\"\n" +
                "      },\n" +
                "      \"language\":{\n" +
                "         \"type\":\"keyword\"\n" +
                "      },\n" +
                "      \"license\":{\n" +
                "         \"type\":\"text\",\n" +
                "        \"fields\": {\n" +
                "          \"raw\": { \n" +
                "            \"type\":  \"keyword\"\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"keywords\":{\n" +
                "         \"type\":\"nested\",\n" +
                "         \"properties\":{\n" +
                "            \"keyword\":{\n" +
                "               \"type\":\"text\",\n" +
                "\t\t\"fields\": {\n" +
                "\t\t  \"raw\": { \n" +
                "\t\t    \"type\":  \"keyword\"\n" +
                "\t\t  }\n" +
                "\t\t}\n" +
                "            }\n" +
                "         }\n" +
                "      },\n" +
                "      \"themes\":{\n" +
                "         \"type\":\"nested\",\n" +
                "         \"properties\":{\n" +
                "            \"type\":{\n" +
                "               \"type\":\"keyword\"\n" +
                "            },\n" +
                "            \"theme\":{\n" +
                "               \"type\":\"keyword\",\n" +
                "\t\t\"fields\": {\n" +
                "\t\t  \"raw\": { \n" +
                "\t\t    \"type\":  \"keyword\"\n" +
                "\t\t  }\n" +
                "\t\t}\n" +
                "            },\n" +
                "            \"prefLabel\":{\n" +
                "               \"type\":\"text\",\n" +
                "\t\t\"fields\": {\n" +
                "\t\t  \"raw\": { \n" +
                "\t\t    \"type\":  \"keyword\"\n" +
                "\t\t  }\n" +
                "\t\t}\n" +
                "            }\n" +
                "         }\n" +
                "      },\n" +
                "      \"issued\":{\n" +
                "         \"type\":\"text\"\n" +
                "      },\n" +
                "      \"modified\":{\n" +
                "         \"type\":\"text\"\n" +
                "      },\n" +
                "      \"modifiedYear\":{\n" +
                "         \"type\":\"keyword\"\n" +
                "      },\n" +
                "      \"modifiedMonth\":{\n" +
                "         \"type\":\"keyword\"\n" +
                "      },\n" +
                "      \"metadataQuality\":{\n" +
                "         \"type\":\"keyword\"\n" +
                "      },\n" +
                "      \"hasQualityMeasurements\":{\n" +
                "         \"type\":\"nested\",\n" +
                "         \"properties\":{\n" +
                "            \"isMeasurementOf\":{\n" +
                "               \"type\":\"keyword\"\n" +
                "            },\n" +
                "            \"hasQualityMeasurement\":{\n" +
                "               \"type\":\"keyword\"\n" +
                "            },\n" +
                "            \"value\":{\n" +
                "               \"type\":\"float\"\n" +
                "            },\n" +
                "            \"type\":{\n" +
                "               \"type\":\"keyword\"\n" +
                "            }\n" +
                "         }\n" +
                "      },\n" +
                "      \"publisherInfo\":{\n" +
                "         \"type\":\"nested\",\n" +
                "         \"properties\":{\n" +
                "            \"name\":{\n" +
                "               \"type\":\"text\",\n" +
                "\t\t\"fields\": {\n" +
                "\t\t  \"raw\": { \n" +
                "\t\t    \"type\":  \"keyword\"\n" +
                "\t\t  }\n" +
                "\t\t}\n" +
                "            },\n" +
                "            \"publisher\":{\n" +
                "               \"type\":\"keyword\",\n" +
                "\t\t\"fields\": {\n" +
                "\t\t  \"raw\": { \n" +
                "\t\t    \"type\":  \"keyword\"\n" +
                "\t\t  }\n" +
                "\t\t}\n" +
                "            },\n" +
                "            \"type\":{\n" +
                "               \"type\":\"keyword\"\n" +
                "            }\n" +
                "         }\n" +
                "      },\n" +
                "      \"spatialInfo\":{\n" +
                "         \"type\":\"nested\",\n" +
                "         \"properties\":{\n" +
                "            \"geometry\":{\n" +
                "               \"type\":\"text\"\n" +
                "            },\n" +
                "            \"spatial\":{\n" +
                "               \"type\":\"keyword\"\n" +
                "            },\n" +
                "            \"type\":{\n" +
                "               \"type\":\"keyword\"\n" +
                "            }\n" +
                "         }\n" +
                "      },\n" +
                "      \"contactPointInfo\":{\n" +
                "         \"type\":\"nested\",\n" +
                "         \"properties\":{\n" +
                "            \"hasEmail\":{\n" +
                "               \"type\":\"text\"\n" +
                "            },\n" +
                "            \"contactPoint\":{\n" +
                "               \"type\":\"keyword\"\n" +
                "            },\n" +
                "            \"fn\":{\n" +
                "               \"type\":\"text\"\n" +
                "            },\n" +
                "            \"type\":{\n" +
                "               \"type\":\"keyword\"\n" +
                "            }\n" +
                "         }\n" +
                "      },\n" +
                "      \"distributions\":{\n" +
                "         \"type\":\"nested\",\n" +
                "         \"properties\":{\n" +
                "            \"distribution\":{\n" +
                "               \"type\":\"keyword\"\n" +
                "            },\n" +
                "            \"type\":{\n" +
                "               \"type\":\"keyword\"\n" +
                "            },\n" +
                "            \"title\":{\n" +
                "               \"type\":\"text\"\n" +
                "            },\n" +
                "            \"description\":{\n" +
                "               \"type\":\"text\"\n" +
                "            },\n" +
                "            \"issued\":{\n" +
                "               \"type\":\"text\"\n" +
                "            },\n" +
                "            \"modified\":{\n" +
                "               \"type\":\"text\"\n" +
                "            },\n" +
                "            \"license\":{\n" +
                "               \"type\":\"text\",\n" +
                "\t\t\"fields\": {\n" +
                "\t\t  \"raw\": { \n" +
                "\t\t    \"type\":  \"keyword\"\n" +
                "\t\t  }\n" +
                "\t\t}\n" +
                "            },\n" +
                "            \"accessURL\":{\n" +
                "               \"type\":\"keyword\"\n" +
                "            },\n" +
                "            \"downloadURL\":{\n" +
                "               \"type\":\"keyword\"\n" +
                "            },\n" +
                "            \"format\":{\n" +
                "               \"type\":\"text\",\n" +
                "\t\t\"fields\": {\n" +
                "\t\t  \"raw\": { \n" +
                "\t\t    \"type\":  \"keyword\"\n" +
                "\t\t  }\n" +
                "\t\t}\n" +
                "            },\n" +
                "            \"mediaType\":{\n" +
                "               \"type\":\"text\",\n" +
                "\t\t\"fields\": {\n" +
                "\t\t  \"raw\": { \n" +
                "\t\t    \"type\":  \"keyword\"\n" +
                "\t\t  }\n" +
                "\t\t}\n" +
                "            },\n" +
                "            \"byteSize\":{\n" +
                "               \"type\":\"text\"\n" +
                "            },\n" +
                "            \"rights\":{\n" +
                "               \"type\":\"keyword\"\n" +
                "            }\n" +
                "         }\n" +
                "      },\n" +
                "      \"accrualPeriodicity\":{\n" +
                "         \"type\":\"keyword\"\n" +
                "      },\n" +
                "      \"identifier\":{\n" +
                "         \"type\":\"keyword\"\n" +
                "      },\n" +
                "      \"temporalInfo\":{\n" +
                "         \"type\":\"nested\",\n" +
                "         \"properties\":{\n" +
                "            \"endDate\":{\n" +
                "               \"type\":\"date\"\n" +
                "            },\n" +
                "            \"temporal\":{\n" +
                "               \"type\":\"keyword\"\n" +
                "            },\n" +
                "            \"startDate\":{\n" +
                "               \"type\":\"date\"\n" +
                "            },\n" +
                "            \"type\":{\n" +
                "               \"type\":\"keyword\"\n" +
                "            }\n" +
                "         }\n" +
                "      }\n" +
                "   }\n" +
                "}");

        Iterator<String> keys = jsonObject.getJSONObject("properties").keys();
        while (keys.hasNext()) {
            String type = null;
            String parent = null;
            boolean nested = false;
            String key = keys.next();
            if (key.equals(fieldToSearch)) {
                type = jsonObject.getJSONObject("properties").getJSONObject(key).get("type").toString();
                fieldDetails.put("type", type);
                fieldDetails.put("parent", parent);
                fieldDetails.put("nested", String.valueOf(nested));
                break;
            } else {
                if (jsonObject.getJSONObject("properties").getJSONObject(key).has("properties")) {
                    parent = key;
                    nested = true;
                    Iterator<String> innerKeys = jsonObject.getJSONObject("properties").getJSONObject(key).getJSONObject("properties").keys();
                    while (innerKeys.hasNext()) {
                        String innerKey = innerKeys.next();
                        if (innerKey.equals(fieldToSearch)) {
                            type = jsonObject.getJSONObject("properties").getJSONObject(key).getJSONObject("properties").getJSONObject(innerKey).get("type").toString();
                            fieldDetails.put("type", type);
                            fieldDetails.put("parent", parent);
                            fieldDetails.put("nested", String.valueOf(nested));
                            break;
                        }
                    }
                }
            }
        }
        return fieldDetails;
    }

    public static void main(String[] args) {

    }


}
