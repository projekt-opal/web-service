package org.dice_research.opal.webservice.model.dto;

public class QualityMeasurementDOT {
    private String quality;
    private Integer value;

    public QualityMeasurementDOT() {
    }

    public QualityMeasurementDOT(String quality, Integer value) {
        this.quality = quality;
        this.value = value;
    }

    public String getQuality() {
        return quality;
    }

    public QualityMeasurementDOT setQuality(String quality) {
        this.quality = quality;
        return this;
    }

    public Integer getValue() {
        return value;
    }

    public QualityMeasurementDOT setValue(Integer value) {
        this.value = value;
        return this;
    }
}
