package de.upb.cs.dice.opalwebservices.model.mapper;

import de.upb.cs.dice.opalwebservices.model.dto.DataSetLongViewDTO;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDFS;
import org.mapstruct.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ModelToLongViewDTOMapper {
    private static final Logger logger = LoggerFactory.getLogger(ModelToLongViewDTOMapper.class);

    public DataSetLongViewDTO toDataSetLongViewDTO(Model model) {
        try {
            String title = getTitle(model);
            String description = getDescription(model);
            String theme = getTheme(model);
            String fileType = "PDF";
            String issueDate = "2018-12-05";
            String overAllRating = "3";
            List<String> keywords = Arrays.asList("key1", "key2");
            DataSetLongViewDTO dataSetLongViewDTO = DataSetLongViewDTO.builder()
                    .title(title)
                    .description(description)
                    .theme(theme)
                    .issueDate(issueDate)
                    .keywords(keywords)
                    .fileType(fileType)
                    .overallRating(overAllRating)
                    .build();
            return dataSetLongViewDTO;
        } catch (Exception e) {
            logger.error("Error in ModelToLongViewDTOMapper ", e);
        }
        return null;
    }

    private String getTitle(Model model) {
        NodeIterator iterator = model.listObjectsOfProperty(DCTerms.title);
        if(!iterator.hasNext()) return "";// TODO: 27.02.19 What exactly we should return?
        RDFNode rdfNode = iterator.nextNode();//must exist
        return rdfNode.asLiteral().getString();
    }

    private String getDescription(Model model) {
        NodeIterator iterator = model.listObjectsOfProperty(DCTerms.description);
        if(!iterator.hasNext()) return "";// TODO: 27.02.19 What exactly we should return?
        RDFNode rdfNode = iterator.nextNode();//must exist
        return rdfNode.asLiteral().getString();
    }

    private String getTheme(Model model) {
        NodeIterator iterator = model.listObjectsOfProperty(DCAT.theme);
        if(!iterator.hasNext()) return "";// TODO: 27.02.19 What exactly we should return?
        RDFNode rdfNode = iterator.nextNode();//must exist
        if(rdfNode.isLiteral()) return rdfNode.asLiteral().getString();
        if(rdfNode.asResource().getURI().startsWith("http://projeckt-opal.de/theme/mcloud")) {
            NodeIterator nodeIterator = model.listObjectsOfProperty(rdfNode.asResource(), RDFS.label);
            if(nodeIterator.hasNext()) nodeIterator.nextNode().asLiteral().getString();
            //else go to next line return URI
        }
        return rdfNode.asResource().getURI();
    }
}
