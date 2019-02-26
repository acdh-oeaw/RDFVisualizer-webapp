package gr.ics.forth.rdfvisualizer.api.core.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.sesame2.driver.VirtuosoRepository;

/*
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
public class VirtuosoManager extends AbstractRDFManager {
    private final static Logger _logger = LoggerFactory.getLogger(VirtuosoManager.class);

    private RepositoryConnection connection;

    public VirtuosoManager(String virtuosoUrl, String virtuosoUser, String virtuosoPassword)
            throws RepositoryException {

        this.connection = new VirtuosoRepository(virtuosoUrl, virtuosoUser, virtuosoPassword).getConnection();

    }

    @Override
    public List<BindingSet> query(String sparqlQuery) throws RepositoryException, Exception {
        List<BindingSet> retList = new ArrayList<>();

        _logger.trace(sparqlQuery);
        TupleQueryResult results = connection.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery).evaluate();

        while (results.hasNext())
            retList.add(results.next());

        results.close();

        return retList;
    }

    @Override
    public void close() {

        try {

            this.connection.close();

        }
        catch (RepositoryException ex) {

            _logger.error("error while attempting to close virtuoso connection", ex);
        }
    }

}
