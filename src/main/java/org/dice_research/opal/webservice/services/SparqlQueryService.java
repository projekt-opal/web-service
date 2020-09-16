package org.dice_research.opal.webservice.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Triple;
import org.dice_research.opal.webservice.fetcher.SparqlFetcher;
import org.springframework.beans.factory.annotation.Autowired;

public class SparqlQueryService {
	
	@Autowired
	private SparqlFetcher fetcher;
	
	
	
	public boolean hasChanges(String uri) {
		return false;
	}
	
	public void getChanges(String uri) {
		
	}
	
	private List<Triple> getTriplesFromQuery(String endpoint, String resource){
		List<Triple> listTriples = new ArrayList<Triple>();
		
		
		
		return listTriples;
	}

}
