package org.dice_research.opal.webservice.model.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValueDTO {
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
	private String value;
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
	private CounterDTO count;
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
	private Boolean selected = false;

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getClass().getSimpleName());

		if (value != null && !value.isEmpty()) {
			stringBuilder.append(" searchKey: ");
			stringBuilder.append(value);
		}

		if (count != null) {
			stringBuilder.append(" ");
			stringBuilder.append(count);
		}

		if (value != null) {
			stringBuilder.append(" selected: ");
			stringBuilder.append(selected ? "Y" : "N");
		}

		return stringBuilder.toString();
	}
}