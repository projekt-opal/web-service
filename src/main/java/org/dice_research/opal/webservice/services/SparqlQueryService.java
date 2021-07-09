package org.dice_research.opal.webservice.services;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.sparql.core.Quad;
import org.dice_research.opal.webservice.config.EnvVars;
import org.dice_research.opal.webservice.fetcher.SparqlFetcher;
import org.dice_research.opal.webservice.model.entity.dto.ChangesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SparqlQueryService {

	@Autowired
	private SparqlFetcher fetcher;

	@Autowired
	Environment env;

	public boolean hasChanges(String uri) {

		Set<Triple> previousTriples = getTriplesFromQuery(env.getProperty(EnvVars.SPARQL_ENDPOINT_PREVIOUS.toString()),
				uri);
		Set<Triple> currentTriples = getTriplesFromQuery(env.getProperty(EnvVars.SPARQL_ENDPOINT_LATEST.toString()),
				uri);

		for (Triple t : currentTriples)
			if (t.getSubject().isBlank() || t.getObject().isBlank())
				continue;

			else if (!previousTriples.contains(t))
				return true;

		return false;
	}

	public ChangesDTO hasChangesAsDTO(String uri) {
		ChangesDTO dto = new ChangesDTO();
		dto.setHasChanges(false);
		dto.setUri(uri);
		dto.setHasChanges(hasChanges(uri));
		return dto;
	}

	public ChangesDTO getChangesAsDTO(String uri) {
		ChangesDTO dto = new ChangesDTO();

		Set<String> previousTriples = new HashSet<String>();
		Set<String> currentTriples = new HashSet<String>();

		Set<String> removedTriples = new HashSet<String>();
		Set<String> addedTriples = new HashSet<String>();

		for (Triple t : getTriplesFromQuery(env.getProperty(EnvVars.SPARQL_ENDPOINT_PREVIOUS.toString()), uri)) {
			if (t.getSubject().isBlank() || t.getObject().isBlank())
				continue;

			previousTriples.add(t.toString());
		}

		for (Triple t : getTriplesFromQuery(env.getProperty(EnvVars.SPARQL_ENDPOINT_LATEST.toString()), uri)) {
			if (t.getSubject().isBlank() || t.getObject().isBlank())
				continue;

			currentTriples.add(t.toString());
		}

		for (String s : currentTriples) {
			if (!previousTriples.contains(s))
				addedTriples.add(s);
		}

		for (String s : previousTriples) {
			if (!currentTriples.contains(s))
				removedTriples.add(s);
		}

		String[] rem = new String[removedTriples.size()];
		String[] add = new String[addedTriples.size()];
		dto.setRemovedTriples(removedTriples.toArray(rem));
		dto.setAddedTriples(addedTriples.toArray(add));

		return dto;
	}

	private Set<Triple> getTriplesFromQuery(String endpoint, String resource) {
		String data = fetcher.fetch(endpoint, resource);
		Set<Triple> setTriples = new HashSet<Triple>();
		FilterSinkRDF filtered = new FilterSinkRDF(setTriples);
		RDFParser.create().source(new StringReader(data)).lang(Lang.TURTLE).parse(filtered);
		setTriples = filtered.getSetTriples();
		return setTriples;
	}

	public class FilterSinkRDF extends StreamRDFBase {

		private Set<Triple> setTriples;

		public FilterSinkRDF(Set<Triple> setTriples) {
			this.setTriples = setTriples;
		}

		public Set<Triple> getSetTriples() {
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

}