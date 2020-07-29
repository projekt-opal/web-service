package org.dice_research.opal.webservice.model.entity.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RangeDTO {
	private String gte = "-1";
	private String lte = "-1";

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getClass().getSimpleName());

		if (gte != null && !gte.isEmpty()) {
			stringBuilder.append(" gte: ");
			stringBuilder.append(gte);
		}

		if (lte != null && !lte.isEmpty()) {
			stringBuilder.append(", lte: ");
			stringBuilder.append(lte);
		}

		return stringBuilder.toString();
	}
}