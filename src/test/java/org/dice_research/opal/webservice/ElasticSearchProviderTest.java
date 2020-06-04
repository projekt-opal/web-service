package org.dice_research.opal.webservice;

import java.util.List;

import org.apache.http.HttpHost;
import org.dice_research.opal.webservice.config.ThemeConfiguration;
import org.dice_research.opal.webservice.model.entity.DataSet;
import org.dice_research.opal.webservice.model.entity.dto.FilterDTO;
import org.dice_research.opal.webservice.model.entity.dto.OrderByDTO;
import org.dice_research.opal.webservice.model.entity.dto.SearchDTO;
import org.dice_research.opal.webservice.services.ElasticSearchProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link ElasticSearchProvider}.
 */
class ElasticSearchProviderTest {

	public static final String BERLIN_UM_1940_WMS = "http://projekt-opal.de/dataset/3e71cf21852472e10462ab4fe97e679d";

	private ElasticSearchProvider elasticSearchProvider;

	/**
	 * Creates a {@link ElasticSearchProvider} instance.
	 */
	public static ElasticSearchProvider createElasticSearchProvider() {

		// Set host and port
		RestHighLevelClient restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost(
				TestConfiguration.getElasticsearchUrl(), TestConfiguration.getElasticsearchPort(), "http")));

		// Does not contain themes by simply creating instance
		ThemeConfiguration themeConfiguration = new ThemeConfiguration();

		ElasticSearchProvider elasticSearchProvider = new ElasticSearchProvider(restHighLevelClient,
				themeConfiguration);

		// Set index
		elasticSearchProvider.es_index = TestConfiguration.getElasticsearchIndex();

		return elasticSearchProvider;
	}

	/**
	 * Calls {@link ElasticSearchProvider#getFilters(SearchDTO, String)} like it is
	 * called by the UI at first time.
	 * 
	 * {"searchKey":"","searchIn":[],"orderBy":{"selectedOrderValue":"relevance"},"filters":[]}
	 */
	public static List<FilterDTO> getInitialFilters(ElasticSearchProvider elasticSearchProvider) {
		SearchDTO searchDTO = new SearchDTO();
		searchDTO.setSearchKey("");
		searchDTO.setSearchIn(new String[0]);
		OrderByDTO orderByDTO = new OrderByDTO();
		orderByDTO.setSelectedOrderValue("relevance");
		searchDTO.setOrderBy(orderByDTO);
		searchDTO.setFilters(new FilterDTO[0]);
		return elasticSearchProvider.getFilters(searchDTO, null);
	}

	@BeforeEach
	void setUp() throws Exception {
		elasticSearchProvider = createElasticSearchProvider();
	}

	/**
	 * Tests getting a specific dataset.
	 */
	@Test
	public void testGetDataset() {
		DataSet berlinUm1940wms = elasticSearchProvider.getDataSet(BERLIN_UM_1940_WMS);
		Assume.assumeNotNull(berlinUm1940wms);
		Assert.assertEquals("Berlin um 1940 - [WMS]", berlinUm1940wms.getTitle());

		// TODO 2019-08-13T13:34:24.766691
		// System.out.println(berlinUm1940wms.getIssued());
		// TODO 2019-08-21T22:09:23.109356
		// System.out.println(berlinUm1940wms.getModified());
	}

	/**
	 * Tests if filters are created. Calls
	 * {@link ElasticSearchProvider#getFilters(SearchDTO, String)}.
	 */
	@Test
	public void testInitialFilters() {
		List<FilterDTO> filters = getInitialFilters(elasticSearchProvider);

		Assert.assertNotNull("Initial filters not null", filters);
		Assert.assertTrue("Initial filters not empty", !filters.isEmpty());

		// For printing set true
		if (Boolean.TRUE) {
			for (FilterDTO filterDTO : filters) {
				System.out.println(filterDTO + " " + ElasticSearchProviderTest.class.getName());
			}
		}
	}
}