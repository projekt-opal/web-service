package org.dice_research.opal.webservice.control;

import org.dice_research.opal.webservice.model.entity.DataSet;
import org.dice_research.opal.webservice.model.entity.dto.ChangesDTO;
import org.dice_research.opal.webservice.model.entity.dto.DataSetDTO;
import org.dice_research.opal.webservice.model.entity.dto.FilterDTO;
import org.dice_research.opal.webservice.model.entity.dto.SearchDTO;
import org.dice_research.opal.webservice.services.ElasticSearchProvider;
import org.dice_research.opal.webservice.services.SparqlQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RestAPIController {

	private final ElasticSearchProvider provider;
	private final SparqlQueryService sparqlService;

	@Autowired
	public RestAPIController(ElasticSearchProvider provider, SparqlQueryService sparqlService) {
		this.provider = provider;
		this.sparqlService = sparqlService;
	}

	@CrossOrigin
	@PostMapping("/dataSets/getNumberOfDataSets")
	public Long getNumberOFDataSets(@RequestBody() SearchDTO searchDTO) {
		return provider.getNumberOfDataSets(searchDTO);
	}

	@CrossOrigin
	@PostMapping("/dataSets/getNumberOfRelatedDataSets")
	public Long getNumberOFRelatedDataSets(@RequestParam(name = "uri") String uri,
			@RequestBody(required = false) SearchDTO searchDTO) {
		return provider.getNumberOfRelatedDataSets(searchDTO, uri);
	}

	@CrossOrigin
	@PostMapping("/dataSets/getSubList")
	public List<DataSetDTO> getSubListOfDataSets(
			@RequestParam(name = "low", required = false, defaultValue = "0") Integer low,
			@RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit,
			@RequestBody(required = false) SearchDTO searchDTO) {
		return provider.getSublistOfDataSets(searchDTO, low, limit);
	}

	@CrossOrigin
	@PostMapping("/dataSets/getRelatedSubList")
	public List<DataSetDTO> getSubListOfRelatedDataSets(@RequestParam(name = "uri", required = false) String uri,
			@RequestParam(name = "low", required = false, defaultValue = "0") Integer low,
			@RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit,
			@RequestBody(required = false) SearchDTO searchDTO) {
		return provider.getSubListOfRelatedDataSets(searchDTO, uri, low, limit);
	}

	@CrossOrigin
	@PostMapping("/filters/listFoRelated")
	public List<FilterDTO> getFiltersForRelatedDataSets(@RequestParam(name = "uri", required = false) String uri,
			@RequestBody(required = false) SearchDTO searchDTO) {
		return provider.getFilters(searchDTO, uri);
	}

	@CrossOrigin
	@GetMapping("/dataSet")
	public DataSet getDataSet(@RequestParam(name = "uri", required = false) String uri) {
		return provider.getDataSet(uri);
	}

	@CrossOrigin
	@PostMapping("/filters/list")
	public List<FilterDTO> getFilters(@RequestBody SearchDTO searchDTO) {
		return provider.getFilters(searchDTO, null);
	}

	@CrossOrigin
	@PostMapping("/dataSets/hasChanges")
	public ChangesDTO hasChanges(@RequestParam(name = "uri", required = false) String uri) {
		return sparqlService.hasChangesAsDTO(uri);
	}

	@CrossOrigin
	@PostMapping("/dataSets/getChanges")
	public ChangesDTO getChanges(String uri) {
		return sparqlService.getChangesAsDTO(uri);
	}

	@CrossOrigin
	@PostMapping("/filteredOptions")
	public FilterDTO getFilter(@RequestBody(required = false) SearchDTO searchDTO,
			@RequestParam(name = "uri", required = false) String uri,
			@RequestParam(name = "filterGroupTitle", required = false) String filterGroupTitle,
			@RequestParam(name = "containsText", required = false) String containsText) {
		return provider.getTopFiltersThatContain(searchDTO, uri, filterGroupTitle, containsText);
	}

	/**
	 * Gets configuration info
	 */
	@CrossOrigin
	@GetMapping("/opalinfo")
	public String getInfo() {
		return provider.getInfo();
	}

	/**
	 * Gets JSON array with JSON object with keys uri and optional title.
	 */
	@CrossOrigin
	@GetMapping("/getGeoDatasets")
	public String getGeoDatasets(@RequestParam String top, @RequestParam String left, @RequestParam String bottom,
			@RequestParam String right) {
		return provider.getGeoDatasets(Double.parseDouble(top), Double.parseDouble(left), Double.parseDouble(bottom),
				Double.parseDouble(right));
	}

}