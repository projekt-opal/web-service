package org.dice_research.opal.webservice.model.entity.dto;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class SearchDTO {

	@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
	private String searchKey;
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
	private String[] searchIn;
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
	private OrderByDTO orderBy;
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
	private FilterDTO[] filters;

	public SearchDTO() {
		searchIn = new String[0];
		filters = new FilterDTO[0];
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(getClass().getSimpleName());

		if (searchKey != null && !searchKey.isEmpty()) {
			stringBuilder.append(" searchKey: ");
			stringBuilder.append(searchKey);
		}

		if (searchIn != null && searchIn.length != 0) {
			stringBuilder.append(" searchIn: ");
			stringBuilder.append(Arrays.toString(searchIn));
		}

		if (orderBy != null) {
			stringBuilder.append(" ");
			stringBuilder.append(orderBy);
		}

		if (filters != null && filters.length != 0) {
			stringBuilder.append(" filters: ");
			stringBuilder.append(Arrays.toString(filters));
		}

		return stringBuilder.toString();
	}
}
