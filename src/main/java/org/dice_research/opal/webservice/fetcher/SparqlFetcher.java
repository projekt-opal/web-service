package org.dice_research.opal.webservice.fetcher;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Iterator;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.delay.core.QueryExecutionFactoryDelay;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple {@link Fetcher} for SPARQL that tries to get DataSets from a SPARQL
 * endpoint using the query {@value #DATA_SET_QUERY}.
 *
 * @author Geraldo de Souza Jr (gsjunior@uni-paderborn.de)
 *
 */
public class SparqlFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(SparqlFetcher.class);

    /**
     * The delay that the system will have between sending two queries.
     */

    //protected String dataSetQuery = "select ?s where {?s a <http://www.w3.org/ns/dcat#Dataset>.} ";
    protected String graphQuery = "construct { ?s ?p ?o. " + "?o ?p2 ?o2. } " + "where { " + " "
            + "?s ?p ?o. " + "OPTIONAL { ?o ?p2 ?o2.} " + " " + "}";

    protected int delay;
    protected int limit = 0;
    protected File dataDirectory = FileUtils.getTempDirectory();
    protected boolean checkForUriType = false;


    public SparqlFetcher() {

    }

    @SuppressWarnings("deprecation")
	public File fetch(String uri, String dataSetResource) {
        // Check whether we can be sure that it is a SPARQL endpoint
        QueryExecutionFactory qef = null;
        QueryExecution execution = null;
        File dataFile = null;
        OutputStream out = null;
            try {
                // Create query execution instance
                qef = initQueryExecution(uri);
                // create temporary file
                try {
                    dataFile = File.createTempFile("fetched_", "", dataDirectory);
                    out = new BufferedOutputStream(new FileOutputStream(dataFile));
                } catch (IOException e) {
                    LOGGER.error("Couldn't create temporary file for storing fetched data. Returning null.", e);
                    return null;
                }
                  
                    LOGGER.info("- Now Fetching - " + dataSetResource);

                    Query query = QueryFactory.create(graphQuery.replaceAll("\\?s", "<" + dataSetResource + ">"));

                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            LOGGER.error("An error occurred when fetching URI: " + uri, e);
                        }

                        try {
                            QueryExecution qexecGraph = org.apache.jena.query.QueryExecutionFactory
                                    .createServiceRequest(uri, query);

                            Iterator<Triple> triples = qexecGraph.execConstructTriples();

                            RDFDataMgr.writeTriples(out, new SelectedTriplesIterator(triples));

                        } catch (QueryExceptionHTTP e) {

                           /* if (e.getResponseCode() == 404 || e.getResponseCode() == 500) {
                                tryAgain = true;
                                LOGGER.info("Error while fetching " + dataSetResource + ". Trying again...");
                            }*/

                        }
                    
                

//            RDFDataMgr.writeTriples(out, new SelectedTriplesIterator(resultSet));
            } catch (Exception e) {
                // If this should have worked, print a message, otherwise silently return null
            	e.printStackTrace();
                return null;
            } finally {
                IOUtils.closeQuietly(out);
                if (execution != null) {
                    execution.close();
                }
                if (qef != null) {
                    qef.close();
                }
            }
            return dataFile;
        
    }

    protected QueryExecutionFactory initQueryExecution(String uri) throws ClassNotFoundException, SQLException {
        QueryExecutionFactory qef;
        qef = new QueryExecutionFactoryHttp(uri);
        qef = new QueryExecutionFactoryDelay(qef, delay);
        try {
            LOGGER.info("Starting to Query uri:" + uri);
            return new QueryExecutionFactoryPaginated(qef, 2000);
        } catch (Exception e) {
            LOGGER.info("Couldn't create Factory with pagination. Returning Factory without pagination. Exception: {}",
                    e.getLocalizedMessage());
            return qef;
        }
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
    
    public static void main(String[] args) {
		new SparqlFetcher().fetch("http://opaldata.cs.uni-paderborn.de:3030/opal-2020-04", "http://projekt-opal.de/dataset/18e77c1c778125780c053b4d3a10c2dd");
	}

}
