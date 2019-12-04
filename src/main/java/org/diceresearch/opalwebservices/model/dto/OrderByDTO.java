package org.diceresearch.opalwebservices.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

public class OrderByDTO {

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String selectedOrderValue;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String latitude;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String longitude;

    public OrderByDTO() {
    }

    public OrderByDTO(String selectedOrderValue, String latitude, String longitude) {
        this.selectedOrderValue = selectedOrderValue;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getSelectedOrderValue() {
        return selectedOrderValue;
    }

    public void setSelectedOrderValue(String selectedOrderValue) {
        this.selectedOrderValue = selectedOrderValue;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
