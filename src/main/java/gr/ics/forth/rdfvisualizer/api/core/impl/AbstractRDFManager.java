/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ics.forth.rdfvisualizer.api.core.impl;

import gr.ics.forth.rdfvisualizer.api.core.utils.Pair;
import gr.ics.forth.rdfvisualizer.api.core.utils.Triple;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Resource;


import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;

import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




/**
 *
 * @author minadakn
 */
public abstract class AbstractRDFManager implements Closeable{
    private final static Logger _logger = LoggerFactory.getLogger(AbstractRDFManager.class);
    
    public abstract List<BindingSet> query(String sparqlQuery) throws RepositoryException, Exception;
    
    public String selectAll()
    {
        String queryString = "Select * where {?s ?p ?o}";
        
        return queryString;
    }
    
    
    public String selectAll(List<String> namedgraphs)
    {
        String fromClauses = "";
        
        for (String namedgraph : namedgraphs)
            fromClauses+="FROM <"+namedgraph+">\n";
            
        String queryString = "Select * \n"+fromClauses+"WHERE {?s ?p ?o}";
        
        return queryString;
    }
    
    public String selectAll(Resource resource)
    {
        String queryString = "Select * where {<"+resource.getURI().toString()+"> ?p ?o}";
        
        return queryString;
    }
    
    public String selectAll(Resource resource,List<String> namedgraphs)
    {
        String fromClauses = "";
        
        for (String namedgraph : namedgraphs)
            fromClauses+="FROM <"+namedgraph+">\n";
            
        String queryString = "Select * \n"+fromClauses+"WHERE {<"+resource.getURI().toString()+"> ?p ?o}";
        
        return queryString;
    }
    
    public String selectAll(String resource)
    {
        String queryString = "Select * where {<"+resource+"> ?p ?o}";
        
        return queryString;
    }
    
    public String selectAll(String resource,List<String> namedgraphs)
    {
        String fromClauses = "";
        
        for (String namedgraph : namedgraphs)
            fromClauses+="FROM <"+namedgraph+">\n";
            
        String queryString = "Select * \n"+fromClauses+"WHERE {<"+resource+"> ?p ?o}";
        
        return queryString;
    }
    
    public String selectLabels(String resource, String labelProperty)
    {
        String queryString = "Select ?label where {<"+resource+"> <"+labelProperty+"> ?label}";
        
        return queryString;
    }
    
    public String selectAllWithLabels(String labelProperty)
    {
        String queryString = "Select ?s ?p ?o ?slabel ?plabel ?olabel where {?s ?p ?o .\n"
                + "?s <"+labelProperty+"> ?slabel .\n"
                + "?p <"+labelProperty+"> ?plabel .\n"
                + "?o <"+labelProperty+"> ?olabel .\n"
                + " }";
        
        return queryString;
    }
    
    public String selectAllWithLabels(String resource, String labelProperty)
    {
        String queryString = "Select * where {<"+resource+"> ?p ?o .\n"
                + "<"+resource+"> <"+labelProperty+"> ?slabel .\n"
                + "?p <"+labelProperty+"> ?plabel .\n"
                + "?o <"+labelProperty+"> ?olabel .\n"
                + " }";
        
        return queryString;
    }
    
     public String selectAllOutgoingWithLabelsAndTypes(String resource, Set<String> labelProperties)
    {
        
        String labelPropertiesParam= "";
        
        Iterator<String> iterator = labelProperties.iterator();
        while(iterator.hasNext()) {
            String labelProperty = iterator.next();  
            labelPropertiesParam = labelPropertiesParam + " <"+labelProperty+"> |";
        }

        labelPropertiesParam =  labelPropertiesParam.substring(0,labelPropertiesParam.length()-1);

        String queryString = "Select *  \n"+
            "where\n"+
            "{ {\n"+
            "<"+resource+"> ?p ?o .\n"+
            "<"+resource+">  rdf:type ?stype .\n"+
            "OPTIONAL {<"+resource+">  \n"+
            labelPropertiesParam +"  ?slabel }.\n"+

            "OPTIONAL {?p "+labelPropertiesParam +" ?plabel }.\n"+
            "OPTIONAL {?o "+labelPropertiesParam +"  ?olabel }.\n"+

            "OPTIONAL {?o rdf:type ?otype} .\n"+
            "} "+
                 "UNION\n"+
            "{ \n"+
            "<"+resource+"> ?p ?o \n"+
            ".\n"+
            "<"+resource+">  rdf:type ?stype .\n"+
             "OPTIONAL {<"+resource+">  \n"+
            labelPropertiesParam +"  ?slabel }.\n"+
            " OPTIONAL{?o "+labelPropertiesParam +"  ?olabel }.\n"+
                   
           "OPTIONAL {?p "+labelPropertiesParam +"  ?plabel }.\n"+
        
            "  \n"+
            "FILTER(isLiteral(?o))\n"+
            "} }\n";
              
        _logger.trace("QUERY"+queryString);
        

        return queryString;
    }
    
    public String selectLabel(String resource, String labelProperty)
    {
        String queryString = "Select ?label where {<"+resource+"> <"+labelProperty+"> ?label .\n"
                + " }";  
        return queryString;
    }
    
