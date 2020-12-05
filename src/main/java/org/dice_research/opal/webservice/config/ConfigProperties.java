package org.dice_research.opal.webservice.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigProperties {

	public static final String FILENAME = "opal-webservices.properties";
	private Properties properties;

	public String get(String key) {
		if (properties == null) {
			loadProperties();
		}
		return properties.getProperty(key);
	}

	private void loadProperties() {
		File file = new File(FILENAME);
		if (!file.canRead()) {
			throw new RuntimeException("Can not read file: " + file.getAbsolutePath());
		}

		properties = new Properties();
		try {
			properties.load(new FileInputStream(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}