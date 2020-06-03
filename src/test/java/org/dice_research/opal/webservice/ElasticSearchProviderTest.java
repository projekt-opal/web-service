package org.dice_research.opal.webservice;

import org.apache.http.HttpHost;
import org.dice_research.opal.webservice.config.ThemeConfiguration;
import org.dice_research.opal.webservice.model.entity.DataSet;
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
	private DataSet berlinUm1940wms;

	@BeforeEach
	void setUp() throws Exception {
		elasticSearchProvider = createElasticSearchProvider();
		berlinUm1940wms = elasticSearchProvider.getDataSet(BERLIN_UM_1940_WMS);

		// Only execute tests if the dataset exists
		Assume.assumeNotNull(berlinUm1940wms);
	}

	@Test
	void test() {
		Assert.assertEquals("Berlin um 1940 - [WMS]", berlinUm1940wms.getTitle());

		// TODO 2019-08-13T13:34:24.766691
		// System.out.println(berlinUm1940wms.getIssued());
		// TODO 2019-08-21T22:09:23.109356
		// System.out.println(berlinUm1940wms.getModified());
	}

	public static ElasticSearchProvider createElasticSearchProvider() {
		RestHighLevelClient restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost(
				TestConfiguration.getElasticsearchUrl(), TestConfiguration.getElasticsearchPort(), "http")));

		// TODO Create proper instance
		ThemeConfiguration themeConfiguration = new ThemeConfiguration();

		return new ElasticSearchProvider(restHighLevelClient, themeConfiguration);
	}
}