package org.dice_research.opal.webservice.sparql;

import org.dice_research.opal.webservice.fetcher.SparqlFetcher;
import org.junit.Test;

public class SparqlQueryTest {
	
	@Test
	public void fetch() {
		new SparqlFetcher()
		.fetch("http://opaldata.cs.uni-paderborn.de:3030/opal-2020-04/query", "http://projekt-opal.de/dataset/18e77c1c778125780c053b4d3a10c2dd");
	}

}
