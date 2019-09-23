package org.diceresearch.opalwebservices.utility;

import org.diceresearch.opalwebservices.model.dto.DataSetLongViewDTO;
import org.diceresearch.opalwebservices.model.dto.FilterDTO;
import org.diceresearch.opalwebservices.model.dto.ReceivingFilterDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface DataProvider {

    long getNumberOfDatasets(String searchQuery, String[] searchIn, String orderBy, ReceivingFilterDTO[] filters);

    List<DataSetLongViewDTO> getSubListOFDataSets(String searchQuery, Long low, Long limit, String[] searchIn, String orderBy, ReceivingFilterDTO[] filters);

    List<FilterDTO> getFilters();
}
