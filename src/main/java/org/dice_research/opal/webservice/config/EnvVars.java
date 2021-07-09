package org.dice_research.opal.webservice.config;

/**
 * Environment variables.
 * 
 * @see Usage of geo variables:
 *      https://github.com/projekt-opal/hackathon/tree/gh-pages/geo
 */
public enum EnvVars {

	// Default ES configuration

	OPAL_ELASTICSEARCH_URL,

	OPAL_ELASTICSEARCH_PORT,

	ES_INDEX,

	// SPARQL endpoint for previously crawled dataset
	SPARQL_ENDPOINT_PREVIOUS,

	// SPARQL endpoint for latest crawled dataset
	SPARQL_ENDPOINT_LATEST,

	// Will be added as parameter 'urlPrefix' in GEO_REDIRECT URL
	GEO_URL_PREFIX,

	// URL to redirect to when map bounding box was selected by user
	GEO_REDIRECT
}