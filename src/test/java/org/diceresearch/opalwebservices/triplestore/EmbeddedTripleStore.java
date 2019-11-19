package org.diceresearch.opalwebservices.triplestore;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Profile("test-triplestore")
@Repository
public class EmbeddedTripleStore {

    private Dataset dataset;

    public EmbeddedTripleStore() {

        Model model = ModelFactory.createDefaultModel();
        model = model.read("db/testdb.ttl", "TURTLE");

        dataset = DatasetFactory.create();
        dataset.addNamedModel("http://projekt-opal.de", model);
    }


    public Dataset getDataSet() {
        return dataset;
    }
}
