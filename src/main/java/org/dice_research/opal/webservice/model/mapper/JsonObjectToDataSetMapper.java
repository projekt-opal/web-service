package org.dice_research.opal.webservice.model.mapper;

import lombok.extern.slf4j.Slf4j;
import org.dice_research.opal.webservice.model.dto.DataSetLongViewDTO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class JsonObjectToDataSetMapper {

    public static DataSetLongViewDTO toDataSetLongViewDTO(JSONObject dataSetJsonObject) {
        DataSetLongViewDTO dataSetLongViewDTO = new DataSetLongViewDTO();
        try {
            setUri(dataSetJsonObject, dataSetLongViewDTO);
            setTitle(dataSetJsonObject, dataSetLongViewDTO);
            setDescription(dataSetJsonObject, dataSetLongViewDTO);
            setIssueDate(dataSetJsonObject, dataSetLongViewDTO);
            setThemes(dataSetJsonObject, dataSetLongViewDTO);
            setPublisherName(dataSetJsonObject, dataSetLongViewDTO);
            setLicenses(dataSetJsonObject, dataSetLongViewDTO);
        } catch (Exception e) {
            log.error("Error in ModelToLongViewDTOMapper ", e);
        }
        return dataSetLongViewDTO;
    }

    private static void setLicenses(JSONObject dataSetJsonObject, DataSetLongViewDTO dataSetLongViewDTO) {
        dataSetLongViewDTO.setLicense(new ArrayList<>());
        if(dataSetJsonObject.has("distributions")) {
            JSONArray distributions = dataSetJsonObject.getJSONArray("distributions");
            for (int i = 0; i < distributions.length(); i++) {
                JSONObject distribution = distributions.getJSONObject(i);
                if(distribution.has("license")) {
                    JSONObject licnese = distribution.getJSONObject("license");
                    if(licnese.has("uri"))
                        dataSetLongViewDTO.getLicense().add(licnese.getString("uri"));
                }
            }
        }
    }

    private static void setPublisherName(JSONObject dataSetJsonObject, DataSetLongViewDTO dataSetLongViewDTO) {
        try {
            if(dataSetJsonObject.has("publisher"))
                if(dataSetJsonObject.getJSONObject("publisher").has("name"))
                    dataSetLongViewDTO.setPublisherName(dataSetJsonObject.getJSONObject("publisher").getString("name"));
        } catch (JSONException e) {
            log.error("", e);
        }
    }

    private static void setThemes(JSONObject dataSetJsonObject, DataSetLongViewDTO dataSetLongViewDTO) {
        try {
            dataSetLongViewDTO.setTheme(new ArrayList<>());
            if (dataSetJsonObject.has("themes")) {
                JSONArray themes = dataSetJsonObject.getJSONArray("themes");
                themes.forEach(theme -> dataSetLongViewDTO.getTheme().add(theme.toString()));
            }
        } catch (JSONException e) {
            log.error("", e);
        }
    }

    private static void setIssueDate(JSONObject dataSetJsonObject, DataSetLongViewDTO dataSetLongViewDTO) {
        try {
            if (dataSetJsonObject.has("issued")) dataSetLongViewDTO.setIssueDate(dataSetJsonObject.getString("issued"));
        } catch (JSONException e) {
            log.error("", e);
        }
    }

    private static void setDescription(JSONObject dataSetJsonObject, DataSetLongViewDTO dataSetLongViewDTO) {
        try {
            if (dataSetJsonObject.has("description_de"))
                dataSetLongViewDTO.setDescription(dataSetJsonObject.getString("description_de"));
            if (dataSetJsonObject.has("description"))
                dataSetLongViewDTO.setDescription(dataSetJsonObject.getString("description"));
        } catch (JSONException e) {
            log.error("", e);
        }
    }

    private static void setTitle(JSONObject dataSetJsonObject, DataSetLongViewDTO dataSetLongViewDTO) {
        try {
            if (dataSetJsonObject.has("title_de")) dataSetLongViewDTO.setTitle(dataSetJsonObject.getString("title_de"));
            if (dataSetJsonObject.has("title")) dataSetLongViewDTO.setTitle(dataSetJsonObject.getString("title"));
        } catch (JSONException e) {
            log.error("", e);
        }
    }

    private static void setUri(JSONObject dataSetJsonObject, DataSetLongViewDTO dataSetLongViewDTO) {
        try {
            if (dataSetJsonObject.has("uri")) dataSetLongViewDTO.setUri(dataSetJsonObject.getString("uri"));
        } catch (JSONException e) {
            log.error("", e);
        }
    }

}
