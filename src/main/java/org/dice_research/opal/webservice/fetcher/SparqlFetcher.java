package org.dice_research.opal.webservice.fetcher;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.stereotype.Component;

/**
 * A simple {@link Fetcher} for SPARQL that tries to get DataSets from a SPARQL
 * endpoint using the query {@value #DATA_SET_QUERY}.
 *
 * @author Geraldo de Souza Jr (gsjunior@uni-paderborn.de)
 *
 */
@Component
public class SparqlFetcher {

	protected String graphQuery = "construct { ?s ?p ?o. " + "?o ?p2 ?o2. } " + "where { " + " " + "?s ?p ?o. "
			+ "OPTIONAL { ?o ?p2 ?o2.} " + " " + "}";

	public String fetch(String uri, String dataSetResource) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		Query query = QueryFactory.create(graphQuery.replaceAll("\\?s", "<" + dataSetResource + ">"));
		QueryExecution qexecGraph = org.apache.jena.query.QueryExecutionFactory.createServiceRequest(uri, query);
		Iterator<Triple> triples = qexecGraph.execConstructTriples();
		RDFDataMgr.writeTriples(stream, new SelectedTriplesIterator(triples));
		return new String(stream.toByteArray());
	}

	protected static class SelectedTriplesIterator implements Iterator<Triple> {
		private Iterator<Triple> triples;

		public SelectedTriplesIterator(Iterator<Triple> triples) {
			this.triples = triples;
		}

		@Override
		public boolean hasNext() {
			return triples.hasNext();
		}

		@Override
		public Triple next() {
			return triples.next();
		}

	}

}