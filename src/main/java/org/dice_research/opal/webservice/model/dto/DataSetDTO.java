package org.dice_research.opal.webservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class DataSetDTO {
    private String uri;
    private String title;
    private String description;
    private String issueDate;
    private List<String> theme;
    private List<String> keywords;
//    private String overallRating;
    @JsonProperty("publisher")
    private PublisherDTO publisherDTO;
    @JsonProperty("distributions")
    private List<DistributionDTO> distributionDTOS;
    @JsonProperty("qualityMetrics")
    private List<QualityMeasurementDTO> qualityMeasurementDOS;
}
