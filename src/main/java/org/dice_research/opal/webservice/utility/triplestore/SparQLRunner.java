package org.dice_research.opal.webservice.utility.triplestore;

import org.aksw.commons.util.Pair;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Profile(value = {"triplestore", "test-triplestore", "default"})
@Service
public class SparQLRunner {

    private static final Logger logger = LoggerFactory.getLogger(SparQLRunner.class);

    private final QueryExecutionFactoryProvider queryExecutionFactoryProvider;

    @Autowired
    public SparQLRunner(QueryExecutionFactoryProvider queryExecutionFactoryProvider) {
        this.queryExecutionFactoryProvider = queryExecutionFactoryProvider;
    }


    public Long execSelectCount(Query query) throws Exception {
        try (QueryExecution queryExecution =
                     queryExecutionFactoryProvider.getQef().createQueryExecution(query)) {
            ResultSet resultSet = queryExecution.execSelect();
            if (resultSet != null && resultSet.hasNext()) {
                QuerySolution querySolution = resultSet.nextSolution();
                long num = querySolution.getLiteral("num").asLiteral().getLong();
                logger.info("num: " + num);
                return num;
            } else throw new Exception("No results received from TripleStore");
        }
    }


    public List<Resource> execSelect(Query query, String resourceVariable) throws Exception {
        try (QueryExecution queryExecution =
                queryExecutionFactoryProvider.getQef().createQueryExecution(query)) {
            ResultSet resultSet = queryExecution.execSelect();
            if (resultSet != null) {
                List<Resource> ret = new ArrayList<>();
                while (resultSet.hasNext()) {
                    try {
                        QuerySolution querySolution = resultSet.nextSolution();
                        Resource s = querySolution.getResource(resourceVariable);
                        ret.add(s);
                    } catch (Exception ignore) {
                    }
                }
                return ret;
            }
            else throw new Exception("No results received from TripleStore");
        }
    }

    public List<Pair<Resource, Integer>> execSelectReturnPair(Query query, String resourceVariable, String num) throws Exception {
            try (QueryExecution queryExecution =
                    queryExecutionFactoryProvider.getQef().createQueryExecution(query)) {
                ResultSet resultSet = queryExecution.execSelect();
                if (resultSet != null) {
                    List<Pair<Resource, Integer>> ret = new ArrayList<>();
                    while (resultSet.hasNext()) {
                        try {
                            QuerySolution querySolution = resultSet.nextSolution();
                            Resource s = querySolution.getResource(resourceVariable);
                            Integer n = querySolution.getLiteral(num).getInt();
                            ret.add(Pair.create(s, n));
                        } catch (Exception ignore) {
                        }
                    }
                    return ret;
                }
                else throw new Exception("No results received from TripleStore");
            }
        }

    public Model executeConstruct(Query query) {
        Model model;
        try (QueryExecution queryExecution =
                queryExecutionFactoryProvider.getQef().createQueryExecution(query)) {
            model = queryExecution.execConstruct();
        }
        return model;
    }


}
