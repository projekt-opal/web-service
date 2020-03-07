package org.dice_research.opal.webservice.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.dice_research.opal.webservice.config.ThemeConfiguration;
import org.dice_research.opal.webservice.model.dto.*;
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
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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

            searchRequest.indices("opal");
            searchRequest.source(searchSourceBuilder);

            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            return search.getHits().getTotalHits().value;
        } catch (IOException e) {
            log.error("", e);
            return -1L;
        }
    }

    public List<DataSetLongViewDTO> getSublistOfDataSets(SearchDTO searchDTO, Integer low, Integer limit) {
        List<DataSetLongViewDTO> ret = new ArrayList<>();
        try {
            SearchRequest searchRequest = new SearchRequest();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.from(low);
            searchSourceBuilder.size(limit);

            BoolQueryBuilder qb = QueryBuilders.boolQuery();
            QueryBuilder searchKeyQuery = getSearchKeyQuery(searchDTO.getSearchKey(), searchDTO.getSearchIn());
            List<QueryBuilder> filtersQueries = getFiltersQueries(searchDTO.getFilters());
            List<QueryBuilder> orderByQueries = getOrderBy(searchDTO); // TODO: 3/4/20 for the ones using sort maybe more code is needed

            qb.must(searchKeyQuery);
            filtersQueries.forEach(qb::must);
            orderByQueries.forEach(qb::should);

            searchSourceBuilder.query(qb);

            searchRequest.indices("opal");
            searchRequest.source(searchSourceBuilder);

            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] hits = search.getHits().getHits();
            for (SearchHit hit : hits) {
                String sourceAsString = hit.getSourceAsString();
                JSONObject jsonObject = new JSONObject(sourceAsString);
                DataSetLongViewDTO dataSetLongViewDTO = JsonObjectToDataSetMapper.toDataSetLongViewDTO(jsonObject);
                ret.add(dataSetLongViewDTO);
            }
        } catch (IOException e) {
            log.error("", e);
        }
        return ret;
    }

    public List<FilterDTO> getFilters(SearchDTO searchDTO) {
        List<FilterDTO> ret = new ArrayList<>();
        try {
            ret.add(getFilterListOfThemes(searchDTO));
            ret.add(getFilterListOfPublishers(searchDTO));
            ret.add(getFilterListOfLicenses(searchDTO));
            ret.add(getIssueDateFilter());
        } catch (IOException e) {
            log.error("", e);
        }
        return ret;
    }

    public FilterDTO getTopFiltersThatContain(SearchDTO searchDTO, String filterGroupTitle, String containsText) {
        try {
            Optional<FilterDTO> optionalFilterDTO = Arrays.stream(searchDTO.getFilters())
                    .filter(f -> f.getFilterGroupTitle().equals(filterGroupTitle)).findFirst();
            if (!optionalFilterDTO.isPresent()) return null;
            FilterDTO filterDTO = optionalFilterDTO.get();
            List<ValueDTO> values = calculateTopValues(searchDTO, filterDTO.getSearchField(), containsText);

            //update absoluteCount
            values.forEach(v ->
                    v.getCount().setAbsolute(calculateTheAbsoluteCount(searchDTO, filterDTO.getSearchField(), v.getValue())));

            //update relativeCount
            values.forEach(v ->
                    v.getCount().setRelative(calculateTheRelativeCount(searchDTO, filterDTO.getSearchField(), v.getValue())));

            filterDTO.setValues(values);
            return filterDTO;
        } catch (IOException e) {
            log.error("", e);
        }
        return null;
    }

    private FilterDTO getIssueDateFilter() {
        return FilterDTO.builder().filterGroupTitle("Issue Date").searchField("issued")
                .hasExternalLink(false).hasStaticValues(true).selectedRangeValues(new RangeDTO()).build();
    }

    private FilterDTO getFilterListOfThemes(SearchDTO searchDTO) {
        String searchField = "themes";
        List<ValueDTO> values = getTopThemeValues();

        //update relativeCount
        values.forEach(v ->
                v.getCount().setRelative(calculateTheRelativeCount(searchDTO, searchField,
                        themeConfiguration.getRevMap().get(v.getValue()))));

        //update absoluteCount
        values.forEach(v ->
                v.getCount().setAbsolute(calculateTheAbsoluteCount(searchDTO, searchField,
                        themeConfiguration.getRevMap().get(v.getValue()))));

        return FilterDTO.builder().filterGroupTitle("Theme").hasExternalLink(true).hasStaticValues(false).values(values)
                .searchField(searchField).build();
    }

    private FilterDTO getFilterListOfLicenses(SearchDTO searchDTO) throws IOException {
        String searchField = "distributions.license.uri.keyword";
        List<ValueDTO> values = calculateTopValues(searchDTO, searchField, null);

        //update absoluteCount
        values.forEach(v ->
                v.getCount().setAbsolute(calculateTheAbsoluteCount(searchDTO, searchField, v.getValue())));

        //update relativeCount
        values.forEach(v ->
                v.getCount().setRelative(calculateTheRelativeCount(searchDTO, searchField, v.getValue())));
        return FilterDTO.builder().filterGroupTitle("License").hasExternalLink(true).hasStaticValues(false).values(values)
                .searchField(searchField).build();
    }

    private FilterDTO getFilterListOfPublishers(SearchDTO searchDTO) throws IOException {
        String searchField = "publisher.name.keyword";
        List<ValueDTO> values = calculateTopValues(searchDTO, searchField, null);


        //update absoluteCount
        values.forEach(v ->
                v.getCount().setAbsolute(calculateTheAbsoluteCount(searchDTO, searchField, v.getValue())));

        //update relativeCount
        values.forEach(v ->
                v.getCount().setRelative(calculateTheRelativeCount(searchDTO, searchField, v.getValue())));
        return FilterDTO.builder().filterGroupTitle("Publisher").hasExternalLink(true).hasStaticValues(false).values(values)
                .searchField(searchField).build();
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
        searchRequest.indices("opal");

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

    private int calculateTheAbsoluteCount(SearchDTO searchDTO, String fieldQuery, String value) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        QueryBuilder searchKeyQuery = getSearchKeyQuery(searchDTO.getSearchKey(), searchDTO.getSearchIn());
        boolQueryBuilder.must(searchKeyQuery);

        TermQueryBuilder query = QueryBuilders.termQuery(fieldQuery, value);
        if (fieldQuery.contains(".")) {
            String[] split = fieldQuery.split("\\.");
            int lastIdx = (split[split.length - 1].equals("keyword")) ? split.length - 2 : split.length - 1;
            String[] subPath = Arrays.copyOf(split, lastIdx);
            boolQueryBuilder.must(QueryBuilders.nestedQuery(String.join(".", subPath), query, ScoreMode.Avg));
        } else
            boolQueryBuilder.must(query);

        searchSourceBuilder.query(boolQueryBuilder);

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("opal");
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            return (int) searchResponse.getHits().getTotalHits().value;
        } catch (IOException e) {
            log.error("", e);
            return -1;
        }

    }

    private int calculateTheRelativeCount(SearchDTO searchDTO, String fieldQuery, String value) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        QueryBuilder searchKeyQuery = getSearchKeyQuery(searchDTO.getSearchKey(), searchDTO.getSearchIn());
        boolQueryBuilder.must(searchKeyQuery);

        List<QueryBuilder> filtersQueries = getFiltersQueries(searchDTO.getFilters());
        filtersQueries.forEach(boolQueryBuilder::must);

        TermQueryBuilder query = QueryBuilders.termQuery(fieldQuery, value);
        if (fieldQuery.contains(".")) {
            String nestedPath = calculateNestedPath(fieldQuery);
            boolQueryBuilder.must(QueryBuilders.nestedQuery(nestedPath, query, ScoreMode.Avg));
        } else
            boolQueryBuilder.must(query);

        searchSourceBuilder.query(boolQueryBuilder);

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("opal");
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            return (int) searchResponse.getHits().getTotalHits().value;
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
        // TODO: 3/4/20 For issue date and location sort must be used
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

        RangeQueryBuilder range = QueryBuilders.rangeQuery(getFieldName(filterDTO.getSearchField()));
        if (!selectedRangeValues.getGte().equals("-1")) range.gte(selectedRangeValues.getGte());
        if (!selectedRangeValues.getLte().equals("-1")) range.lte(selectedRangeValues.getLte() + "||+1d");
        return range;
    }

    private QueryBuilder getFilterQuery(FilterDTO filterDTO) {
        if (filterDTO.getValues() == null || filterDTO.getValues().isEmpty() ||
                filterDTO.getValues().stream().noneMatch(ValueDTO::getSelected)
        ) return null;
        String fieldName = getFieldName(filterDTO.getSearchField());
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        for (ValueDTO v : filterDTO.getValues()) {
            if(v.getSelected())
                filterQueryBuilder.should(QueryBuilders.matchQuery(fieldName, v.getValue()));
        }
        filterQueryBuilder.minimumShouldMatch(1);

        if (filterDTO.getSearchField().contains("."))
            return QueryBuilders.nestedQuery(calculateNestedPath(filterDTO.getSearchField()), filterQueryBuilder, ScoreMode.Avg);
        return filterQueryBuilder;
    }

    private String getFieldName(String searchField) {
//        switch (searchField) {
//            case "http://www.w3.org/ns/dcat#theme":
//                return "themes";
//            case "http://purl.org/dc/terms/publisher":
//                return "publisher.uri";
//            case "http://purl.org/dc/terms/creator":
//                return "creator.uri";
//            case "http://purl.org/dc/terms/license":
//                return "distributions.license.uri.keyword";
//        } todo probably we can remove this function
        return searchField;
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
            return QueryBuilders.multiMatchQuery(searchKey, fieldNames.toArray(new String[0]));
        }
    }
}
