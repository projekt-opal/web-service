package org.diceresearch.opalwebservices.control;

import org.diceresearch.opalwebservices.model.dto.DataSetLongViewDTO;
import org.diceresearch.opalwebservices.model.dto.FilterDTO;
import org.diceresearch.opalwebservices.model.dto.FilterValueDTO;
import org.diceresearch.opalwebservices.model.dto.ReceivingFilterDTO;
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
    @GetMapping("/filters/list")
    public List<FilterDTO> getFilters() {
        return provider.getFilters();
    }

    @CrossOrigin
    @GetMapping("/titles")
    public List<String> getTitles() {
        return Arrays.asList("Title 1", "Title 2");
    }

    @CrossOrigin
    @RequestMapping(value = "/values", method = {RequestMethod.GET, RequestMethod.POST})
    public List<FilterValueDTO> getValues(@RequestParam(name = "title", required = false) String title) {
        logger.info("getValues.title: " + title);
        switch (title) {
            case "Title 1":
                return Arrays.asList(new FilterValueDTO("uri1", "value1", "label1", 20));
            case "Title 2":
                return Arrays.asList(
                        new FilterValueDTO("uri10", "value10", "label10", 20),
                        new FilterValueDTO("uri2", "value2", "label2", 40),
                        new FilterValueDTO("uri3", "value3", "label3", 20)
                        );
        }
        return null;
    }


    // TODO: 10/1/19 An DTO for the RequestBody is needed
    @CrossOrigin
    @PostMapping("/count")
    public String getCount(
            @RequestBody(required = false) String header,
            @RequestBody(required = false) String uri,
            @RequestBody(required = false) String searchKey,
            @RequestBody(required = false) String searchIn
                                  ) {
        logger.info("getCount.header: " + header);
        logger.info("getCount.uri: " + uri);
        logger.info("getCount.searchKey: " + searchKey);
        logger.info("getCount.searchIn: " + searchIn);
        return "10";
    }

    @CrossOrigin
    @GetMapping("/filteredOptions")
    public List<FilterValueDTO> getFilter(@RequestParam(name = "name", required = false) String name) {
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

