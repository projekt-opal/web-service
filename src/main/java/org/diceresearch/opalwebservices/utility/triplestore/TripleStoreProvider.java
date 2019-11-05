package org.diceresearch.opalwebservices.utility.triplestore;

import org.aksw.commons.util.Pair;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.diceresearch.opalwebservices.model.dto.*;
import org.diceresearch.opalwebservices.model.mapper.ModelToDataSetMapper;
import org.diceresearch.opalwebservices.utility.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Profile(value = {"triplestore", "default"})
@Component
public class TripleStoreProvider implements DataProvider {

    private static final Logger logger = LoggerFactory.getLogger(TripleStoreProvider.class);

    private final SparQLRunner sparQLRunner;
    private final ModelToDataSetMapper modelToDataSetMapper;


    @Autowired
    public TripleStoreProvider(SparQLRunner sparQLRunner, ModelToDataSetMapper modelToDataSetMapper) {
        this.sparQLRunner = sparQLRunner;
        this.modelToDataSetMapper = modelToDataSetMapper;
    }

    @Override
    public long getNumberOfDatasets(String searchQuery, String[] searchIn, String orderBy, ReceivingFilterDTO[] filters) {
        Long num = -1L;
        try {
            ParameterizedSparqlString pss = new ParameterizedSparqlString();

            String filtersString = getFiltersString(searchQuery, filters);

            String query = "SELECT (COUNT(DISTINCT ?s) AS ?num) WHERE {  GRAPH ?g { " +
                    "?s a dcat:Dataset. " + filtersString +
                    "} }";

            pss.setCommandText(query);
            pss.setNsPrefix("dcat", "http://www.w3.org/ns/dcat#");

            num = sparQLRunner.execSelectCount(pss.asQuery());
        } catch (Exception e) {
            logger.error("An error occurred in getting the results", e);
        }
        return num;
    }

    @Override
    public List<DataSetLongViewDTO> getSubListOFDataSets(String searchQuery, Long low, Long limit, String[] searchIn, String orderBy, ReceivingFilterDTO[] filters) {
        List<DataSetLongViewDTO> ret = new ArrayList<>();
        try {
            ParameterizedSparqlString pss = new ParameterizedSparqlString();

            String filtersString = getFiltersString(searchQuery, filters);

            String query = "SELECT DISTINCT ?s WHERE { GRAPH ?g { " +
                    "?s a dcat:Dataset. " + filtersString +
                    "} } OFFSET " + low + " LIMIT " + limit;

            pss.setCommandText(query);
            pss.setNsPrefix("dcat", "http://www.w3.org/ns/dcat#");

            List<Resource> dataSets = sparQLRunner.execSelect(pss.asQuery(), "s");

            dataSets.forEach(dataSet -> {
                Model model = getGraphOfDataSet(dataSet);
                Resource catalog = getCatalog(dataSet);
                DataSetLongViewDTO dataSetLongViewDTO = modelToDataSetMapper.toDataSetLongViewDTO(model, catalog);
                ret.add(dataSetLongViewDTO);
            });

        } catch (Exception e) {
            logger.error("An error occurred in getting the results", e);
        }
        return ret;
    }

    @Override
    public List<FilterDTO> getFilters(String searchQuery, String[] searchIn) {
        List<FilterDTO> ret = new ArrayList<>();
        ret.add(new FilterDTO()
                .setUri("http://www.w3.org/ns/dcat#theme")
                .setTitle("Theme")
                .setValues(Arrays.asList(
                        new FilterValueDTO("Energy", "Energy", "label", -1),
                        new FilterValueDTO("Environment", "Environment", "label", -1),
                        new FilterValueDTO("http://projeckt-opal.de/theme/mcloud/climateAndWeather", "climate and weather", "label", -1))));
        ret.add(new FilterDTO()
                .setUri("http://www.w3.org/ns/dcat#publisher")
                .setTitle("publisher")
                .setValues(Arrays.asList(
                        new FilterValueDTO("DB", "DB", "label", -1),
                        new FilterValueDTO("others", "others", "label", -1))));
        ret.add(getLicenseFilterValues(searchQuery, searchIn));
        return ret;
    }

