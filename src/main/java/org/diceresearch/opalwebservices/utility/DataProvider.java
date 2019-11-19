package org.diceresearch.opalwebservices.utility;

import org.diceresearch.opalwebservices.model.dto.DataSetDTO;
import org.diceresearch.opalwebservices.model.dto.DataSetLongViewDTO;
import org.diceresearch.opalwebservices.model.dto.FilterDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface DataProvider {

    long getNumberOfDatasets(String searchKey, String[] searchIn, String orderBy, FilterDTO[] filters);

    List<DataSetLongViewDTO> getSubListOFDataSets(String searchKey, Long low, Long limit, String[] searchIn, String orderBy, FilterDTO[] filters);

    List<FilterDTO> getFilters(String searchKey, String[] searchIn);

    Long getCountOfFilterValue(String filterUri, String valueUri, String searchKey, String[] searchIn);

    DataSetDTO getDataSet(String uri);

    FilterDTO getTopFilterOptions(String filterType, String searchKey, String[] searchIn, String filterText);
}
