package org.dice_research.opal.webservice.sparql;

import org.dice_research.opal.webservice.fetcher.SparqlFetcher;
import org.junit.Test;

public class SparqlQueryTest {

	public static final String ENDPOINT = "http://opaldata.cs.uni-paderborn.de:3030/opal-2020-04/query";

	@Test
	public void fetch() {
		new SparqlFetcher().fetch(ENDPOINT, "http://projekt-opal.de/dataset/18e77c1c778125780c053b4d3a10c2dd");
	}

	@Test
	public void fetch2() {
		// URI with changes in 2 endpoints
		new SparqlFetcher().fetch(ENDPOINT, "http://projekt-opal.de/dataset/baea40bcb5785f459a04fa87646589e6");
	}

}
