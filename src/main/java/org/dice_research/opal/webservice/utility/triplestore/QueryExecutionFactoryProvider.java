package org.dice_research.opal.webservice.utility.triplestore;

import org.aksw.jena_sparql_api.core.QueryExecutionFactoryQuery;
import org.springframework.stereotype.Service;

@Service
public interface QueryExecutionFactoryProvider {
    QueryExecutionFactoryQuery getQef();
}
