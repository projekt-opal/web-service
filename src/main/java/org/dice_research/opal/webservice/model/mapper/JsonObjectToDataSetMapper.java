package org.dice_research.opal.webservice.model.mapper;

import lombok.extern.slf4j.Slf4j;
import org.dice_research.opal.webservice.model.entity.dto.DataSetDTO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class JsonObjectToDataSetMapper {


    public static DataSetDTO toDataSetLongViewDTO(JSONObject dataSetJsonObject) {
        DataSetDTO dataSetDTO = new DataSetDTO();
        try {
            setUri(dataSetJsonObject, dataSetDTO);
            setTitle(dataSetJsonObject, dataSetDTO);
            setDescription(dataSetJsonObject, dataSetDTO);
            setIssueDate(dataSetJsonObject, dataSetDTO);
            setThemes(dataSetJsonObject, dataSetDTO);
            setPublisherName(dataSetJsonObject, dataSetDTO);
            setLicenses(dataSetJsonObject, dataSetDTO);
        } catch (Exception e) {
            log.error("Error in ModelToLongViewDTOMapper ", e);
        }
        return dataSetDTO;
    }

    private static void setLicenses(JSONObject dataSetJsonObject, DataSetDTO dataSetDTO) {
        Set<String> licenses = new HashSet<>();
        if (dataSetJsonObject.has("distributions")) {
            JSONArray distributions = dataSetJsonObject.getJSONArray("distributions");
            for (int i = 0; i < distributions.length(); i++) {
                JSONObject distribution = distributions.getJSONObject(i);
                if (distribution.has("license") && distribution.get("license") instanceof JSONObject) {
                    log.info(distribution.toString());
                    JSONObject licnese = distribution.getJSONObject("license");
                    if (licnese.has("uri"))
                        licenses.add(licnese.getString("uri"));
                }
            }
        }
        dataSetDTO.setLicense(new ArrayList<>(licenses));
    }

    private static void setPublisherName(JSONObject dataSetJsonObject, DataSetDTO dataSetDTO) {
        try {
            if (dataSetJsonObject.has("publisher"))
                if (dataSetJsonObject.getJSONObject("publisher").has("name"))
                    dataSetDTO.setPublisherName(dataSetJsonObject.getJSONObject("publisher").getString("name"));
        } catch (JSONException e) {
            log.error("", e);
        }
    }

    private static void setThemes(JSONObject dataSetJsonObject, DataSetDTO dataSetDTO) {
        try {
            dataSetDTO.setTheme(new ArrayList<>());
            if (dataSetJsonObject.has("themes")) {
                JSONArray themes = dataSetJsonObject.getJSONArray("themes");
                themes.forEach(theme -> dataSetDTO.getTheme().add(theme.toString()));
            }
        } catch (JSONException e) {
            log.error("", e);
        }
    }

    private static void setIssueDate(JSONObject dataSetJsonObject, DataSetDTO dataSetDTO) {
        try {
            if (dataSetJsonObject.has("issued")) dataSetDTO.setIssueDate(dataSetJsonObject.getString("issued"));
        } catch (JSONException e) {
            log.error("", e);
        }
    }

    private static void setDescription(JSONObject dataSetJsonObject, DataSetDTO dataSetDTO) {
        try {
            if (dataSetJsonObject.has("description_de"))
                dataSetDTO.setDescription(dataSetJsonObject.getString("description_de"));
            if (dataSetJsonObject.has("description"))
                dataSetDTO.setDescription(dataSetJsonObject.getString("description"));
        } catch (JSONException e) {
            log.error("", e);
        }
    }

    private static void setTitle(JSONObject dataSetJsonObject, DataSetDTO dataSetDTO) {
        try {
            if (dataSetJsonObject.has("title_de")) dataSetDTO.setTitle(dataSetJsonObject.getString("title_de"));
            if (dataSetJsonObject.has("title")) dataSetDTO.setTitle(dataSetJsonObject.getString("title"));
        } catch (JSONException e) {
            log.error("", e);
        }
    }

    private static void setUri(JSONObject dataSetJsonObject, DataSetDTO dataSetDTO) {
        try {
            if (dataSetJsonObject.has("uri")) dataSetDTO.setUri(dataSetJsonObject.getString("uri"));
        } catch (JSONException e) {
            log.error("", e);
        }
    }

}
