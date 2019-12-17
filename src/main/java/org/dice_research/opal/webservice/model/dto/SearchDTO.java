package org.dice_research.opal.webservice.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

public class SearchDTO {
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private OrderByDTO orderByDTO;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
    private FilterDTO[] filterDTOS;

    public SearchDTO() {
    }

    public SearchDTO(OrderByDTO orderByDTO, FilterDTO[] filterDTOS) {
        this.orderByDTO = orderByDTO;
        this.filterDTOS = filterDTOS;
    }

    public OrderByDTO getOrderByDTO() {
        return orderByDTO;
    }

    public void setOrderByDTO(OrderByDTO orderByDTO) {
        this.orderByDTO = orderByDTO;
    }

    public FilterDTO[] getFilterDTOS() {
        return filterDTOS;
    }

    public void setFilterDTOS(FilterDTO[] filterDTOS) {
        this.filterDTOS = filterDTOS;
    }
}