    private FilterDTO getLicenseFilterValues(String searchQuery, String[] searchIn) {
        FilterDTO filterDTO = new FilterDTO()
                .setUri("http://purl.org/dc/terms/license")
                .setTitle("License")
                .setValues(new ArrayList<>());

        String filterOptions = getSearchFiltersString(searchQuery, searchIn);
        ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
                "PREFIX dcat: <http://www.w3.org/ns/dcat#> " +
                "PREFIX dct: <http://purl.org/dc/terms/> " +
                "select ?license ?num " +
                "WHERE { " +
                "    {  " +
                "      select ?license (COUNT(?license) AS ?num) " +
                "      WHERE { " +
                "        graph ?g { " +
                "          ?s a dcat:Dataset. " +
                filterOptions +
                "          ?s dcat:distribution ?dist. " +
                "          ?dist dct:license ?license. " +
                "        }  " +
                "      } " +
                "      group by ?license " +
                "   } " +
                "} " +
                "ORDER BY DESC(?num) " +
                "LIMIT 10");

        try {
            List<Pair<Resource, Integer>> pairs =
                    sparQLRunner.execSelectReturnPair(pss.asQuery(), "license", "num");
            pairs.forEach(p -> {
                String uri = p.getKey().getURI();
                filterDTO.getValues().add(new FilterValueDTO(uri, uri, uri, p.getValue()));
            });
        } catch (Exception ex) {
            logger.error("", ex);
        }
        return filterDTO;
    }

    private String getSearchFiltersString(String searchQuery, String[] searchIn) {
        String ret = "";
        if (searchIn == null || searchIn.length == 0)
            return ret;
        if (contains(searchIn, "title"))
            ret += " ?s dct:title ?title. " +
                    " FILTER (CONTAINS(?title, " + searchQuery + " )). ";
        if (contains(searchIn, "description"))
            ret += " ?s dct:description ?description. " +
                    " FILTER (CONTAINS(?description, " + searchQuery + " )). ";
        //todo keywords
        return ret;
    }

    private boolean contains(String[] searchIn, String s) {
        for (String search : searchIn)
            if (search.equals(s))
                return true;
        return false;
    }

    @Override
    public Long getCountOfFilterValue(String filterUri, String valueUri, String searchKey, String searchIn) {
        return 10L;
    }

    @Override
    public DataSetDTO getDataSet(String uri) {
        Model model = getGraphOfDataSet(ResourceFactory.createResource(uri));
        return modelToDataSetMapper.toDataSetDTO(model);
    }

    private String getFiltersString(String searchQuery, ReceivingFilterDTO[] filters) {
        StringBuilder filtersString = new StringBuilder();
        if (searchQuery.length() > 0)
            filtersString.append("?s ?p ?o. +\n" + "FILTER(isLiteral(?o)).  +\n" + "FILTER CONTAINS (STR(?o),\" + searchQuery + \").");
//        if ((filters != null && filters.length > 0)) {
//            for (ReceivingFilterDTO entry : filters) {
//                String key = entry.getUri();
//                String[] values = entry.getValues();
//                if (values.length == 1) {
//                    if (values[0].startsWith("http://"))
//                        filtersString.append(" ?s <").append(key).append("> <").append(values[0]).append("> .");
//                    else
//                        filtersString.append(" ?s <").append(key).append("> \"").append(values[0]).append("\" .");
//                } else if (values.length > 1) {
//                    filtersString.append("VALUES (?value) {  ");
//                    for (String val : values)
//                        if (val.startsWith("http://"))
//                            filtersString.append(" ( <").append(val).append("> )");
//                        else
//                            filtersString.append(" ( \"").append(val).append("\" )");
//                    filtersString.append("} ?s <").append(key).append("> ?value.");
//                }
//            }
//        }
        return filtersString.toString();
    }

    private Model getGraphOfDataSet(Resource dataSet) {
        Model model;
        ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
                "CONSTRUCT { " + "?dataSet ?predicate ?object. " +
                "?object ?p2 ?o2} " +
                "WHERE { " +
                "  GRAPH ?g { " +
                "    ?dataSet ?predicate ?object. " +
                "    OPTIONAL { ?object ?p2 ?o2 } " +
                "  }" +
                "}");

        pss.setParam("dataSet", dataSet);

        model = sparQLRunner.executeConstruct(pss.asQuery());

        return model;
    }

    private Resource getCatalog(Resource dataSet) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
                "SELECT ?catalog " +
                "WHERE { " +
                "  GRAPH ?g { " +
                "    ?catalog dcat:dataset ?dataSet" +
                "  }" +
                "}");

        pss.setParam("dataSet", dataSet);
        pss.setNsPrefix("dcat", "http://www.w3.org/ns/dcat#");

        List<Resource> resources;
        try {
            resources = sparQLRunner.execSelect(pss.asQuery(), "catalog");
        } catch (Exception e) {
            logger.error("An error occurred in getting the catalog {} {}", dataSet, e);
            return null;
        }
        if (resources.size() != 1) {
            logger.error("catalog size is not 1, {}", dataSet);
            return null;
        }
        return resources.get(0);
    }
}
