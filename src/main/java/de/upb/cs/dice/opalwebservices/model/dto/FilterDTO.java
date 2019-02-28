package de.upb.cs.dice.opalwebservices.model.dto;

import java.io.Serializable;
import java.util.List;

public class FilterDTO implements Serializable {
    private static final long serialVersionUID = -7109984421794445640L;
    private String title;
    private List<FilterValueDTO> values;

    public FilterDTO() {
    }

    public FilterDTO(String title, List<FilterValueDTO> values) {
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
}
