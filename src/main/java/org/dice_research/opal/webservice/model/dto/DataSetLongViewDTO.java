package org.dice_research.opal.webservice.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class DataSetLongViewDTO {
    private String uri;
    private String title;
    private String description;
    private String issueDate;
    private List<String> theme;
    private String publisherName;
    private List<String> license;
    // TODO: 12/18/19 Add overall rating
}
