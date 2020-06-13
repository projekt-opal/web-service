package org.dice_research.opal.webservice.control;

import com.google.gson.Gson;
import org.dice_research.opal.webservice.model.entity.DataSet;
import org.dice_research.opal.webservice.model.entity.dto.*;
import org.dice_research.opal.webservice.services.ElasticSearchProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(RestAPIController.class)
public class RestAPIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ElasticSearchProvider elasticSearchProvider;

    @Test
    public void getNumberOFDataSets() throws Exception {
        long n = 10L;
        when(elasticSearchProvider.getNumberOfDataSets(any(SearchDTO.class))).thenReturn(n);

        SearchDTO searchDTO = new SearchDTO();
        RequestBuilder request = MockMvcRequestBuilders
                .post("/dataSets/getNumberOfDataSets")
                .content(new Gson().toJson(searchDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string(Long.toString(n)))
                .andReturn();

    }

    @Test
    void getNumberOFRelatedDataSets() throws Exception {
        long n = 3L;
        when(elasticSearchProvider.getNumberOfRelatedDataSets(any(SearchDTO.class), anyString())).thenReturn(n);

        SearchDTO searchDTO = new SearchDTO();
        RequestBuilder request = MockMvcRequestBuilders
                .post("/dataSets/getNumberOfRelatedDataSets")
                .param("uri", "http://projekt-opal.de/test_uri")
                .content(new Gson().toJson(searchDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string(Long.toString(n)))
                .andReturn();
    }

    @Test
    void getSubListOfDataSets() throws Exception {
        List<DataSetDTO> dataSetDTOs = Arrays.asList(
                DataSetDTO.builder().uri("http://projekt-opal.de/dataset/test_dataset_uri_1").title("sample_title_1").build(),
                DataSetDTO.builder().uri("http://projekt-opal.de/dataset/test_dataset_uri_2").title("sample_title_2")
                        .description("sample_description_2").build()
        );
        String jsonContent = new Gson().toJson(dataSetDTOs);

        when(elasticSearchProvider.getSublistOfDataSets(any(SearchDTO.class), anyInt(), anyInt()))
                .thenReturn(dataSetDTOs);

        SearchDTO searchDTO = new SearchDTO();
        RequestBuilder request = MockMvcRequestBuilders
                .post("/dataSets/getSubList")
                .param("low", "0")
                .param("limit", "10")
                .content(new Gson().toJson(searchDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(jsonContent, false))
                .andReturn();
    }

    @Test
    void getSubListOfRelatedDataSets() throws Exception {
        List<DataSetDTO> dataSetDTOs = Arrays.asList(
                DataSetDTO.builder().uri("http://projekt-opal.de/dataset/test_dataset_uri_1").title("sample_title_1").build(),
                DataSetDTO.builder().uri("http://projekt-opal.de/dataset/test_dataset_uri_2").title("sample_title_2")
                        .description("sample_description_2").build()
        );
        String jsonContent = new Gson().toJson(dataSetDTOs);

        when(elasticSearchProvider.getSubListOfRelatedDataSets(any(SearchDTO.class), anyString(), anyInt(), anyInt()))
                .thenReturn(dataSetDTOs);

        SearchDTO searchDTO = new SearchDTO();
        RequestBuilder request = MockMvcRequestBuilders
                .post("/dataSets/getRelatedSubList")
                .param("uri", "http://projekt-opal.de/test_uri")
                .param("low", "0")
                .param("limit", "10")
                .content(new Gson().toJson(searchDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(jsonContent, false))
                .andReturn();
    }

    @Test
    void getFiltersForRelatedDataSets() throws Exception {
        List<FilterDTO> filterDTOList = Arrays.asList(
                FilterDTO.builder().filterGroupTitle("Theme").searchField("themes").hasExternalLink(true).hasStaticValues(true)
                        .values(Arrays.asList(
                                new ValueDTO("Economy and finance", new CounterDTO(787, 64), false),
                                new ValueDTO("Agriculture, fisheries, forestry and food", new CounterDTO(1322, 45), false)
                        )).build(),
                FilterDTO.builder().filterGroupTitle("License").searchField("distributions.license.uri.keyword").hasExternalLink(true).hasStaticValues(false)
                        .values(Arrays.asList(
                                new ValueDTO("http://reference.data.gov.uk/id/open-government-licence", new CounterDTO(530, 68), false),
                                new ValueDTO("http://dcat-ap.de/def/licenses/dl-by-de/2.0", new CounterDTO(500, 100), false)
                        )).build()
        );
        String jsonContent = new Gson().toJson(filterDTOList);

        when(elasticSearchProvider.getFilters(any(SearchDTO.class), anyString()))
                .thenReturn(filterDTOList);

        SearchDTO searchDTO = new SearchDTO();
        RequestBuilder request = MockMvcRequestBuilders
                .post("/filters/listFoRelated")
                .param("uri", "http://projekt-opal.de/test_uri")
                .content(new Gson().toJson(searchDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(jsonContent, false))
                .andReturn();
    }

    @Test
    void getDataSet() throws Exception {
        DataSet dataSet = DataSet.builder().uri("http://projekt-opal.de/test_uri").title("some title")
                .description("some description").keywords(Arrays.asList("keywords1", "keywords2")).build();

        String jsonContent = new Gson().toJson(dataSet);

        when(elasticSearchProvider.getDataSet(anyString()))
                .thenReturn(dataSet);

        RequestBuilder request = MockMvcRequestBuilders
                .get("/dataSet")
                .param("uri", "http://projekt-opal.de/test_uri")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(jsonContent, false))
                .andReturn();
    }

    @Test
    void getFilters() throws Exception {
        List<FilterDTO> filterDTOList = Arrays.asList(
                FilterDTO.builder().filterGroupTitle("Theme").searchField("themes").hasExternalLink(true).hasStaticValues(true)
                        .values(Arrays.asList(
                                new ValueDTO("Economy and finance", new CounterDTO(787, 787), false),
                                new ValueDTO("Agriculture, fisheries, forestry and food", new CounterDTO(1322, 1322), false)
                        )).build(),
                FilterDTO.builder().filterGroupTitle("License").searchField("distributions.license.uri.keyword").hasExternalLink(true).hasStaticValues(false)
                        .values(Arrays.asList(
                                new ValueDTO("http://reference.data.gov.uk/id/open-government-licence", new CounterDTO(530, 530), false),
                                new ValueDTO("http://dcat-ap.de/def/licenses/dl-by-de/2.0", new CounterDTO(500, 500), false)
                        )).build()
        );
        String jsonContent = new Gson().toJson(filterDTOList);

        when(elasticSearchProvider.getFilters(any(SearchDTO.class), isNull()))
                .thenReturn(filterDTOList);

        SearchDTO searchDTO = new SearchDTO();
        RequestBuilder request = MockMvcRequestBuilders
                .post("/filters/list")
                .content(new Gson().toJson(searchDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(jsonContent, false))
                .andReturn();

    }

    @Test
    void getFilter() throws Exception {

        FilterDTO filterDTO = FilterDTO.builder().filterGroupTitle("License").searchField("distributions.license.uri.keyword")
                .hasExternalLink(true).hasStaticValues(true).values(Arrays.asList(
                        new ValueDTO("http://dcat-ap.de/def/licenses/dl-by-de/2.0", new CounterDTO(500, 500), false)
                )).build();

        String jsonContent = new Gson().toJson(filterDTO);

        SearchDTO searchDTO = new SearchDTO();
        when(elasticSearchProvider.getTopFiltersThatContain(any(SearchDTO.class), isNull(), anyString(), anyString()))
                .thenReturn(filterDTO);

        RequestBuilder request = MockMvcRequestBuilders
                .post("/filteredOptions")
                .content(new Gson().toJson(searchDTO))
                .param("uri", (String) null)
                .param("filterGroupTitle", "License")
                .param("containsText", "dcat")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(jsonContent, false))
                .andReturn();

    }
}