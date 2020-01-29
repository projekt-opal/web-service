package org.dice_research.opal.webservice.model.mapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.dice_research.opal.common.vocabulary.Dqv;
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
            DataSetDTO dataSetDTO = new DataSetDTO();
            dataSetDTO.setUri(getUri(model) == null ? getTitle(model) : getUri(model));
            dataSetDTO.setTitle(getTitle(model));
            dataSetDTO.setDescription(getDescription(model));
            dataSetDTO.setTheme(getThemeList(model));
            dataSetDTO.setIssueDate(getIssueDate(model));
            dataSetDTO.setKeywords(getKeywords(model));
//            dataSetDTO.setOverallRating(overAllRating);
            dataSetDTO.setDistributionDTOS(getDistributions(model));
            dataSetDTO.setQualityMeasurementDOS(getMeasurements(model));
            return dataSetDTO;
        } catch (Exception e) {
            log.error("Error in ModelToLongViewDTOMapper ", e);
        }
        return null;
    }

    private List<QualityMeasurementDTO> getMeasurements(Model model) {
        List<QualityMeasurementDTO> ret = new ArrayList<>();
        try {
            NodeIterator nodeIterator = model.listObjectsOfProperty(Dqv.HAS_QUALITY_MEASUREMENT);
            while (nodeIterator.hasNext()) {
                try {
                    List<QualityMeasurementDTO> list = new ArrayList<>();
                    Resource measurement = nodeIterator.nextNode().asResource();
                    NodeIterator measurementIterator = model.listObjectsOfProperty(measurement, Dqv.IS_MEASUREMENT_OF);
                    while (measurementIterator.hasNext()) {
                        Resource measurementOf = measurementIterator.nextNode().asResource();
                        list.add(QualityMeasurementDTO.builder().quality(measurementOf.getURI()).value(0).build());
                    }
                    NodeIterator measurementValueIterator = model.listObjectsOfProperty(measurement, Dqv.HAS_VALUE);
                    int i = 0;
                    while (measurementValueIterator.hasNext()) {
                        int v = measurementValueIterator.nextNode().asLiteral().getInt();
                        if (i < list.size()) list.get(i++).setValue(v);
                    }
                    ret.addAll(list);
                } catch (NoSuchElementException e) {
                    log.error("", e);
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return ret;
    }

    private List<String> getKeywords(Model model) {
        Set<String> ret = new HashSet<>();
        try {
            NodeIterator nodeIterator = model.listObjectsOfProperty(DCAT.keyword);
            while (nodeIterator.hasNext()) {
                try {
                    ret.add(nodeIterator.nextNode().asLiteral().getString());
                } catch (NoSuchElementException e) {
                    log.error("", e);
                }
            }
        } catch (NoSuchElementException e) {
            log.error("", e);
        }
        return new ArrayList<>(ret);
    }

    private List<DistributionDTO> getDistributions(Model model) {
        List<DistributionDTO> ret = new ArrayList<>();
        try {
            NodeIterator nodeIterator = model.listObjectsOfProperty(DCAT.distribution);
            while (nodeIterator.hasNext()) {
                try {
                    Resource dist = nodeIterator.nextNode().asResource();
                    NodeIterator downloadUrlIterator = model.listObjectsOfProperty(dist, DCAT.downloadURL);
                    if (downloadUrlIterator.hasNext()) {
                        String s = downloadUrlIterator.nextNode().toString();
                        DistributionDTO distributionDTO = new DistributionDTO();
                        distributionDTO.setUrl(s);
                        if (s.matches(".*\\..+")) { // TODO: 1/13/20 User dct:format
                            String[] split = s.split("\\.");
                            String fileType = split[split.length - 1];
                            distributionDTO.setFileType(fileType);
                        }
                        ret.add(distributionDTO);
                    } else {
                        NodeIterator accessIterator = model.listObjectsOfProperty(dist, DCAT.accessURL);
                        if (accessIterator.hasNext()) {
                            String s = accessIterator.nextNode().toString();
                            DistributionDTO distributionDTO = new DistributionDTO();
                            distributionDTO.setUrl(s);
                            ret.add(distributionDTO);
                        }
                    }
                } catch (NoSuchElementException e) {
                    log.error("", e);
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return ret;
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
