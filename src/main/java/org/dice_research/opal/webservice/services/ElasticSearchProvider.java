package org.dice_research.opal.webservice.services;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.dice_research.opal.webservice.config.ThemeConfiguration;
import org.dice_research.opal.webservice.model.entity.DataSet;
import org.dice_research.opal.webservice.model.entity.dto.*;
import org.dice_research.opal.webservice.model.mapper.JsonObjectToDataSetMapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.analysis.AnalysisRegistry;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Cardinality;
import org.elasticsearch.search.aggregations.metrics.CardinalityAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.NestedSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ElasticSearchProvider {

    private final RestHighLevelClient restHighLevelClient;
    private final ThemeConfiguration themeConfiguration;
    
    private static final int LIST_MAX_RESULTS = 300;

    @Value("${ES_INDEX}")
    private String es_index;

    @Autowired
    public ElasticSearchProvider(RestHighLevelClient restHighLevelClient, ThemeConfiguration themeConfiguration) {
        this.restHighLevelClient = restHighLevelClient;
        this.themeConfiguration = themeConfiguration;
    }

    public long getNumberOfDataSets(SearchDTO searchDTO) {
        try {
            SearchRequest searchRequest = new SearchRequest();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(0);

            BoolQueryBuilder qb = QueryBuilders.boolQuery();
            QueryBuilder searchKeyQuery = getSearchKeyQuery(searchDTO.getSearchKey(), searchDTO.getSearchIn());
            List<QueryBuilder> filtersQueries = getFiltersQueries(searchDTO.getFilters());

            qb.must(searchKeyQuery);
            filtersQueries.forEach(qb::must);

            searchSourceBuilder.query(qb);

            CardinalityAggregationBuilder cardinalityAggregationBuilder =
                    AggregationBuilders.cardinality("aggs_number_of_datasets").precisionThreshold(100).field("uri");

            searchSourceBuilder.aggregation(cardinalityAggregationBuilder);

            searchRequest.indices(es_index);
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Cardinality cardinality = searchResponse.getAggregations().get("aggs_number_of_datasets");
            return cardinality.getValue();
        } catch (IOException e) {
            log.error("", e);
        }
        return -1L;
    }

    public List<DataSetDTO> getSublistOfDataSets(SearchDTO searchDTO, Integer low, Integer limit) {
        List<DataSetDTO> ret = new ArrayList<>();
        try {
            SearchRequest searchRequest = new SearchRequest();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(low);
            searchSourceBuilder.size(limit);

            BoolQueryBuilder qb = QueryBuilders.boolQuery();
            QueryBuilder searchKeyQuery = getSearchKeyQuery(searchDTO.getSearchKey(), searchDTO.getSearchIn());
            List<QueryBuilder> filtersQueries = getFiltersQueries(searchDTO.getFilters());

            if (searchDTO.getOrderBy().getSelectedOrderValue().equals("location")) {
                GeoDistanceSortBuilder geoDistanceSortBuilder = SortBuilders.geoDistanceSort("spatial.geometry",
                        searchDTO.getOrderBy().getLatitude(), searchDTO.getOrderBy().getLongitude());
//                geoDistanceSortBuilder.setNestedPath("spatial");
                NestedSortBuilder nestedSort = new NestedSortBuilder("spatial");
                geoDistanceSortBuilder.setNestedSort(nestedSort);

                searchSourceBuilder.sort(geoDistanceSortBuilder);
            } else {
                List<QueryBuilder> orderByQueries = getOrderBy(searchDTO);
                orderByQueries.forEach(qb::should);
            }


            qb.must(searchKeyQuery);
            filtersQueries.forEach(qb::must);
            searchSourceBuilder.query(qb);

            searchRequest.indices(es_index);
            searchRequest.source(searchSourceBuilder);

            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] hits = search.getHits().getHits();
            for (SearchHit hit : hits) {
                String sourceAsString = hit.getSourceAsString();
                JSONObject jsonObject = new JSONObject(sourceAsString);
                DataSetDTO dataSetDTO = JsonObjectToDataSetMapper.toDataSetLongViewDTO(jsonObject);
                ret.add(dataSetDTO);
            }
        } catch (IOException e) {
            log.error("", e);
        }
        return ret;
    }

    public List<FilterDTO> getFilters(SearchDTO searchDTO, String uri) {
        if (searchDTO.getFilters().length == 0)
            return generateFilters(searchDTO, uri);
        updateRelativeCountOfAllFilters(searchDTO, uri);
        updateAbsoluteCountOfAllFilters(searchDTO, uri);
        return Arrays.asList(searchDTO.getFilters());
    }

    private void updateAbsoluteCountOfAllFilters(SearchDTO searchDTO, String uri) {
        Arrays.stream(searchDTO.getFilters()).forEach(filterDTO ->
                filterDTO.getValues().forEach(v ->
                        v.getCount().setAbsolute(calculateTheAbsoluteCount(searchDTO, uri, filterDTO.getSearchField(),
                                filterDTO.getSearchField().equals("themes") ? themeConfiguration.getRevMap().get(v.getValue()) : v.getValue())))
        );
    }

    private void updateRelativeCountOfAllFilters(SearchDTO searchDTO, String uri) {
        Arrays.stream(searchDTO.getFilters()).forEach(
                filterDTO -> updateRelativeCount(searchDTO, uri, filterDTO.getSearchField(), filterDTO.getValues()));
    }

    private List<FilterDTO> generateFilters(SearchDTO searchDTO, String uri) {
        List<FilterDTO> ret = new ArrayList<>();
        try {
            ret.add(getFilterListOfThemes(searchDTO, uri));
            ret.add(getFilterListOfPublishers(searchDTO, uri));
            ret.add(getFilterListOfLicenses(searchDTO, uri));
//            ret.add(getIssueDateFilter());
        } catch (IOException e) {
            log.error("", e);
        }
        return ret;
    }

    public FilterDTO getTopFiltersThatContain(SearchDTO searchDTO, String uri, String filterGroupTitle, String containsText) {
        try {
            Optional<FilterDTO> optionalFilterDTO = Arrays.stream(searchDTO.getFilters())
                    .filter(f -> f.getFilterGroupTitle().equals(filterGroupTitle)).findFirst();
            if (!optionalFilterDTO.isPresent()) return null;
            FilterDTO filterDTO = optionalFilterDTO.get();
            List<ValueDTO> values = calculateTopValues(searchDTO, filterDTO.getSearchField(), containsText);

            updateRelativeCount(searchDTO, uri, filterDTO.getSearchField(), values);
            updateAbsoluteCount(searchDTO, uri, filterDTO.getSearchField(), values);

            filterDTO.setValues(values);
            return filterDTO;
        } catch (IOException e) {
            log.error("", e);
        }
        return null;
    }

    public DataSet getDataSet(String uri) {

        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(1);
            searchSourceBuilder.query(QueryBuilders.termQuery("uri", uri));

            SearchRequest searchRequest = new SearchRequest();
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse.getHits().getTotalHits().value >= 1) {
                SearchHit[] hits = searchResponse.getHits().getHits();
                SearchHit hit = hits[0];
                String sourceAsString = hit.getSourceAsString();
                DataSet dataSet = new Gson().fromJson(sourceAsString, DataSet.class);
                return dataSet;
            }
        } catch (IOException e) {
            log.error("", e);
        }


        return null;
    }

    public Long getNumberOfRelatedDataSets(SearchDTO searchDTO, String uri) {

        try {
            DataSet dataSet = getDataSet(uri);
            if (dataSet == null) return 0L;

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(0);

            BoolQueryBuilder query = QueryBuilders.boolQuery();

            List<QueryBuilder> filtersQueries = getFiltersQueries(searchDTO.getFilters());
            filtersQueries.forEach(query::must);

            addRelatedQuery(dataSet, query);

            searchSourceBuilder.query(query);


            CardinalityAggregationBuilder cardinalityAggregationBuilder =
                    AggregationBuilders.cardinality("aggs_number_of_datasets").precisionThreshold(100).field("uri");
            searchSourceBuilder.aggregation(cardinalityAggregationBuilder);


            SearchRequest searchRequest = new SearchRequest();
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Cardinality cardinality = searchResponse.getAggregations().get("aggs_number_of_datasets");
            return cardinality.getValue();
        } catch (IOException e) {
            log.error("", e);
        }
        return -1L;
    }

    private void addRelatedQuery(DataSet dataSet, BoolQueryBuilder query) {
        query.minimumShouldMatch(1);
        if (dataSet.getTitle() != null)
            query.should(QueryBuilders.matchQuery("title", dataSet.getTitle()));
        if (dataSet.getTitle_de() != null)
            query.should(QueryBuilders.matchQuery("title_de", dataSet.getTitle_de()));
        List<String> filteredMaxkw = null;
        if (dataSet.getKeywords() != null)
            filteredMaxkw = dataSet.getKeywords().subList(0, LIST_MAX_RESULTS);
        if (filteredMaxkw != null)
        	filteredMaxkw.forEach(k -> query.should(QueryBuilders.matchQuery("keywords", k)));
        if (dataSet.getKeywords_de() != null)
            dataSet.getKeywords_de().forEach(k -> query.should(QueryBuilders.matchQuery("keywords_de", k)));
        query.mustNot(QueryBuilders.termQuery("uri", dataSet.getUri()));
    }

    public List<DataSetDTO> getSubListOfRelatedDataSets(SearchDTO searchDTO, String uri, Integer low, Integer limit) {
        List<DataSetDTO> ret = new ArrayList<>();
        try {
            DataSet dataSet = getDataSet(uri);
            if (dataSet == null) return ret;

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(low);
            searchSourceBuilder.size(limit);

            BoolQueryBuilder qb = QueryBuilders.boolQuery();

            List<QueryBuilder> filtersQueries = getFiltersQueries(searchDTO.getFilters());
            filtersQueries.forEach(qb::must);

            if (searchDTO.getOrderBy().getSelectedOrderValue().equals("location")) {
                GeoDistanceSortBuilder geoDistanceSortBuilder = SortBuilders.geoDistanceSort("spatial.geometry",
                        searchDTO.getOrderBy().getLatitude(), searchDTO.getOrderBy().getLongitude());
//                geoDistanceSortBuilder.setNestedPath("spatial");
                NestedSortBuilder nestedSort = new NestedSortBuilder("spatial");
                geoDistanceSortBuilder.setNestedSort(nestedSort);

                searchSourceBuilder.sort(geoDistanceSortBuilder);
            } else {
                List<QueryBuilder> orderByQueries = getOrderBy(searchDTO);
                orderByQueries.forEach(qb::should);
            }
            addRelatedQuery(dataSet, qb);

            searchSourceBuilder.query(qb);
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit hit : searchResponse.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                JSONObject jsonObject = new JSONObject(sourceAsString);
                DataSetDTO dataSetDTO = JsonObjectToDataSetMapper.toDataSetLongViewDTO(jsonObject);
                ret.add(dataSetDTO);
            }
        } catch (IOException e) {
            log.error("", e);
        }
        return ret;
    }

    private FilterDTO getIssueDateFilter() {
        return FilterDTO.builder().filterGroupTitle("Issue Date").searchField("issued")
                .hasExternalLink(false).hasStaticValues(true).selectedRangeValues(new RangeDTO()).build();
    }

    private FilterDTO getFilterListOfThemes(SearchDTO searchDTO, String uri) {
        String searchField = "themes";
        List<ValueDTO> values = getTopThemeValues();

        updateRelativeCount(searchDTO, uri, searchField, values);
        updateAbsoluteCount(searchDTO, uri, searchField, values);

        return FilterDTO.builder().filterGroupTitle("Theme").hasExternalLink(true).hasStaticValues(true).values(values)
                .searchField(searchField).build();
    }

    private FilterDTO getFilterListOfLicenses(SearchDTO searchDTO, String uri) throws IOException {
        String searchField = "distributions.license.uri.keyword";
        List<ValueDTO> values = calculateTopValues(searchDTO, searchField, null);

        updateRelativeCount(searchDTO, uri, searchField, values);
        updateAbsoluteCount(searchDTO, uri, searchField, values);

        return FilterDTO.builder().filterGroupTitle("License").hasExternalLink(true).hasStaticValues(false).values(values)
                .searchField(searchField).build();
    }

    private FilterDTO getFilterListOfPublishers(SearchDTO searchDTO, String uri) throws IOException {
        String searchField = "publisher.name.keyword";
        List<ValueDTO> values = calculateTopValues(searchDTO, searchField, null);

        updateRelativeCount(searchDTO, uri, searchField, values);
        updateAbsoluteCount(searchDTO, uri, searchField, values);

        return FilterDTO.builder().filterGroupTitle("Publisher").hasExternalLink(true).hasStaticValues(false).values(values)
                .searchField(searchField).build();
    }

    private void updateRelativeCount(SearchDTO searchDTO, String uri, String searchField, List<ValueDTO> values) {
    	
        values.forEach(v ->
               v.getCount().setRelative(calculateTheRelativeCount(searchDTO, uri,
                     searchField, searchField.equals("themes") ? themeConfiguration.getRevMap().get(v.getValue()) : v.getValue())));
    }

    private void updateAbsoluteCount(SearchDTO searchDTO, String uri, String searchField, List<ValueDTO> values) {
        values.forEach(v ->
                v.getCount().setAbsolute(calculateTheAbsoluteCount(searchDTO, uri, searchField,
                        searchField.equals("themes") ? themeConfiguration.getRevMap().get(v.getValue()) : v.getValue())));
    }

    private List<ValueDTO> getTopThemeValues() {
        List<ValueDTO> values = new ArrayList<>();
        themeConfiguration.getMap().forEach((uri, value) -> {
            ValueDTO valueDTO = new ValueDTO();
            valueDTO.setValue(value);
            valueDTO.setCount(new CounterDTO());
            values.add(valueDTO);
        });

        return values;
    }

    private List<ValueDTO> calculateTopValues(SearchDTO searchDTO, String searchField, String containsText) throws IOException {
        List<ValueDTO> values = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(es_index);

        SearchSourceBuilder searchSourceBuilder = generateTopQuery(searchDTO, searchField, containsText);
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        Aggregation top_nestedPath_outer = searchResponse.getAggregations().get("top_nestedPath");
        Aggregation top_nestedPath_inner = ((ParsedNested) top_nestedPath_outer).getAggregations().asList().get(0);
        List<? extends Terms.Bucket> buckets = ((ParsedStringTerms) top_nestedPath_inner).getBuckets();

        buckets.forEach(b -> {
            String keyAsString = b.getKeyAsString();
            ValueDTO valueDTO = new ValueDTO();
            valueDTO.setValue(keyAsString);
            valueDTO.setCount(new CounterDTO());
            values.add(valueDTO);
        });
        return values;
    }

    private long calculateTheAbsoluteCount(SearchDTO searchDTO, String uri, String fieldQuery, String value) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        QueryBuilder searchKeyQuery = getSearchKeyQuery(searchDTO.getSearchKey(), searchDTO.getSearchIn());
        boolQueryBuilder.must(searchKeyQuery);

        if (uri != null) {
            DataSet dataSet = getDataSet(uri);
            if (dataSet != null)
                addRelatedQuery(dataSet, boolQueryBuilder);
        }

        TermQueryBuilder query = QueryBuilders.termQuery(fieldQuery, value);
        if (fieldQuery.contains(".")) {
            String[] split = fieldQuery.split("\\.");
            int lastIdx = (split[split.length - 1].equals("keyword")) ? split.length - 2 : split.length - 1;
            String[] subPath = Arrays.copyOf(split, lastIdx);
            boolQueryBuilder.must(QueryBuilders.nestedQuery(String.join(".", subPath), query, ScoreMode.Avg));
        } else
            boolQueryBuilder.must(query);

        searchSourceBuilder.query(boolQueryBuilder);


        CardinalityAggregationBuilder cardinalityAggregationBuilder =
                AggregationBuilders.cardinality("aggs_number_of_datasets").precisionThreshold(100).field("uri");
        searchSourceBuilder.aggregation(cardinalityAggregationBuilder);

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(es_index);
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Cardinality cardinality = searchResponse.getAggregations().get("aggs_number_of_datasets");
            return cardinality.getValue();
        } catch (IOException e) {
            log.error("", e);
            return -1;
        }

    }

    private long calculateTheRelativeCount(SearchDTO searchDTO, String uri, String fieldQuery, String value) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        QueryBuilder searchKeyQuery = getSearchKeyQuery(searchDTO.getSearchKey(), searchDTO.getSearchIn());
        boolQueryBuilder.must(searchKeyQuery);

        List<QueryBuilder> filtersQueries = getFiltersQueries(searchDTO.getFilters());
        filtersQueries.forEach(boolQueryBuilder::must);

        if (uri != null) {
            DataSet dataSet = getDataSet(uri);
            if (dataSet != null)
                addRelatedQuery(dataSet, boolQueryBuilder);
        }

        TermQueryBuilder query = QueryBuilders.termQuery(fieldQuery, value);
        if (fieldQuery.contains(".")) {
            String nestedPath = calculateNestedPath(fieldQuery);
            boolQueryBuilder.must(QueryBuilders.nestedQuery(nestedPath, query, ScoreMode.Avg));
        } else
            boolQueryBuilder.must(query);

        searchSourceBuilder.query(boolQueryBuilder);


        CardinalityAggregationBuilder cardinalityAggregationBuilder =
                AggregationBuilders.cardinality("aggs_number_of_datasets").precisionThreshold(100).field("uri");
        searchSourceBuilder.aggregation(cardinalityAggregationBuilder);

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(es_index);
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Cardinality cardinality = searchResponse.getAggregations().get("aggs_number_of_datasets");
            return cardinality.getValue();
        } catch (IOException e) {
            log.error("", e);
            return -1;
        }

    }

    private String calculateNestedPath(String fieldQuery) {
        String[] split = fieldQuery.split("\\.");
        int lastIdx = (split[split.length - 1].equals("keyword")) ? split.length - 2 : split.length - 1;
        String[] subPath = Arrays.copyOf(split, lastIdx);
        return String.join(".", subPath);
    }

    private SearchSourceBuilder generateTopQuery(SearchDTO searchDTO, String searchField, String containsText) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);

        BoolQueryBuilder qb = QueryBuilders.boolQuery();
