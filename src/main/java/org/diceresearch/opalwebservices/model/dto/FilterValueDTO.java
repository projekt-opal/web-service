package org.diceresearch.opalwebservices.model.dto;

public class FilterValueDTO {
    private String uri;
    private String value;
    private String label;
    private Integer count;

    public FilterValueDTO() {
    }

    public FilterValueDTO(String uri, String value, String label, Integer count) {
        this.uri = uri;
        this.value = value;
        this.label = label;
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

    public String getUri() {
        return uri;
    }

    public FilterValueDTO setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public FilterValueDTO setLabel(String label) {
        this.label = label;
        return this;
    }

    @Override
    public String toString() {
        return "FilterValueDTO{" +
                "uri='" + uri + '\'' +
                ", value='" + value + '\'' +
                ", label='" + label + '\'' +
                ", count=" + count +
                '}';
    }
}
