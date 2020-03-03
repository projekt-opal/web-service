package org.dice_research.opal.webservice.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CounterDTO {
    private long absolute;
    private long relative;
}
