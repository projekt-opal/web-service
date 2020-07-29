package org.dice_research.opal.webservice.model.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FilterDTO {

	@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
	private String filterGroupTitle;
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
	private String searchField;
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
	private Boolean hasExternalLink;
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
	private Boolean hasStaticValues;
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
	private List<ValueDTO> values;
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
	private RangeDTO selectedRangeValues;

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getClass().getSimpleName());

		if (filterGroupTitle != null && !filterGroupTitle.isEmpty()) {
			stringBuilder.append(" filterGroupTitle: ");
			stringBuilder.append(filterGroupTitle);
		}

		if (searchField != null && !searchField.isEmpty()) {
			stringBuilder.append(" searchField: ");
			stringBuilder.append(searchField);
		}

		if (hasExternalLink != null) {
			stringBuilder.append(" hasExternalLink: ");
			stringBuilder.append(hasExternalLink ? "Y" : "N");
		}

		if (hasStaticValues != null) {
			stringBuilder.append(" hasStaticValues: ");
			stringBuilder.append(hasStaticValues ? "Y" : "N");
		}

		if (values != null && !values.isEmpty()) {
			stringBuilder.append(" values: ");
			stringBuilder.append(System.lineSeparator());
			for (ValueDTO valueDTO : values) {
				stringBuilder.append(" ");
				stringBuilder.append(valueDTO);
				stringBuilder.append(System.lineSeparator());
			}
		}

		if (selectedRangeValues != null) {
			stringBuilder.append(" ");
			stringBuilder.append(selectedRangeValues);
		}

		return stringBuilder.toString();
	}

}