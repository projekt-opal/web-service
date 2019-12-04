package org.diceresearch.opalwebservices.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;

public class FilterDTO {

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String uri;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private String title;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private List<FilterValueDTO> values;

    public FilterDTO() {
    }

    public FilterDTO(String uri, String title, List<FilterValueDTO> values) {
        this.uri = uri;
        this.title = title;
        this.values = values;
    }

    public String getTitle() {
        return title;
    }

    public FilterDTO setTitle(String title) {
        this.title = title;
        return this;
    }

    public List<FilterValueDTO> getValues() {
        return values;
    }

    public FilterDTO setValues(List<FilterValueDTO> values) {
        this.values = values;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public FilterDTO setUri(String uri) {
        this.uri = uri;
        return this;
    }
}
