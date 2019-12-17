package org.dice_research.opal.webservice.model.mapper;

import org.dice_research.opal.webservice.model.dto.DataSetLongViewDTO;
import org.dice_research.opal.webservice.model.dto.PublisherDTO;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mapstruct.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class JsonObjecttoDataSetMapper {
    private static final Logger logger = LoggerFactory.getLogger(JsonObjecttoDataSetMapper.class);

    public DataSetLongViewDTO toDataSetLongViewDTO(JSONObject dataSet) {
        try {

            String uri = getPropertyValue(dataSet, "URL");
            String description = getPropertyValue(dataSet, "description");
            String title = getPropertyValue(dataSet, "title");
            List<String> themes = getPropertyArrayValues(dataSet, "themes", "theme");
            String landingPage = getPropertyValue(dataSet, "landingPage");
            String language = getPropertyValue(dataSet, "language");
            String license = getPropertyValue(dataSet, "license");
            List<String> keywords = getPropertyArrayValues(dataSet, "keywords", "keyword");
            String issued = getPropertyValue(dataSet, "issued");
            String modified = getPropertyValue(dataSet, "modified");
            String accrualPeriodicity = getPropertyValue(dataSet, "accrualPeriodicity");
            PublisherDTO pulisherInfo = getPublisherInfo(dataSet);
            DataSetLongViewDTO dataSetLongViewDTO = new DataSetLongViewDTO()
                    .setKeywords(keywords)
                    .setTheme(themes)
                    .setTitle(title)
                    .setDescription(description)
                    .setIssueDate(issued)
                    .setUri(uri)
                    .setPublisherDTO(pulisherInfo);
            return dataSetLongViewDTO;

        } catch (Exception e) {
            logger.error("Error in ModelToLongViewDTOMapper ", e);
        }
        return null;
    }

    private PublisherDTO getPublisherInfo(JSONObject dataSet) {

        PublisherDTO publisherDTO = new PublisherDTO();
        if (dataSet.has("publisherInfo")) {
            JSONArray propertyArray = dataSet.getJSONArray("publisherInfo");
            for (int i = 0; i < propertyArray.length(); i++) {
                JSONObject property = propertyArray.getJSONObject(i);
                if(property.has("name"))
                    publisherDTO.setName(property.getString("name"));
                publisherDTO.setUri(property.getString("publisher"));
            }
        }
        return publisherDTO;
    }

    protected List<String> getPropertyArrayValues(JSONObject dataSet, String parent, String child) {
        List<String> propertyValues = new ArrayList<String>();
        if (dataSet.has(parent)) {
            JSONArray themesArray = dataSet.getJSONArray(parent);
            for (int i = 0; i < themesArray.length(); i++) {
                propertyValues.add(themesArray.getJSONObject(i).getString(child));
            }
        }
        return propertyValues;
    }

    protected String getPropertyValue(JSONObject dataSet, String fieldName) {
        String propertyValue = null;
        if (dataSet.has(fieldName)) {
            propertyValue = dataSet.getString(fieldName);
        }

        return propertyValue;
    }
}
