package de.upb.cs.dice.opalwebservices.model.dto;

import java.io.Serializable;

public class FilterValueDTO implements Serializable {
    private static final long serialVersionUID = -3429602967985658169L;
    private String value;
    private Integer count;

    public FilterValueDTO() {
    }

    public FilterValueDTO(String value, Integer count) {
        this.value = value;
        this.count = count;
    }

    public String getValue() {
        return value;
    }

    public FilterValueDTO setValue(String value) {
        this.value = value;
        return this;
    }

    public Integer getCount() {
        return count;
    }

    public FilterValueDTO setCount(Integer count) {
        this.count = count;
        return this;
    }
}
