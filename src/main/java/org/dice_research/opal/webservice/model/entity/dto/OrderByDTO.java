package org.dice_research.opal.webservice.model.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderByDTO {

	@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
	private String selectedOrderValue;
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
	private double latitude;
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
	private double longitude;

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getClass().getSimpleName());

		if (selectedOrderValue != null && !selectedOrderValue.isEmpty()) {
			stringBuilder.append(" selectedOrderValue: ");
			stringBuilder.append(selectedOrderValue);
		}

		if (latitude != 0) {
			stringBuilder.append(" latitude: ");
			stringBuilder.append(latitude);
		}

		if (longitude != 0) {
			stringBuilder.append(" longitude: ");
			stringBuilder.append(longitude);
		}

		return stringBuilder.toString();
	}
}
