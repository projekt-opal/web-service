package org.diceresearch.opalwebservices.utility.triplestore;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.retry.core.QueryExecutionFactoryRetry;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Profile(value = {"triplestore", "default"})
@Service
public class QueryExecutionFactoryHttpProvider implements QueryExecutionFactoryProvider, CredentialsProvider {

    private org.apache.http.auth.Credentials credentials;
    private org.aksw.jena_sparql_api.core.QueryExecutionFactory qef;

    @Value(value = "${info.opal.tripleStore.url}")
    private String url;
    @Value(value = "${info.opal.tripleStore.username}")
    private String username;
    @Value(value = "${info.opal.tripleStore.password}")
    private String password;

    @PostConstruct
    public void initialQueryExecutionFactory() {
        credentials = new UsernamePasswordCredentials(username, password);

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.setDefaultCredentialsProvider(this);
        org.apache.http.impl.client.CloseableHttpClient client = clientBuilder.build();

        this.qef = new QueryExecutionFactoryHttp(url,
                new org.apache.jena.sparql.core.DatasetDescription(), client);
        this.qef = new QueryExecutionFactoryRetry(qef, 5, 1000);
    }

    public QueryExecutionFactory getQef() {
        return qef;
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
