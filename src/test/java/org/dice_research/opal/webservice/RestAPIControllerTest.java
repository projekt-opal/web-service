package org.dice_research.opal.webservice;

import org.dice_research.opal.webservice.control.RestAPIController;
import org.dice_research.opal.webservice.model.entity.DataSet;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link RestAPIController}.
 * 
 * As the controller mainly uses ElasticSearchProvider,
 * {@link ElasticSearchProviderTest} should be used for main tests.
 */
class RestAPIControllerTest {

	private RestAPIController restAPIController;
	private DataSet berlinUm1940wms;

	@BeforeEach
	void setUp() {
		restAPIController = new RestAPIController(ElasticSearchProviderTest.createElasticSearchProvider());
		berlinUm1940wms = restAPIController.getDataSet(ElasticSearchProviderTest.BERLIN_UM_1940_WMS);

		// Only execute tests if the dataset exists
		Assume.assumeNotNull(berlinUm1940wms);
	}

	@Test
	void test() {
		Assert.assertEquals("Berlin um 1940 - [WMS]", berlinUm1940wms.getTitle());
	}
}