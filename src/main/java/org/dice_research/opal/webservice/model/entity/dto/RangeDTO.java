package org.dice_research.opal.webservice.model.entity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RangeDTO {
    private String gte = "-1";
    private String lte = "-1";
}
