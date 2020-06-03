package org.dice_research.opal.webservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Provides methods to access .env settings.
 */
public abstract class TestConfiguration {

	public static final String KEY_ELASTICSEARCH_INDEX = "ES_INDEX";
	public static final String KEY_ELASTICSEARCH_PORT = "OPAL_ELASTICSEARCH_PORT";
	public static final String KEY_ELASTICSEARCH_URL = "OPAL_ELASTICSEARCH_URL";

	public static String getElasticsearchIndex() {
		return getEnvValue(KEY_ELASTICSEARCH_INDEX);
	}

	public static int getElasticsearchPort() {
		return Integer.parseInt(getEnvValue(KEY_ELASTICSEARCH_PORT));
	}

	public static String getElasticsearchUrl() {
		return getEnvValue(KEY_ELASTICSEARCH_URL);
	}

	public static String getEnvValue(String key) {
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(new File(".env")));
			return properties.getProperty(key);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}