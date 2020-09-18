package org.dice_research.opal.webservice.services;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.sparql.core.Quad;
import org.dice_research.opal.webservice.fetcher.SparqlFetcher;
import org.dice_research.opal.webservice.model.entity.dto.ChangesDTO;
import org.dice_research.opal.webservice.utils.SparqlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SparqlQueryService {
	
	@Autowired
	//private SparqlFetcher fetcher = new SparqlFetcher();
	private SparqlFetcher fetcher;
	
	
	
	public ChangesDTO hasChanges(String uri) {
		Set<Triple> previousTriples = getTriplesFromQuery(SparqlUtils.PREVIOUS_URI, uri);
		Set<Triple> currentTriples = getTriplesFromQuery(SparqlUtils.CURRENT_URI, uri);

		ChangesDTO dto = new ChangesDTO();
		dto.setHasChanges(false);
		dto.setUri(uri);
		
		
		for(Triple t : currentTriples)
			if(t.getSubject().isBlank() || t.getObject().isBlank())
				continue;
			
			else if(!previousTriples.contains(t))
				dto.setHasChanges(true);
	
		return dto;
	}
	
	public ChangesDTO getChanges(String uri) {
		ChangesDTO dto = new ChangesDTO();
		
		Set<String> previousTriples = new HashSet<String>();
		Set<String> currentTriples = new HashSet<String>();

		Set<String> removedTriples = new HashSet<String>();
		Set<String> addedTriples = new HashSet<String>();

		
		for (Triple t : getTriplesFromQuery(SparqlUtils.PREVIOUS_URI, uri)) {
			if(t.getSubject().isBlank() || t.getObject().isBlank())
				continue;
			
			previousTriples.add(t.toString());
			
		}
		
		
		for (Triple t : getTriplesFromQuery(SparqlUtils.CURRENT_URI, uri)) {
			if(t.getSubject().isBlank() || t.getObject().isBlank())
				continue;
			
			currentTriples.add(t.toString());
			
		}
		
		for (String s: currentTriples) {
			if(!previousTriples.contains(s))
				removedTriples.add(s);
		}
		
		for (String s: previousTriples) {
			if(!currentTriples.contains(s))
				addedTriples.add(s);
		}
		
		String[] rem = new String[removedTriples.size()];
		String[] add = new String[addedTriples.size()];
		dto.setRemovedTriples(removedTriples.toArray(rem));
		dto.setAddedTriples(addedTriples.toArray(add));
		
		return dto;
		
	}
	
	private Set<Triple> getTriplesFromQuery(String endpoint, String resource){
		
		File file = fetcher.fetch(endpoint, resource);
		
		
		Set<Triple> setTriples = new HashSet<Triple>();
		FilterSinkRDF filtered = new FilterSinkRDF(setTriples);
		
		RDFDataMgr.parse(filtered, file.getAbsolutePath(), Lang.TURTLE);
		
		setTriples = filtered.getSetTriples();
		
		
		return setTriples;
	}
	
	
	public class FilterSinkRDF extends StreamRDFBase {
		
		private Set<Triple> setTriples;

	    public FilterSinkRDF(Set<Triple> setTriples) {
	    	this.setTriples = setTriples;
	    }
	    
	    public Set<Triple> getSetTriples(){
	    	return setTriples;
	    }

	    @Override
	    public void triple(Triple triple) {
	    	setTriples.add(triple);
	    }

	    @Override
	    public void quad(Quad quad) {
	    	setTriples.add(quad.asTriple());
	    }

	}
	
	public static void main(String[] args) {
		System.out.println(new SparqlQueryService().hasChanges("http://projekt-opal.de/dataset/18e77c1c778125780c053b4d3a10c2dd"));
	}

}
