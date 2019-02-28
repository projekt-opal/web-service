package de.upb.cs.dice.opalwebservices.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder(toBuilder = false)
@NoArgsConstructor
@AllArgsConstructor
public class DataSetLongViewDTO implements Serializable {
    private static final long serialVersionUID = 7992064242653372582L;
    private String title;
    private String description;
    private String issueDate;
    private String theme;
    private List<String> keywords;
    private String fileType;
    private String overallRating;
}
