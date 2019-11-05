package org.diceresearch.opalwebservices.model.dto;

public class DistributionDTO {
    private String url; //download or access url
    private String fileType;

    public DistributionDTO() {
    }

    public DistributionDTO(String url, String fileType) {
        this.url = url;
        this.fileType = fileType;
    }

    public String getUrl() {
        return url;
    }

    public DistributionDTO setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getFileType() {
        return fileType;
    }

    public DistributionDTO setFileType(String fileType) {
        this.fileType = fileType;
        return this;
    }
}
