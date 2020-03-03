package org.dice_research.opal.webservice.control;

import org.dice_research.opal.webservice.model.dto.*;
import org.dice_research.opal.webservice.services.ElasticSearchProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RestAPIController {

    private static final Logger logger = LoggerFactory.getLogger(RestAPIController.class);

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
    public Long getNumberOFDataSets(
            @RequestParam(name = "uri", required = false, defaultValue = "0") String uri,
            @RequestBody(required = false) SearchDTO searchDTO
    ) {
        return null;
//        return provider.getNumberOfRelatedDataSets(uri, searchDTO.getOrderBy(), searchDTO.getFilters());
    }

    @CrossOrigin
    @PostMapping("/dataSets/getSubList")
    public List<DataSetLongViewDTO> getSubListOfDataSets(
            @RequestParam(name = "searchKey", required = false, defaultValue = "") String searchKey,
            @RequestParam(name = "searchIn", required = false) String[] searchIn,
            @RequestParam(name = "low", required = false, defaultValue = "0") Long low,
            @RequestParam(name = "limit", required = false, defaultValue = "10") Long limit,
            @RequestBody(required = false) SearchDTO searchDTO
    ) {
        return null;
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
    @GetMapping("/filters/list")
    public List<FilterDTO> getFilters(
            @RequestParam(name = "searchKey", required = false, defaultValue = "") String searchKey,
            @RequestParam(name = "searchIn", required = false) String[] searchIn) {
        return null;
    }

    // TODO: 10/1/19 An DTO for the RequestBody is needed
    @CrossOrigin
    @PostMapping("/filter/count")
    public Long getCount(
            @RequestParam(required = false) String searchKey,
            @RequestParam(required = false) String[] searchIn,
            @RequestBody(required = false) FilterValueCountDTO filterValueCountDTO
    ) {
        return null;
    }

    @CrossOrigin
    @GetMapping("/filteredOptions")
    public FilterDTO getFilter(
            @RequestParam(required = false) String filterText,
            @RequestParam(required = false) String searchKey,
            @RequestParam(required = false) String[] searchIn,
            @RequestParam(required = false) String filterType) {
        return null;
    }

}

