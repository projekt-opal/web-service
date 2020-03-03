package org.dice_research.opal.webservice.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RangeDTO {
    private String gte;
    private String lte;
}
