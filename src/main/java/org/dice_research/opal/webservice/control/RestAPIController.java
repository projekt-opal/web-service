package org.dice_research.opal.webservice.control;

import org.dice_research.opal.webservice.model.dto.DataSetDTO;
import org.dice_research.opal.webservice.model.dto.DataSetLongViewDTO;
import org.dice_research.opal.webservice.model.dto.FilterDTO;
import org.dice_research.opal.webservice.model.dto.SearchDTO;
import org.dice_research.opal.webservice.services.ElasticSearchProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RestAPIController {

    private final ElasticSearchProvider provider;

    @Autowired
    public RestAPIController(ElasticSearchProvider provider) {
        this.provider = provider;
    }

    @CrossOrigin
    @PostMapping("/dataSets/getNumberOfDataSets")
    public Long getNumberOFDataSets(@RequestBody() SearchDTO searchDTO) {
        return provider.getNumberOfDataSets(searchDTO);
    }

    @CrossOrigin
    @PostMapping("/dataSets/getNumberOfRelatedDataSets")
    public Long getNumberOFRelatedDataSets(
            @RequestParam(name = "uri", required = false, defaultValue = "0") String uri,
            @RequestBody(required = false) SearchDTO searchDTO
    ) {
        return null;
//        return provider.getNumberOfRelatedDataSets(uri, searchDTO.getOrderBy(), searchDTO.getFilters());
    }

    @CrossOrigin
    @PostMapping("/dataSets/getSubList")
    public List<DataSetLongViewDTO> getSubListOfDataSets(
            @RequestParam(name = "low", required = false, defaultValue = "0") Integer low,
            @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit,
            @RequestBody(required = false) SearchDTO searchDTO
    ) {
        return provider.getSublistOfDataSets(searchDTO, low, limit);
    }

    @CrossOrigin
    @PostMapping("/dataSets/getRelatedSubList")
    public List<DataSetLongViewDTO> getSubListOfRelatedDataSets(
            @RequestParam(name = "uri", required = false, defaultValue = "0") String uri,
            @RequestParam(name = "low", required = false, defaultValue = "0") Long low,
            @RequestParam(name = "limit", required = false, defaultValue = "10") Long limit,
            @RequestBody(required = false) SearchDTO searchDTO
    ) {
        return null;
    }

    @CrossOrigin
    @GetMapping("/dataSet")
    public DataSetDTO getDataSet(@RequestParam(name = "uri", required = false) String uri) {
        return null;
    }

    @CrossOrigin
    @PostMapping("/filters/list")
    public List<FilterDTO> getFilters(
            @RequestBody SearchDTO searchDTO
    ) {
        return provider.getFilters(searchDTO);
    }

    @CrossOrigin
    @PostMapping("/filteredOptions")
    public FilterDTO getFilter(
            @RequestBody(required = false) SearchDTO searchDTO,
            @RequestParam(required = false) String filterGroupTitle,
            @RequestParam(required = false) String containsText
    ) {
        return provider.getTopFiltersThatContain(searchDTO, filterGroupTitle, containsText);
    }

}