//            only searchQuery and searchIn are important for generating filter list
        QueryBuilder searchKeyQuery = getSearchKeyQuery(searchDTO.getSearchKey(), searchDTO.getSearchIn());
        qb.must(searchKeyQuery);

        if (containsText != null)
            qb.must(getContainsTextQuery(searchField, containsText));

        searchSourceBuilder.query(qb);

        String nestedPath = calculateNestedPath(searchField);
        NestedAggregationBuilder publisherAggregation = AggregationBuilders.nested("top_nestedPath", nestedPath)
                .subAggregation(AggregationBuilders.terms("top_nestedPath").field(searchField));

        searchSourceBuilder.aggregation(publisherAggregation);
        return searchSourceBuilder;
    }

    private QueryBuilder getContainsTextQuery(String searchField, String containsText) {
        MatchQueryBuilder query = QueryBuilders.matchQuery(removeKeywordFromSearchField(searchField), containsText).analyzer(AnalysisRegistry.DEFAULT_ANALYZER_NAME);
        if (searchField.contains(".")) {
            String nestedPath = calculateNestedPath(searchField);
            return QueryBuilders.nestedQuery(nestedPath, query, ScoreMode.Avg);
        }
        return query;
    }

    private String removeKeywordFromSearchField(String searchField) {
        String[] split = searchField.split("\\.");
        if (!split[split.length - 1].equals("keyword")) return searchField;
        String[] subPath = Arrays.copyOf(split, split.length - 1);
        return String.join(".", subPath);
    }

    private List<QueryBuilder> getOrderBy(SearchDTO searchDTO) {
        List<QueryBuilder> ret = new ArrayList<>();

        String selectedOrderValue = searchDTO.getOrderBy().getSelectedOrderValue();
        if (selectedOrderValue.equals("relevance")) return ret;
        if (selectedOrderValue.equals("title")) {
            ret.add(QueryBuilders.matchQuery("title", searchDTO.getSearchKey()));
            ret.add(QueryBuilders.matchQuery("title_de", searchDTO.getSearchKey()));
            return ret;
        }
        return ret;
    }


    private List<QueryBuilder> getFiltersQueries(FilterDTO[] filters) {

        List<QueryBuilder> ret = new ArrayList<>();

        for (FilterDTO filterDTO : filters) {
            if (filterDTO.getValues() != null) {
                QueryBuilder filterQuery = getFilterQuery(filterDTO);
                if (filterQuery != null)
                    ret.add(filterQuery);
            }
            if (filterDTO.getSelectedRangeValues() != null) {
                QueryBuilder rangeFilterQuery = getRangeFilterQuery(filterDTO);
                if (rangeFilterQuery != null)
                    ret.add(rangeFilterQuery);
            }
        }

        return ret;
    }

    private QueryBuilder getRangeFilterQuery(FilterDTO filterDTO) {
        RangeDTO selectedRangeValues = filterDTO.getSelectedRangeValues();
        if (selectedRangeValues == null ||
                (selectedRangeValues.getGte().equals("-1") && selectedRangeValues.getLte().equals("-1")))
            return null;

        RangeQueryBuilder range = QueryBuilders.rangeQuery(filterDTO.getSearchField());
        if (!selectedRangeValues.getGte().equals("-1")) range.gte(selectedRangeValues.getGte());
        if (!selectedRangeValues.getLte().equals("-1")) range.lte(selectedRangeValues.getLte() + "||+1d");
        return range;
    }

    private QueryBuilder getFilterQuery(FilterDTO filterDTO) {
        if (filterDTO.getValues() == null || filterDTO.getValues().isEmpty() ||
                filterDTO.getValues().stream().noneMatch(ValueDTO::getSelected)
        ) return null;
        String fieldName = filterDTO.getSearchField();
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        for (ValueDTO v : filterDTO.getValues()) {
            if (v.getSelected())
                filterQueryBuilder.should(QueryBuilders.matchQuery(fieldName,
                        fieldName.equals("themes") ? themeConfiguration.getRevMap().get(v.getValue()) : v.getValue()));
        }
        filterQueryBuilder.minimumShouldMatch(1);

        if (filterDTO.getSearchField().contains("."))
            return QueryBuilders.nestedQuery(calculateNestedPath(filterDTO.getSearchField()), filterQueryBuilder, ScoreMode.Avg);
        return filterQueryBuilder;
    }

    private QueryBuilder getSearchKeyQuery(String searchKey, String[] searchIn) {
        if (searchKey == null || searchKey.isEmpty()) {
            return QueryBuilders.matchAllQuery();
        } else {
            List<String> fieldNames = new ArrayList<>();
            if (searchIn.length > 0) for (String x : searchIn) {
                fieldNames.add(x);
                fieldNames.add(x + "_de");
            }
            else
                fieldNames = Arrays.asList("title", "title_de", "description", "description_de", "keywords", "keywords_de");
            return QueryBuilders.multiMatchQuery(searchKey, fieldNames.toArray(new String[0])).fuzziness(1);
        }
    }
}
