package org.diceresearch.opalwebservices.utility.elasticsearch;

import org.diceresearch.opalwebservices.model.dto.DataSetDTO;
import org.diceresearch.opalwebservices.model.dto.DataSetLongViewDTO;
import org.diceresearch.opalwebservices.model.dto.FilterDTO;
import org.diceresearch.opalwebservices.model.dto.ReceivingFilterDTO;
import org.diceresearch.opalwebservices.utility.DataProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Profile("elasticsearch")
@Component
public class ElasticSearchProvider implements DataProvider {
    @Override
    public long getNumberOfDatasets(String searchQuery, String[] searchIn, String orderBy, ReceivingFilterDTO[] filters) {
        return 0; //Todo complete it
    }

    @Override
    public List<DataSetLongViewDTO> getSubListOFDataSets(String searchQuery, Long low, Long limit, String[] searchIn, String orderBy, ReceivingFilterDTO[] filters) {
        return null; //Todo complete it
    }

    @Override
    public List<FilterDTO> getFilters(String searchQuery, String[] searchIn) {
        return null; //Todo complete it
    }

    @Override
    public Long getCountOfFilterValue(String filterUri, String valueUri, String searchKey, String[] searchIn) {
        return null;
    }

    @Override
    public DataSetDTO getDataSet(String uri) {
        return null;
    }
}
