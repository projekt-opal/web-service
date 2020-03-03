package org.dice_research.opal.webservice.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.lucene.search.join.ScoreMode;
import org.dice_research.opal.webservice.model.dto.*;
import org.dice_research.opal.webservice.model.mapper.JsonObjecttoDataSetMapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class ElasticSearchProvider {

    private final RestHighLevelClient restHighLevelClient;
    private final JsonObjecttoDataSetMapper jsonObjecttoDataSetMapper;

    @Autowired
    public ElasticSearchProvider(RestHighLevelClient restHighLevelClient, JsonObjecttoDataSetMapper jsonObjecttoDataSetMapper) {
        this.restHighLevelClient = restHighLevelClient;
        this.jsonObjecttoDataSetMapper = jsonObjecttoDataSetMapper;
    }

    public long getNumberOfDataSets(SearchDTO searchDTO) {
        try {
            SearchRequest searchRequest = new SearchRequest();

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.size(0);

            BoolQueryBuilder qb = QueryBuilders.boolQuery();
            QueryBuilder searchKeyQuery = getSearchKeyQuery(searchDTO.getSearchKey(), searchDTO.getSearchIn());
            List<QueryBuilder> filtersQueries = getFiltersQueries(searchDTO.getFilters());

            qb.must(searchKeyQuery).must(searchKeyQuery);
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

        RangeQueryBuilder range = QueryBuilders.rangeQuery(getFieldName(filterDTO.getUri()));
        if (!selectedRangeValues.getGte().equals("-1")) range.gte(selectedRangeValues.getGte());
        if (!selectedRangeValues.getLte().equals("-1")) range.lte(selectedRangeValues.getLte() + "||+1d");
        return range;
    }

    private QueryBuilder getFilterQuery(FilterDTO filterDTO) {
        if (filterDTO.getValues() == null || filterDTO.getValues().isEmpty()) return null;
        String fieldName = getFieldName(filterDTO.getUri());
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        for (ValueDTO v : filterDTO.getValues()) {
            filterQueryBuilder.should(QueryBuilders.matchQuery(fieldName, v.getUri()));
        }
        filterQueryBuilder.minimumShouldMatch(1);

        if (fieldName.equals("distributions.license.uri.keyword"))
            return QueryBuilders.nestedQuery("distributions.license", filterQueryBuilder, ScoreMode.Avg);
        else if (fieldName.equals("publisher.uri"))
            return QueryBuilders.nestedQuery("publisher", filterQueryBuilder, ScoreMode.Avg);
        return filterQueryBuilder;
    }

    private String getFieldName(String uri) {
        switch (uri) {
            case "http://www.w3.org/ns/dcat#theme":
                return "themes";
            case "http://purl.org/dc/terms/publisher":
                return "publisher.uri";
            case "http://purl.org/dc/terms/creator":
                return "creator.uri";
            case "http://purl.org/dc/terms/license":
                return "distributions.license.uri.keyword";
        }
        return uri;
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
