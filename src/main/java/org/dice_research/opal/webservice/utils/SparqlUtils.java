package org.dice_research.opal.webservice.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SparqlUtils {

	public static String PREVIOUS_URI;
	public static String CURRENT_URI;

	static {

		File file = new File(File.separator + System.getenv("SPARQL_CONFIG_FILE"));

		// Workaround if environment variable is not set
		if (!file.exists()) {
			file = new File("config" + File.separator + "sparql-config.properties");
			System.out.println(file.getAbsolutePath());
		}

		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();

			while (line != null) {
				String[] prop = line.split("=");
				if (prop[0].equals("previous_endpoint_uri"))
					PREVIOUS_URI = prop[1];
				else
					CURRENT_URI = prop[1];

				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
