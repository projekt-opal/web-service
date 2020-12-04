package org.dice_research.opal.webservice.model.entity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangesDTO {
	
	private String uri;
	private boolean hasChanges;
	private String[] removedTriples;
	private String[] addedTriples;
}
