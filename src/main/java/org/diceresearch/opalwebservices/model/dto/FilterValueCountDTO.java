package org.diceresearch.opalwebservices.model.dto;

public class FilterValueCountDTO {
    private String filterUri;
    private String valueUri;

    public String getFilterUri() {
        return filterUri;
    }

    public FilterValueCountDTO setFilterUri(String filterUri) {
        this.filterUri = filterUri;
        return this;
    }

    public String getValueUri() {
        return valueUri;
    }

    public FilterValueCountDTO setValueUri(String valueUri) {
        this.valueUri = valueUri;
        return this;
    }
}
