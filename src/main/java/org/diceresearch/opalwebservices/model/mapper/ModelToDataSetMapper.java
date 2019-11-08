package org.diceresearch.opalwebservices.model.mapper;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.diceresearch.opalwebservices.model.dto.*;
import org.mapstruct.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Mapper(componentModel = "spring")
public abstract class ModelToDataSetMapper {
    private static final Logger logger = LoggerFactory.getLogger(ModelToDataSetMapper.class);

    public DataSetLongViewDTO toDataSetLongViewDTO(Model model, Resource catalog) {
        try {
            String uri = getUri(model);
            String title = getTitle(model);
            String description = getDescription(model);
            List<String> theme = getTheme(model);
            String fileType = "PDF";
            String issueDate = "2018-12-05";
            Random r = new Random();
            String overAllRating = Double.toString(r.nextDouble() * 4 + 1);
            List<String> keywords = Arrays.asList("key1", "key2");
            DataSetLongViewDTO dataSetLongViewDTO = new DataSetLongViewDTO()
                    .setUri(uri == null ? title : uri)
                    .setTitle(title)
                    .setDescription(description)
                    .setTheme(theme)
                    .setIssueDate(issueDate)
                    .setKeywords(keywords)
                    .setFileType(fileType)
                    .setOverallRating(overAllRating)
                    .setCatalog(catalog.getURI())
                    .setPublisherDTO(new PublisherDTO("publisher name", "publisher uri"));
            return dataSetLongViewDTO;
        } catch (Exception e) {
            logger.error("Error in ModelToLongViewDTOMapper ", e);
        }
        return null;
    }

    public DataSetDTO toDataSetDTO(Model model) {
        try {
            String uri = getUri(model);
            String title = getTitle(model);
            String description = getDescription(model);
            List<String> theme = getTheme(model);
            String issueDate = "2018-12-05";
            Random r = new Random();
            String overAllRating = Double.toString(r.nextDouble() * 4 + 1);
            List<String> keywords = Arrays.asList("key1", "key2");
            DataSetDTO dataSetDTO = new DataSetDTO()
                    .setUri(uri == null ? title : uri)
                    .setTitle(title)
                    .setDescription(description)
                    .setTheme(theme)
                    .setIssueDate(issueDate)
                    .setKeywords(keywords)
                    .setOverallRating(overAllRating)
                    .setPublisherDTO(new PublisherDTO("publisher name", "publisher uri"))
                    .setDistributionDTOS(Arrays.asList(new DistributionDTO("uri1", "pdf")))
                    .setQualityMeasurementDOS(Arrays.asList(new QualityMeasurementDOT("q1", 5)));
            return dataSetDTO;
        } catch (Exception e) {
            logger.error("Error in ModelToLongViewDTOMapper ", e);
        }
        return null;
    }

    private String getUri(Model model) {
        ResIterator resIterator = model.listResourcesWithProperty(RDF.type, DCAT.Dataset);
        if (resIterator.hasNext()) return resIterator.nextResource().getURI();
        return null;
    }

    private String getTitle(Model model) {
        NodeIterator iterator = model.listObjectsOfProperty(DCTerms.title);
        if (!iterator.hasNext()) return "";// TODO: 27.02.19 What exactly we should return?
        RDFNode rdfNode = iterator.nextNode();//must exist
        return rdfNode.asLiteral().getString();
    }

    private String getDescription(Model model) {
        NodeIterator iterator = model.listObjectsOfProperty(DCTerms.description);
        if (!iterator.hasNext()) return "";// TODO: 27.02.19 What exactly we should return?
        RDFNode rdfNode = iterator.nextNode();//must exist
        return rdfNode.asLiteral().getString();
    }

    private List<String> getTheme(Model model) {
        ArrayList<String> ret = new ArrayList<>();
        NodeIterator iterator = model.listObjectsOfProperty(DCAT.theme);
        while (iterator.hasNext()) {
            RDFNode rdfNode = iterator.nextNode();//must exist
            if (rdfNode.isLiteral()) ret.add(rdfNode.asLiteral().getString());
            if (rdfNode.isURIResource()) {
                NodeIterator nodeIterator = model.listObjectsOfProperty(rdfNode.asResource(),
                        ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel"));
                if (nodeIterator.hasNext()) ret.add(nodeIterator.nextNode().asLiteral().getString());
                else {
                    nodeIterator = model.listObjectsOfProperty(rdfNode.asResource(),
                            ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel", "en"));
                    if (nodeIterator.hasNext()) ret.add(nodeIterator.nextNode().asLiteral().getString());
                    else {
                        nodeIterator = model.listObjectsOfProperty(rdfNode.asResource(),
                                ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel", "de"));
                        if (nodeIterator.hasNext()) ret.add(nodeIterator.nextNode().asLiteral().getString());
                        else ret.add(rdfNode.asResource().getURI());
                    }
                }
            }
        }
        return ret;
    }
}
