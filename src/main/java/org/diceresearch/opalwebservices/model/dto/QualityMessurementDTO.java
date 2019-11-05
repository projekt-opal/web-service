package org.diceresearch.opalwebservices.model.dto;

public class QualityMessurementDTO {
    private String quality;
    private Integer value;

    public QualityMessurementDTO() {
    }

    public QualityMessurementDTO(String quality, Integer value) {
        this.quality = quality;
        this.value = value;
    }

    public String getQuality() {
        return quality;
    }

    public QualityMessurementDTO setQuality(String quality) {
        this.quality = quality;
        return this;
    }

    public Integer getValue() {
        return value;
    }

    public QualityMessurementDTO setValue(Integer value) {
        this.value = value;
        return this;
    }
}
