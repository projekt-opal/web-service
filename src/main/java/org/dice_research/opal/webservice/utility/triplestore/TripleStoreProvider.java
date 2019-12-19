package org.dice_research.opal.webservice.utility.triplestore;

import org.aksw.commons.util.Pair;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DCAT;
import org.dice_research.opal.webservice.model.dto.*;
import org.dice_research.opal.webservice.model.mapper.ModelToDataSetMapper;
import org.dice_research.opal.webservice.utility.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Profile(value = {"triplestore", "test-triplestore", "default"})
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
    public long getNumberOfDataSets(String searchKey, String[] searchIn, OrderByDTO orderBy, FilterDTO[] filters) {
        Long num = -1L;
        try {
            ParameterizedSparqlString pss = new ParameterizedSparqlString();

            boolean containsLicenseFilter = isContainsLicenseFilter(filters);

            String query;
            if (containsLicenseFilter) {
                String filtersStringS = getSparQLSearchQuery(searchKey, searchIn, filters, "?s");
                String filtersStringDist1 = getSparQLSearchQuery(searchKey, searchIn, filters, "?dist1");

                query = "SELECT (COUNT(DISTINCT ?s) AS ?num) WHERE { " +
                        "GRAPH ?g { " +
                        "{" +
                        "SELECT ?s WHERE { " +
                        "?s a dcat:Dataset. " +
                        "?s dcat:distribution ?dist1. " +
                        filtersStringDist1 +
                        " } " +
                        "} UNION { " +
                        "SELECT ?s WHERE { " +
                        "?s a dcat:Dataset. " +
                        filtersStringS +
                        " } " +
                        "} " +
                        "} " +
                        "}";
            } else {
                String filtersString = getSparQLSearchQuery(searchKey, searchIn, filters, "?s");

                query = "SELECT (COUNT(DISTINCT ?s) AS ?num) WHERE { " +
                        " GRAPH ?g { " +
                        "?s a dcat:Dataset. " + filtersString +
                        " } " +
                        "}";
            }
            pss.setCommandText(query);
            pss.setNsPrefix("dcat", "http://www.w3.org/ns/dcat#");
            pss.setNsPrefix("dct", "http://purl.org/dc/terms/");

            logger.info(pss.toString());
            num = sparQLRunner.execSelectCount(pss.asQuery());
        } catch (Exception e) {
            logger.error("An error occurred in getting the results", e);
        }
        return num;
    }

    @Override
    public List<DataSetLongViewDTO> getSubListOfDataSets(String searchKey, Long low, Long limit, String[] searchIn, OrderByDTO orderBy, FilterDTO[] filters) {
        List<DataSetLongViewDTO> ret = new ArrayList<>();
        try {
            ParameterizedSparqlString pss = new ParameterizedSparqlString();
            boolean containsLicenseFilter = isContainsLicenseFilter(filters);

            String query;
            String filtersStringS = getSparQLSearchQuery(searchKey, searchIn, filters, "?s");
            if (containsLicenseFilter) {
                String filtersStringDist1 = getSparQLSearchQuery(searchKey, searchIn, filters, "?dist1");
                query = "SELECT DISTINCT ?s WHERE { " +
                        "GRAPH ?g { " +
                        "{" +
                        "SELECT ?s WHERE { " +
                        "?s a dcat:Dataset. " +
                        "?s dcat:distribution ?dist1. " +
                        filtersStringDist1 +
                        "} " +
                        "} UNION { " +
                        "SELECT ?s WHERE { " +
                        "?s a dcat:Dataset. " +
                        filtersStringS +
                        "} " +
                        "} " +
                        "} " +
                        "}";
            } else {
                query = "SELECT DISTINCT ?s WHERE { " +
                        " GRAPH ?g { " +
                        "?s a dcat:Dataset. " + filtersStringS +
                        " } " +
                        " } OFFSET " + low + " LIMIT " + limit;
            }

            pss.setCommandText(query);
            pss.setNsPrefix("dcat", "http://www.w3.org/ns/dcat#");
            pss.setNsPrefix("dct", "http://purl.org/dc/terms/");

            logger.info(pss.toString());
            List<Resource> dataSets = sparQLRunner.execSelect(pss.asQuery(), "s");

            dataSets.forEach(dataSet -> {
                Model model = getGraphOfDataSet(dataSet);
                DataSetLongViewDTO dataSetLongViewDTO = modelToDataSetMapper.toDataSetLongViewDTO(model);
                ret.add(dataSetLongViewDTO);
            });

        } catch (Exception e) {
            logger.error("An error occurred in getting the results", e);
        }
        return ret;
    }

    @Override
    public List<FilterDTO> getFilters(String searchKey, String[] searchIn) {
        List<FilterDTO> ret = new ArrayList<>();
        ret.add(getThemeValues());
        FilterDTO publishers = getPublisherValues(searchKey, searchIn, null);
        if (publishers.getValues().size() > 0)
            ret.add(publishers);
        FilterDTO licenses = getLicenseFilterValues(searchKey, searchIn, null);
        if (licenses.getValues().size() > 0)
            ret.add(licenses);
        return ret;
    }

    private boolean isContainsLicenseFilter(FilterDTO[] filters) {
        boolean containsLicenseFilter = false;
        for (FilterDTO filterDTO : filters)
            if (filterDTO.getTitle().toLowerCase().equals("license")) {
                containsLicenseFilter = true;
                break;
            }
        return containsLicenseFilter;
    }

    private FilterDTO getThemeValues() {

        return FilterDTO.builder()
                .uri(DCAT.theme.getURI())
                .title("Theme")
                .externalLink(true)
                .isTypeStatic(true)
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

    private FilterDTO getPublisherValues(String searchQuery, String[] searchIn, String filterText) {
        FilterDTO filterDTO = FilterDTO.builder()
                .uri("http://purl.org/dc/terms/publisher")
                .title("Publisher")
                .externalLink(true)
                .isTypeStatic(false)
                .values(new ArrayList<>()).build();

        String filterOptions = getSparQLSearchQuery(searchQuery, searchIn, null, "?s");
        ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
                "PREFIX dcat: <http://www.w3.org/ns/dcat#> " +
                "PREFIX dct: <http://purl.org/dc/terms/> " +
                "select ?publisher ?num " +
                "from <http://projekt-opal.de> " +
                "WHERE { " +
                " { " +
                " select ?publisher (COUNT(?publisher) AS ?num) " +
                " WHERE { " +
                " ?s a dcat:Dataset. " +
                filterOptions +
                " ?s dct:publisher ?publisher. " +
                (filterText != null ? "FILTER(CONTAINS(STR(?publisher), \"" + filterText + "\"))" : "") +
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

    private FilterDTO getLicenseFilterValues(String searchKey, String[] searchIn, String filterText) {
        FilterDTO filterDTO = FilterDTO.builder()
                .uri("http://purl.org/dc/terms/license")
                .title("License")
                .externalLink(true)
                .isTypeStatic(false)
                .values(new ArrayList<>()).build();

        String filterOptions = getSparQLSearchQuery(searchKey, searchIn, null, "?s");
        ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
                "PREFIX dcat: <http://www.w3.org/ns/dcat#> " +
                "PREFIX dct: <http://purl.org/dc/terms/> " +
                "select ?license (COUNT(?license) AS ?num) " +
                "from <http://projekt-opal.de> " +
                "WHERE { " +
                "{ " +
                "select distinct ?license ?s " +
                "WHERE { " +
                "?s a dcat:Dataset. " +
                filterOptions +
                "?s dcat:distribution ?dist. " +
                "?dist dct:license ?license. " +
                (filterText != null ? "FILTER(CONTAINS(STR(?license), \"" + filterText + "\"))" : "") +
                "}  group by ?license ?s " +
                "} " +
                "UNION " +
                "{ " +
                "select distinct ?license ?s " +
                "WHERE " +
                "{ " +
                "?s a dcat:Dataset. " +
                filterOptions +
                "?s dct:license ?license. " +
                (filterText != null ? "FILTER(CONTAINS(STR(?license), \"" + filterText + "\"))" : "") +
                "}  group by ?license ?s " +
                "} " +
                "} " +
                "group by ?license " +
                "ORDER BY DESC(?num) " +
                "LIMIT 10");

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

    @Override
    public Long getCountOfFilterValue(String filterUri, String valueUri, String searchKey, String[] searchIn) {
        String filterOptions = getSparQLSearchQuery(searchKey, searchIn, null, "?s");
        ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
                "PREFIX dcat: <http://www.w3.org/ns/dcat#> " +
                "PREFIX dct: <http://purl.org/dc/terms/> " +
                "select (COUNT(?s) AS ?num) " +
                "from <http://projekt-opal.de> " +
                "WHERE { " +
                "?s a dcat:Dataset. " +
                filterOptions +
                "FILTER(EXISTS{?s dcat:theme ?theme.}) " +
                "}");
        pss.setParam("?theme", ResourceFactory.createResource(valueUri));

        try {
            logger.info(pss.toString());
            return sparQLRunner.execSelectCount(pss.asQuery());
        } catch (Exception ex) {
            logger.error("", ex);
        }
        return -1L;
    }

    @Override
    public DataSetDTO getDataSet(String uri) {
        Model model = getGraphOfDataSet(ResourceFactory.createResource(uri));
        return modelToDataSetMapper.toDataSetDTO(model);
    }

    @Override
    public FilterDTO getTopFilterOptions(String filterType, String searchKey, String[] searchIn, String filterText) {
        switch (filterType.toLowerCase()) {
            case "license":
                return getLicenseFilterValues(searchKey, searchIn, filterText);
            case "publisher":
                return getPublisherValues(searchKey, searchIn, filterText);
        }
        return null;
    }

    @Override
    public List<DataSetLongViewDTO> getSubRelatedListOfDataSets(String uri, Long low, Long limit, OrderByDTO orderByDTO, FilterDTO[] filterDTOS) {
        return getSubListOfDataSets("", low, limit, new String[0], orderByDTO, filterDTOS);// TODO: 12/19/19 implement the function
    }

    @Override
    public Long getNumberOfRelatedDataSets(String uri, OrderByDTO orderByDTO, FilterDTO[] filterDTOS) {
        return getNumberOfDataSets("", new String[0], orderByDTO, filterDTOS); // TODO: 12/19/19 implement the function
    }

    private String getSparQLSearchQuery(String searchKey, String[] searchIn, FilterDTO[] filters, String resourceName) {
        StringBuilder filtersString = new StringBuilder();
        int cnt = 0;
        if (searchKey.length() > 0) {
            if (searchIn == null || searchIn.length == 0)
                filtersString
                        .append(resourceName + " ?p ?o. ")
                        .append("FILTER(isLiteral(?o)).  ")
                        .append("FILTER CONTAINS (STR(?o),\"")
                        .append(searchKey)
                        .append("\"). ");
            else {
                ArrayList<String> filterValues = new ArrayList<>();
                if (Arrays.asList(searchIn).contains("title")) {
                    filtersString
                            .append(resourceName + " dct:title ?title. ");
                    filterValues.add("CONTAINS (STR(?title),\"" + searchKey + "\") ");
                }
                if (Arrays.asList(searchIn).contains("description")) {
                    filtersString
                            .append(resourceName + " dct:description ?description. ");
                    filterValues.add("CONTAINS (STR(?description),\"" + searchKey + "\") ");
                }
                if (Arrays.asList(searchIn).contains("keyword")) {
                    filtersString
                            .append(resourceName + " dcat:keyword ?keyword. ");
                    filterValues.add("CONTAINS (STR(?keyword),\"" + searchKey + "\") ");
                }
                filtersString.append("FILTER ( ").append(filterValues.get(0));
                for (int i = 1; i < filterValues.size(); i++)
                    filtersString.append(" || ").append(filterValues.get(i));
                filtersString.append("). ");
            }
        }
        if ((filters != null && filters.length > 0)) {
            for (FilterDTO filterDTO : filters) {
                String key = filterDTO.getUri();
                List<FilterValueDTO> values = filterDTO.getValues();
                if (values.size() == 1) {
                    if (values.get(0).getUri().matches("^[a-zA-Z0-9]+:\\/\\/.*")) {//if it is a url
                        filtersString.append("FILTER(EXISTS{" +
                                (filterDTO.getTitle().toLowerCase().equals("license") ? resourceName : "?s") +
                                " <").append(key).append("> <").append(values.get(0).getUri()).append(">}). ");
                    } else {
                        filtersString.append("FILTER(EXISTS{" +
                                (filterDTO.getTitle().toLowerCase().equals("license") ? resourceName : "?s") +
                                " <").append(key).append("> \"").append(values.get(0).getUri()).append("\" }). ");
                    }
                } else if (values.size() > 1) {
                    String multiValue = "?value" + cnt++;
                    filtersString.append("VALUES (" + multiValue + ") { ");
                    for (FilterValueDTO val : values)
                        if (val.getUri().matches("^[a-zA-Z0-9]+:\\/\\/.*"))//if it is a url
                            filtersString.append(" ( <").append(val.getUri()).append("> ) ");
                        else
                            filtersString.append(" ( \"").append(val.getUri()).append("\" ) ");
                    filtersString.append("} " +
                            (filterDTO.getTitle().toLowerCase().equals("license") ? resourceName : "?s") +
                            " <").append(key).append("> " + multiValue + ". ");
                }
            }
        }
        return filtersString.toString();
    }

    private Model getGraphOfDataSet(Resource dataSet) {
        Model model;
        ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
                "CONSTRUCT { " + "?dataSet ?predicate ?object. " +
                "?object ?p2 ?o2} " +
                "WHERE { " +
                " GRAPH ?g { " +
                " ?dataSet ?predicate ?object. " +
                " OPTIONAL { ?object ?p2 ?o2 } " +
                " } " +
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
