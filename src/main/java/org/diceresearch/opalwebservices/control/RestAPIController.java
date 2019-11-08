package org.diceresearch.opalwebservices.control;

import org.diceresearch.opalwebservices.model.dto.*;
import org.diceresearch.opalwebservices.utility.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class RestAPIController {

    private static final Logger logger = LoggerFactory.getLogger(RestAPIController.class);

    private final DataProvider provider;

    @Autowired
    public RestAPIController(DataProvider provider) {
        this.provider = provider;
    }

    @CrossOrigin
    @PostMapping("/dataSets/getNumberOfDataSets")
    public Long getNumberOFDataSets(
            @RequestParam(name = "searchQuery", required = false, defaultValue = "") String searchQuery,
            @RequestParam(name = "searchIn", required = false) String[] searchIn,
            @RequestParam(name = "orderBy", required = false) String orderBy, // TODO: 26.02.19 if quality metrics can be set then we need to have asc, des
            @RequestBody(required = false) ReceivingFilterDTO[] filters
    ) {
        return provider.getNumberOfDatasets(searchQuery, searchIn, orderBy, filters);
    }

    @CrossOrigin
    @PostMapping("/dataSets/getSubList")
    public List<DataSetLongViewDTO> getSubListOFDataSets(
            @RequestParam(name = "searchQuery", required = false, defaultValue = "") String searchQuery,
            @RequestParam(name = "searchIn", required = false) String[] searchIn,
            @RequestParam(name = "orderBy", required = false) String orderBy, // TODO: 26.02.19 if quality metrics can be set then we need to have asc, des
            @RequestParam(name = "low", required = false, defaultValue = "0") Long low,
            @RequestParam(name = "limit", required = false, defaultValue = "10") Long limit,
            @RequestBody(required = false) ReceivingFilterDTO[] filters
    ) {
        return provider.getSubListOFDataSets(searchQuery, low, limit, searchIn, orderBy, filters);
    }

    @CrossOrigin
    @GetMapping("/dataSet")
    public DataSetDTO getDataSet(@RequestParam(name = "uri", required = false) String uri) {
        return provider.getDataSet(uri);
    }

    @CrossOrigin
    @GetMapping("/filters/list")
    public List<FilterDTO> getFilters(
            @RequestParam(name = "searchQuery", required = false, defaultValue = "") String searchQuery,
            @RequestParam(name = "searchIn", required = false) String[] searchIn) {
        return provider.getFilters(searchQuery, searchIn);
    }

    // TODO: 10/1/19 An DTO for the RequestBody is needed
    @CrossOrigin
    @GetMapping("/filter/count")
    public Long getCount(
            @RequestParam(required = false) String filterUri,
            @RequestParam(required = false) String valueUri,
            @RequestParam(required = false) String searchKey,
            @RequestParam(required = false) String[] searchIn
    ) {
        return provider.getCountOfFilterValue(filterUri, valueUri, searchKey, searchIn);
    }

    @CrossOrigin
    @GetMapping("/filteredOptions")
    public List<FilterValueDTO> getFilter(@RequestParam(name = "name", required = false) String name) {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<FilterValueDTO> filterValueDTOS = Arrays.asList(
                new FilterValueDTO("uri1", "value1", "qwertyy", 20),
                new FilterValueDTO("uri2", "value2", "asdfg", 40),
                new FilterValueDTO("uri3", "value3", "label3", 20)
        );
        List<FilterValueDTO> collect = filterValueDTOS
                .stream()
                .filter(x -> x.getLabel().contains(name)).collect(Collectors.toList());
        logger.info(Arrays.toString(collect.toArray(new FilterValueDTO[0])));
        return collect;
    }

}

