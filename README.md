# OPAL Web-Service

This component is part of the [demo](https://github.com/projekt-opal/demo) project.
It requires [opaldata](https://github.com/projekt-opal/opaldata) to access data.
It provides data for the [web-ui](https://github.com/projekt-opal/web-ui).

# How to run

To be able to run you must provide a *.env* file in the root folder of the project that is similar to

```
# Elasticsearch configuration
OPAL_ELASTICSEARCH_URL=localhost
OPAL_ELASTICSEARCH_PORT=9200
ES_INDEX=opal

# SPARQL endpoint for previously crawled dataset
SPARQL_ENDPOINT_PREVIOUS=http://localhost:3030/2020-06/

# SPARQL endpoint for latest crawled dataset
SPARQL_ENDPOINT_LATEST=http://localhost:3030/2020-10/

# Will be added as parameter 'urlPrefix' in GEO_REDIRECT URL
GEO_URL_PREFIX=http://localhost:3000/view/datasetView?uri=

# URL to redirect to when map bounding box was selected by user
GEO_REDIRECT=http://localhost:8081/getGeoDatasetsHtml
```

Then, by running the command 

```
docker-compose up -d
```

you have the demo containers running and the demo is available on port 8081 (you can set any port that you want in the docker-compose.yml) of your server.

Check the running Docker container by opening 
http://localhost:8081/opalinfo

## Notes

- API REST methods can be found in [RestAPIController](src/main/java/org/dice_research/opal/webservice/control/RestAPIController.java).
- Development notes are in the [wiki](https://github.com/projekt-opal/web-service/wiki).


## Credits

[Data Science Group (DICE)](https://dice-research.org/) at [Paderborn University](https://www.uni-paderborn.de/)

This work has been supported by the German Federal Ministry of Transport and Digital Infrastructure (BMVI) in the project [Open Data Portal Germany (OPAL)](http://projekt-opal.de/) (funding code 19F2028A).