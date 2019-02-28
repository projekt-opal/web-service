package de.upb.cs.dice.opalwebservices.model.dto;

import java.io.Serializable;
import java.util.List;

public class DataSetLongViewDTO implements Serializable {
    private static final long serialVersionUID = 7992064242653372582L;
    private String title;
    private String description;
    private String issueDate;
    private String theme;
    private List<String> keywords;
    private String fileType;
    private String overallRating;

    public DataSetLongViewDTO() {
    }

    public DataSetLongViewDTO(String title, String description, String issueDate, String theme, List<String> keywords, String fileType, String overallRating) {
        this.title = title;
        this.description = description;
        this.issueDate = issueDate;
        this.theme = theme;
        this.keywords = keywords;
        this.fileType = fileType;
        this.overallRating = overallRating;
    }

    public String getTitle() {
        return title;
    }

    public DataSetLongViewDTO setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DataSetLongViewDTO setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public DataSetLongViewDTO setIssueDate(String issueDate) {
        this.issueDate = issueDate;
        return this;
    }

    public String getTheme() {
        return theme;
    }

    public DataSetLongViewDTO setTheme(String theme) {
        this.theme = theme;
        return this;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public DataSetLongViewDTO setKeywords(List<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    public String getFileType() {
        return fileType;
    }

    public DataSetLongViewDTO setFileType(String fileType) {
        this.fileType = fileType;
        return this;
    }

    public String getOverallRating() {
        return overallRating;
    }

    public DataSetLongViewDTO setOverallRating(String overallRating) {
        this.overallRating = overallRating;
        return this;
    }

}
