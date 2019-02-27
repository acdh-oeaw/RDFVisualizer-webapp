/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ics.forth.rdfvisualizer.api.core.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.bigdata.rdf.sail.webapp.client.RemoteRepositoryManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
public class BlazegraphManager extends AbstractRDFManager{
    private final static Logger _logger = LoggerFactory.getLogger(BlazegraphManager.class);
    
    private static HttpClient _httpClient;
    
    private final RemoteRepositoryManager rpm;
    
    
    public BlazegraphManager(String sparqlEndPoint) throws RepositoryException, Exception{
         
        this.rpm = new RemoteRepositoryManager(sparqlEndPoint);

    } 
    
    public BlazegraphManager(String sparqlEndPoint, HttpClient httpClient, ExecutorService executor) throws RepositoryException, Exception{
         
        this.rpm = new RemoteRepositoryManager(sparqlEndPoint, httpClient, executor);
            
    }
    
    
    public BlazegraphManager(String blazegraphUrl, String blazegraphUser, String blazegraphPassword) throws RepositoryException, Exception{
        if(_httpClient == null) {

            SslContextFactory sslContextFactory = new SslContextFactory(true);
    
            _httpClient = new HttpClient(sslContextFactory);
        
        
            // Add authentication credentials
            AuthenticationStore auth = _httpClient.getAuthenticationStore();
            auth.addAuthentication(new BasicAuthentication(URI.create(blazegraphUrl), Authentication.ANY_REALM, blazegraphUser, blazegraphPassword));
        }
       
        if(!_httpClient.isStarted())
            _httpClient.start();  
        
        this.rpm = new RemoteRepositoryManager(blazegraphUrl, _httpClient, Executors.newCachedThreadPool());
            
    }    
    
    
    
    public void close(){

        try {
            this.rpm.close();
        }
        catch (Exception ex) {
            _logger.error("", ex);
        }

    }
    
    public List<BindingSet> query(String sparqlQuery) throws RepositoryException, Exception {

        List<BindingSet> retList = new ArrayList<>();

        _logger.trace(sparqlQuery);

        TupleQueryResult results = this.rpm.getRepositoryForDefaultNamespace().prepareTupleQuery(sparqlQuery)
                .evaluate();

        while (results.hasNext()) {

            BindingSet set = results.next();

            _logger.trace(set.toString());

            retList.add(set);

        }

        results.close();

        return retList;
    }
}
