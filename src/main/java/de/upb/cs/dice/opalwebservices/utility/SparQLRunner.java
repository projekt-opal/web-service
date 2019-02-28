package de.upb.cs.dice.opalwebservices.utility;

import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.retry.core.QueryExecutionFactoryRetry;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class SparQLRunner implements CredentialsProvider {

    private static final Logger logger = LoggerFactory.getLogger(SparQLRunner.class);

    private org.apache.http.auth.Credentials credentials;
    private org.aksw.jena_sparql_api.core.QueryExecutionFactory qef;

    public SparQLRunner() {
        initialQueryExecutionFactory();
    }

    private void initialQueryExecutionFactory() {
        credentials = new UsernamePasswordCredentials("dba", "dba");

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.setDefaultCredentialsProvider(this);
        org.apache.http.impl.client.CloseableHttpClient client = clientBuilder.build();

        qef = new QueryExecutionFactoryHttp("http://localhost:8890/sparql",
                new org.apache.jena.sparql.core.DatasetDescription(), client);
        qef = new QueryExecutionFactoryRetry(qef, 5, 1000);
    }

    public Long execSelectCount(Query query) throws Exception {
        try (QueryExecution queryExecution = qef.createQueryExecution(query)) {
            ResultSet resultSet = queryExecution.execSelect();
            if (resultSet != null && resultSet.hasNext()) {
                QuerySolution querySolution = resultSet.nextSolution();
                long num = querySolution.getLiteral("num").asLiteral().getLong();
                logger.info("num: " + num);
                return num;
            } else throw new Exception("No results received from TripleStore");
        }
    }


    public List<Resource> execSelect(Query query) throws Exception {
        try (QueryExecution queryExecution = qef.createQueryExecution(query)) {
            ResultSet resultSet = queryExecution.execSelect();
            if (resultSet != null) {
                List<Resource> ret = new ArrayList<>();
                while (resultSet.hasNext()) {
                    QuerySolution querySolution = resultSet.nextSolution();
                    Resource s = querySolution.getResource("s");
                    ret.add(s);
                }
                return ret;
            }
            else throw new Exception("No results received from TripleStore");
        }
    }

    public Model executeConstruct(Query query) {
        Model model;
        try (QueryExecution queryExecution = qef.createQueryExecution(query)) {
            model = queryExecution.execConstruct();
        }
        return model;
    }

    @Override
    public void setCredentials(AuthScope authScope, Credentials credentials) {

    }

    @Override
    public Credentials getCredentials(AuthScope authScope) {
        return credentials;
    }

    @Override
    public void clear() {

    }
}
