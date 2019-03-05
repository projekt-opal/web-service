package de.upb.cs.dice.opalwebservices.control;

import de.upb.cs.dice.opalwebservices.model.dto.DataSetLongViewDTO;
import de.upb.cs.dice.opalwebservices.model.dto.FilterDTO;
import de.upb.cs.dice.opalwebservices.model.dto.FilterValueDTO;
import de.upb.cs.dice.opalwebservices.model.mapper.ModelToLongViewDTOMapper;
import de.upb.cs.dice.opalwebservices.utility.SparQLRunner;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class RestAPIController {

    private static final Logger logger = LoggerFactory.getLogger(RestAPIController.class);

    private final SparQLRunner sparQLRunner;
    private final ModelToLongViewDTOMapper modelToLongViewDTOMapper;


    @Autowired
    public RestAPIController(SparQLRunner sparQLRunner, ModelToLongViewDTOMapper modelToLongViewDTOMapper) {
        this.sparQLRunner = sparQLRunner;
        this.modelToLongViewDTOMapper = modelToLongViewDTOMapper;
    }

    @CrossOrigin
    @GetMapping("/dataSets/getNumberOfDataSets")
    public Long getNumberOFDataSets(
            @RequestParam(name = "searchQuery", required = false, defaultValue = "") String searchQuery,
            @RequestParam(name = "searchIn", required = false) String[] searchIn,
            @RequestParam(name = "orderBy", required = false) String orderBy, // TODO: 26.02.19 if quality metrics can be set then we need to have asc, des
            @RequestParam(name = "searchFilters", required = false) Map<String, String> filters
    ) {

        Long num = -1L;
        try {
            ParameterizedSparqlString pss = new ParameterizedSparqlString();

            String query = "SELECT (COUNT(DISTINCT ?s) AS ?num) WHERE { " +
                    "?s a dcat:Dataset. " +
                    "?s ?p ?o. " +
                    "FILTER(isLiteral(?o)). " +
                    "FILTER CONTAINS (STR(?o),\"" + searchQuery + "\"). " +
                    "}";

            pss.setCommandText(query);
            pss.setNsPrefix("dcat", "http://www.w3.org/ns/dcat#");

            num = sparQLRunner.execSelectCount(pss.asQuery());
        } catch (Exception e) {
            logger.error("An error occurred in getting the results", e);
        }
        return num;
    }
    @CrossOrigin
    @GetMapping("/dataSets/getSubList")
    public List<DataSetLongViewDTO> getSubListOFDataSets(
            @RequestParam(name = "searchQuery", required = false, defaultValue = "") String searchQuery,
            @RequestParam(name = "searchIn", required = false) String[] searchIn,
            @RequestParam(name = "orderBy", required = false) String orderBy, // TODO: 26.02.19 if quality metrics can be set then we need to have asc, des
            @RequestParam(name = "searchFilters", required = false) Map<String, String> filters,
            @RequestParam(name = "low", required = false, defaultValue = "0") Long low,
            @RequestParam(name = "limit", required = false, defaultValue = "100") Long limit

    ) {
        List<DataSetLongViewDTO> ret = new ArrayList<>();
        try {
            ParameterizedSparqlString pss = new ParameterizedSparqlString();

            String query = "SELECT DISTINCT ?s WHERE { GRAPH ?g { " +
                    "?s a dcat:Dataset. " +
                    "?s ?p ?o. " +
                    "FILTER(isLiteral(?o)). " +
                    "FILTER CONTAINS (STR(?o),\"" + searchQuery + "\"). " +
                    "} } OFFSET " + low + " LIMIT " + limit;

            pss.setCommandText(query);
            pss.setNsPrefix("dcat", "http://www.w3.org/ns/dcat#");

            List<Resource> dataSets = sparQLRunner.execSelect(pss.asQuery(), "s");

            dataSets.forEach(dataSet -> {
                Model model = getGraphOfDataSet(dataSet);
                Resource catalog = getCatalog(dataSet);
                DataSetLongViewDTO dataSetLongViewDTO = modelToLongViewDTOMapper.toDataSetLongViewDTO(model, catalog);
                ret.add(dataSetLongViewDTO);
            });

        } catch (Exception e) {
            logger.error("An error occurred in getting the results", e);
        }
        return ret;
    }

    @CrossOrigin
    @GetMapping("/filters/list")
    public List<FilterDTO> getFilters() {
        List<FilterDTO> ret = new ArrayList<>();
        ret.add(new FilterDTO()
                .setTitle("Theme")
                .setValues(Arrays.asList(
                        new FilterValueDTO("Energy", 10),
                        new FilterValueDTO("Environment", 143))));
        ret.add(new FilterDTO()
                .setTitle("publisher")
                .setValues(Arrays.asList(
                        new FilterValueDTO("DB", 10),
                        new FilterValueDTO("others", 143))));
        ret.add(new FilterDTO()
                .setTitle("license")
                .setValues(Arrays.asList(
                        new FilterValueDTO("CCv4.0", 10),
                        new FilterValueDTO("others", 143))));
        return ret;
    }

    private Model getGraphOfDataSet(Resource dataSet) {
        Model model;


        ParameterizedSparqlString pss = new ParameterizedSparqlString("" +
                "CONSTRUCT { " + "?dataSet ?predicate ?object . " +
                "\t?object ?p2 ?o2} " +
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
            logger.error("An error occurred in getting the catalog", dataSet, e);
            return null;
        }
        if(resources.size() != 1) {
            logger.error("catalog size is not 1, ", dataSet);
            return null;
        }
        return resources.get(0);
    }

}
