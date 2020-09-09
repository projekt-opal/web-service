import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

/**
 * Minimal code example for OPAL and Elasticsearch.
 * 
 * The required Elasticsearch classes are included via pom.xml.
 * 
 * The available API methods can be combined. Have a look in the documentation
 * chapters "Getting started", "Search APIs" and "Using Java Builders". For
 * learning, exchange some code parts.
 * 
 * @see https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.3/java-rest-high-getting-started.html
 * 
 * @author Adrian Wilke
 */
public class ElasticsearchExample {

	/**
	 * Configuration and running example.
	 */
	public static void main(String[] args) throws Exception {

		String hostname = "opaldata.cs.uni-paderborn.de";

		// Available indexes: http://opaldata.cs.uni-paderborn.de:9200/_cat/indices?v
		String index = "opal_july";

		// Available fields:
		// http://opaldata.cs.uni-paderborn.de:9200/opal_july/_mapping/?pretty
		String field = "title";

		String query = "berlin";

		ElasticsearchExample example = new ElasticsearchExample();

		// Initialize client
		example.setClient(hostname);

		// Search
		SearchResponse results = example.search(index, field, query);

		// Print results
		for (SearchHit hit : results.getHits().getHits()) {
			System.out.println(hit);
		}

		// Finish
		example.client.close();
	}

	private RestHighLevelClient client;

	/**
	 * @see https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.3/java-rest-high-getting-started-initialization.html
	 */
	private void setClient(String hostname) {
		client = new RestHighLevelClient(RestClient.builder(new HttpHost(hostname, 9200, "http")));
	}

	/**
	 * @see https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.3/java-rest-high-search.html
	 */
	private SearchResponse search(String index, String field, String query) throws Exception {

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.termQuery(field, query));
		searchSourceBuilder.from(0);
		searchSourceBuilder.size(5);
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

		SearchRequest searchRequest = new SearchRequest(index);
		searchRequest.source(searchSourceBuilder);

		return client.search(searchRequest, RequestOptions.DEFAULT);
	}

}