package org.diceresearch.opalwebservices.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DataSetDTO {
    private String uri;
    private String title;
    private String description;
    private String issueDate;
    private String theme;
    private List<String> keywords;
    private String overallRating;
    private PublisherDTO publisherDTO;
    private List<DistributionDTO> distributionDTOS;
    private List<QualityMessurementDTO> qualityMessurementDTOS;

    public DataSetDTO() {
    }

    public String getUri() {
        return uri;
    }

    public DataSetDTO setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public DataSetDTO setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DataSetDTO setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public DataSetDTO setIssueDate(String issueDate) {
        this.issueDate = issueDate;
        return this;
    }

    public String getTheme() {
        return theme;
    }

    public DataSetDTO setTheme(String theme) {
        this.theme = theme;
        return this;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public DataSetDTO setKeywords(List<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    public String getOverallRating() {
        return overallRating;
    }

    public DataSetDTO setOverallRating(String overallRating) {
        this.overallRating = overallRating;
        return this;
    }

    @JsonProperty("publisher")
    public PublisherDTO getPublisherDTO() {
        return publisherDTO;
    }

    public DataSetDTO setPublisherDTO(PublisherDTO publisherDTO) {
        this.publisherDTO = publisherDTO;
        return this;
    }

    @JsonProperty("distributions")
    public List<DistributionDTO> getDistributionDTOS() {
        return distributionDTOS;
    }

    public DataSetDTO setDistributionDTOS(List<DistributionDTO> distributionDTOS) {
        this.distributionDTOS = distributionDTOS;
        return this;
    }

    @JsonProperty("qualityMetrics")
    public List<QualityMessurementDTO> getQualityMessurementDTOS() {
        return qualityMessurementDTOS;
    }

    public DataSetDTO setQualityMessurementDTOS(List<QualityMessurementDTO> qualityMessurementDTOS) {
        this.qualityMessurementDTOS = qualityMessurementDTOS;
        return this;
    }
}
