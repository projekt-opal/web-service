package org.diceresearch.opalwebservices.model.dto;

public class PublisherDTO {
    private String name;
    private String uri;

    public PublisherDTO() {
    }

    public PublisherDTO(String name, String uri) {
        this.name = name;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public PublisherDTO setName(String name) {
        this.name = name;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public PublisherDTO setUri(String uri) {
        this.uri = uri;
        return this;
    }
}
