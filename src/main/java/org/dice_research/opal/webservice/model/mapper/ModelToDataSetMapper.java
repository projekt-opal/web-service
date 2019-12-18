package org.dice_research.opal.webservice.model.mapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.dice_research.opal.webservice.model.dto.DataSetDTO;
import org.dice_research.opal.webservice.model.dto.DataSetLongViewDTO;
import org.dice_research.opal.webservice.model.dto.DistributionDTO;
import org.dice_research.opal.webservice.model.dto.QualityMeasurementDTO;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class ModelToDataSetMapper {

    public DataSetLongViewDTO toDataSetLongViewDTO(Model model) {
        try {
            DataSetLongViewDTO dataSetLongViewDTO = new DataSetLongViewDTO();
            dataSetLongViewDTO.setUri(getUri(model));
            dataSetLongViewDTO.setTitle(getTitle(model));
            dataSetLongViewDTO.setDescription(getDescription(model));
            dataSetLongViewDTO.setTheme(getThemeList(model));
            dataSetLongViewDTO.setIssueDate(getIssueDate(model));
            dataSetLongViewDTO.setLicense(getLicenseList(model));
            return dataSetLongViewDTO;
        } catch (Exception e) {
            log.error("Error in ModelToLongViewDTOMapper ", e);
        }
        return null;
    }

    private List<String> getLicenseList(Model model) {
        Set<String> ret = new HashSet<>(); //to remove duplicate licenses in the view
        try {
            NodeIterator resIterator = model.listObjectsOfProperty(DCAT.distribution);
            while (resIterator.hasNext()) {
                try {
                    Resource dist = resIterator.nextNode().asResource();
                    NodeIterator nodeIterator = model.listObjectsOfProperty(dist, DCTerms.license);
                    if (nodeIterator.hasNext()) {
                        String license = nodeIterator.nextNode().toString();
                        ret.add(license);
                    }
                } catch (NoSuchElementException e) {
                    log.error("", e);
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return new ArrayList<>(ret);
    }

    public DataSetDTO toDataSetDTO(Model model) {
        try {
            String uri = getUri(model);
            String title = getTitle(model);
            String description = getDescription(model);
            List<String> theme = getThemeList(model);
            String issueDate = "2018-12-05";
            Random r = new Random();
            String overAllRating = Double.toString(r.nextDouble() * 4 + 1);
            List<String> keywords = Arrays.asList("key1", "key2");
            DataSetDTO dataSetDTO = new DataSetDTO();
            dataSetDTO.setUri(uri == null ? title : uri);
            dataSetDTO.setTitle(title);
            dataSetDTO.setDescription(description);
            dataSetDTO.setTheme(theme);
            dataSetDTO.setIssueDate(issueDate);
            dataSetDTO.setKeywords(keywords);
            dataSetDTO.setOverallRating(overAllRating);
//            dataSetDTO.setDistributionDTOS(Arrays.asList(new DistributionDTO("uri1", "pdf")));
//            dataSetDTO.setQualityMeasurementDOS(Arrays.asList(new QualityMeasurementDTO("q1", 5)));
            return dataSetDTO;
        } catch (Exception e) {
            log.error("Error in ModelToLongViewDTOMapper ", e);
        }
        return null;
    }

    private String getIssueDate(Model model) {
        try {
            NodeIterator resIterator = model.listObjectsOfProperty(DCTerms.issued);
            if (resIterator.hasNext()) return resIterator.nextNode().asLiteral().getString();
        } catch (NoSuchElementException e) {
            log.error("", e);
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

    private List<String> getThemeList(Model model) {
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
