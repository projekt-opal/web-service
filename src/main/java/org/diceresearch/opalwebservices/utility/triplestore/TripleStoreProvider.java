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
        ret.add(getThemeValues());
        ret.add(getPublisherValues(searchQuery, searchIn));
        ret.add(getLicenseFilterValues(searchQuery, searchIn));
        return ret;
    }

    private FilterDTO getThemeValues() {
        return new FilterDTO()
                .setUri("http://www.w3.org/ns/dcat#theme")
                .setTitle("Theme")
                .setValues(Arrays.asList(
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
                ));

    }

    private FilterDTO getPublisherValues(String searchQuery, String[] searchIn) {
        FilterDTO filterDTO = new FilterDTO()
                .setUri("http://purl.org/dc/terms/publisher")
                .setTitle("Publisher")
                .setValues(new ArrayList<>());

        String filterOptions = getSearchFiltersString(searchQuery, searchIn);
        ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
                "PREFIX dcat: <http://www.w3.org/ns/dcat#> " +
                "PREFIX dct: <http://purl.org/dc/terms/> " +
                "select ?publisher ?num " +
                "from <http://projekt-opal.de> " +
                "WHERE { " +
                " {  " +
                " select ?publisher (COUNT(?publisher) AS ?num) " +
                " WHERE { " +
                " ?s a dcat:Dataset. " +
                filterOptions +
                " ?s dct:publisher ?publisher. " +
                " } " +
                " group by ?publisher " +
                " } " +
                "} " +
                "ORDER BY DESC(?num) " +
                "LIMIT 10 ");

        try {
            List<Pair<Resource, Integer>> pairs =
                    sparQLRunner.execSelectReturnPair(pss.asQuery(), "publisher", "num");
            pairs.forEach(p -> {
                String uri = p.getKey().getURI();
                filterDTO.getValues().add(new FilterValueDTO(uri, uri, uri, p.getValue()));
            });
        } catch (Exception ex) {
            logger.error("", ex);
        }
        return filterDTO;
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
                "from <http://projekt-opal.de> " +
                "WHERE { " +
                " { " +
                " select ?license (COUNT(?license) AS ?num) " +
                " WHERE { " +
                " ?s a dcat:Dataset. " +
                filterOptions +
                " ?s dcat:distribution ?dist. " +
                " ?dist dct:license ?license. " +
                " } " +
                " group by ?license " +
                " } " +
                " } " +
                "ORDER BY DESC(?num) " +
                "LIMIT 10 ");

        try {
            logger.info(pss.toString());
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
    public Long getCountOfFilterValue(String filterUri, String valueUri, String searchKey, String[] searchIn) {
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
