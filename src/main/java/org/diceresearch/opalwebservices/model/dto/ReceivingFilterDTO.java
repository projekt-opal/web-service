package org.diceresearch.opalwebservices.model.dto;

import java.util.List;

public class ReceivingFilterDTO {
    private String title;
    private String uri;
    private List<FilterValueDTO> values;

    public ReceivingFilterDTO() {
    }

    public ReceivingFilterDTO(String title, String uri, List<FilterValueDTO> values) {
        this.title = title;
        this.uri = uri;
        this.values = values;
    }

    public String getTitle() {
        return title;
    }

    public ReceivingFilterDTO setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public ReceivingFilterDTO setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public List<FilterValueDTO> getValues() {
        return values;
    }

    public ReceivingFilterDTO setValues(List<FilterValueDTO> values) {
        this.values = values;
        return this;
    }
}
