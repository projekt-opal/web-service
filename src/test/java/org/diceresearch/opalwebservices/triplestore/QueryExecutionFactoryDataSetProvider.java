package org.diceresearch.opalwebservices.triplestore;


import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactoryDataset;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.retry.core.QueryExecutionFactoryRetry;
import org.diceresearch.opalwebservices.utility.triplestore.QueryExecutionFactoryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Profile("test-triplestore")
@Repository
public class QueryExecutionFactoryDataSetProvider implements QueryExecutionFactoryProvider {

    private final EmbeddedTripleStore embeddedTripleStore;

    private org.aksw.jena_sparql_api.core.QueryExecutionFactory qef;

    @Autowired
    public QueryExecutionFactoryDataSetProvider(EmbeddedTripleStore embeddedTripleStore) {
        this.embeddedTripleStore = embeddedTripleStore;
    }


    @PostConstruct
    public void init() {
        this.qef = new QueryExecutionFactoryDataset(embeddedTripleStore.getDataSet());
        this.qef = new QueryExecutionFactoryRetry(qef, 5, 1000);
    }

    public QueryExecutionFactory getQef() {
        return qef;
    }
}