    public String selectType(String resource)
    {
        String queryString = "Select ?type where {<"+resource+"> rdf:type ?type .\n"
                + " }";  
        return queryString;
    }
    
     public String returnLabel(String resource, String labelProperty) throws RepositoryException, MalformedQueryException, QueryEvaluationException, Exception
    {

        String query = selectLabel(resource, labelProperty);
      
        List<BindingSet> sparqlResults = query(query);
        
        String label = "";
       
        for (BindingSet result : sparqlResults) {
           
            _logger.trace("LABEL: "+result.toString());
           
            label = result.getBinding("label").getValue().stringValue();

    }
        
    return label;
        
    }
     
      public String returnType(String resource) throws RepositoryException, MalformedQueryException, QueryEvaluationException, Exception
    {

        String query = selectType(resource);
      
        List<BindingSet> sparqlResults = query(query);
        
        String label = "";
       
        for (BindingSet result : sparqlResults) {
           
            _logger.trace("LABEL: "+result.toString());
           
            label = result.getBinding("type").getValue().stringValue();

    }
        
    return label;
        
    }
    
    

    
     public Map<Pair,List<Pair>> returnOutgoingLinks(String resource, String labelProperty) 
             throws RepositoryException, MalformedQueryException, QueryEvaluationException, Exception {

        Map<Pair,List<Pair>> outgoingLinks = new HashMap<Pair,List<Pair>>();
        
        String query = selectAllWithLabels(resource,labelProperty);
      
        List<BindingSet> sparqlResults = query(query);
       
        for (BindingSet result : sparqlResults) {
           
            _logger.trace(result.toString());
           
            Pair mapKey = new Pair();
            Pair mapValue = new Pair();
            
            String key_uri = result.getBinding("p").getValue().stringValue();
            String key_label = result.getBinding("plabel").getValue().stringValue();
            mapKey.setPairKey(key_uri);
            mapKey.setPairValue(key_label);
            
            String value_uri = result.getBinding("o").getValue().stringValue();
            String value_label = result.getBinding("olabel").getValue().stringValue();
            mapValue.setPairKey(value_uri);
            mapValue.setPairValue(value_label);           
            
            if(outgoingLinks.containsKey(mapKey)) {

             List<Pair> objects = outgoingLinks.get(mapKey);

             objects.add(mapValue);

             outgoingLinks.put(mapKey, objects);

            } 
            else {
                List<Pair> objects = new ArrayList();
                objects.add(mapValue);
                outgoingLinks.put(mapKey, objects);
            }
        }

        return outgoingLinks;
        
    }
    
     
     public Map<Triple,List<Triple>> returnOutgoingLinksWithTypes(String resource,Set<String> labelProperty) 
             throws RepositoryException, MalformedQueryException, QueryEvaluationException, Exception{

        Map<Triple,List<Triple>> outgoingLinks = new HashMap<Triple,List<Triple>>();
        
        String query = selectAllOutgoingWithLabelsAndTypes(resource,labelProperty);
      
        List<BindingSet> sparqlResults = query(query);
       
        _logger.trace("QUERY"+query);
        
        for (BindingSet result : sparqlResults) {
           
           
            Triple mapKey = new Triple();
            Triple mapValue = new Triple();
            
            String key_uri = result.getBinding("p").getValue().stringValue();
            String key_label = "NOLABEL";
            if(result.getBinding("plabel")!=null)
                key_label = result.getBinding("plabel").getValue().stringValue();
            
            //String key_type = result.getBinding("ptype").getValue().stringValue();
            String key_type = "NOTYPE";
            mapKey.setSubject(key_uri);
            mapKey.setLabel(key_label);
            mapKey.setType(key_type);
            
            String value_label="NOLABEL";
            String value_type="NOTYPE";
            String value_uri = result.getBinding("o").getValue().stringValue();
            
            if(result.getBinding("olabel")!=null)
            value_label = result.getBinding("olabel").getValue().stringValue();

            if(result.getBinding("otype")!=null)
            value_type = result.getBinding("otype").getValue().stringValue();
            mapValue.setSubject(value_uri);
            mapValue.setLabel(value_label);
            mapValue.setType(value_type);
            
            
           
             if(outgoingLinks.containsKey(mapKey)) {

                 List<Triple> objects = outgoingLinks.get(mapKey);
    
                 objects.add(mapValue);
    
                 outgoingLinks.put(mapKey, objects);

             } 
             else {
                List<Triple> objects = new ArrayList();
                objects.add(mapValue);
                outgoingLinks.put(mapKey, objects);
             }
        }

        return outgoingLinks;
        
    }
    
     public List<String> returnSubjects(String namedGraph) throws RepositoryException, MalformedQueryException, QueryEvaluationException, Exception{

        List<String> subjects = new ArrayList<>();
        
                List<String> namedgraphs = new ArrayList<>();
        namedgraphs.add(namedGraph);
        
        String query = selectAll(namedgraphs);
      
        List<BindingSet> sparqlResults = query(query);
       
        for (BindingSet result : sparqlResults) {
           
            _logger.trace(result.toString());
           
            String value = result.getBinding("s").getValue().stringValue();
            
            subjects.add(value);
        }

        return subjects;
        
    }



    @Override
    public abstract void close() throws IOException;

}
