package org.dice_research.opal.webservice.utility;

import org.dice_research.opal.webservice.model.dto.DataSetDTO;
import org.dice_research.opal.webservice.model.dto.DataSetLongViewDTO;
import org.dice_research.opal.webservice.model.dto.FilterDTO;
import org.dice_research.opal.webservice.model.dto.OrderByDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface DataProvider {

    long getNumberOfDataSets(String searchKey, String[] searchIn, OrderByDTO orderBy, FilterDTO[] filters);

    List<DataSetLongViewDTO> getSubListOfDataSets(String searchKey, Long low, Long limit, String[] searchIn, OrderByDTO orderBy, FilterDTO[] filters);

    List<FilterDTO> getFilters(String searchKey, String[] searchIn);

    Long getCountOfFilterValue(String filterUri, String valueUri, String searchKey, String[] searchIn);

    DataSetDTO getDataSet(String uri);

    FilterDTO getTopFilterOptions(String filterType, String searchKey, String[] searchIn, String filterText);

    List<DataSetLongViewDTO> getSubRelatedListOfDataSets(String uri, Long low, Long limit, OrderByDTO orderByDTO, FilterDTO[] filterDTOS);

    Long getNumberOfRelatedDataSets(String uri, OrderByDTO orderByDTO, FilterDTO[] filterDTOS);
}
