package org.dice_research.opal.webservice.model.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CounterDTO {
    private long absolute;
    private long relative;
}
