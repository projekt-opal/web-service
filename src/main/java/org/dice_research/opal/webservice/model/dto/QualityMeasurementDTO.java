package org.dice_research.opal.webservice.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QualityMeasurementDTO {
    private String quality;
    private Integer value;
}
