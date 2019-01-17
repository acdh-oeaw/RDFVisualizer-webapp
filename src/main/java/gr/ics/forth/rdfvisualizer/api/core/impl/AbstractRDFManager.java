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
    
    public String selectAll(){
        
        return "Select * where {?s ?p ?o}";
    
    }
    
    
    public String selectAll(List<String> namedgraphs){
        
        return String.format(
                "Select * \n %s WHERE {?s ?p ?o}",
                namedgraphs.stream().collect(Collectors.joining(">\nFROM <", "FROM <", ">"))
            );
        
    }
    
    public String selectAll(Resource resource){
        
        return String.format(
                "Select * where {<%s> ?p ?o}",
                resource.getURI().toString()               
            );
        
    }
    
    public String selectAll(Resource resource,List<String> namedgraphs){
        
        return String.format(
                "Select * \n%1$sWHERE {<%2$s> ?p ?o}",
                namedgraphs.stream().collect(Collectors.joining(">\nFROM <", "FROM <", ">")),
                resource.getURI().toString()
            );
    }
    
    public String selectAll(String resource){
        
        return String.format(
                "Select * where {<%s> ?p ?o}",
                resource
            );
    }
    
    public String selectAll(String resource,List<String> namedgraphs){
        
        return String.format(
                "Select * \n%1$sWHERE {<%2$s> ?p ?o}",
                namedgraphs.stream().collect(Collectors.joining(">\nFROM <", "FROM <", ">")),
                resource
            );
    }
    
    public String selectLabels(String resource, String labelProperty){
        
        return String.format(
                "Select ?label where {<%1$s> <%2$s> ?label}",
                resource,
                labelProperty                
            );
        
    }
    
    public String selectAllWithLabels(String labelProperty){
        
        return String.format(
                "Select ?s ?p ?o ?slabel ?plabel ?olabel where {?s ?p ?o .\n"
                        + "?s <%s> ?slabel .\n"
                        + "?p <%s> ?plabel .\n"
                        + "?o <%s> ?olabel .\n"
                        + " }",
                labelProperty          
            );
        
    }
    
    public String selectAllWithLabels(String resource, String labelProperty){

        return String.format(
                "Select * where {<%1$s> ?p ?o .\n"
                        + "<%1$s> <%2$s> ?slabel .\n"
                        + "?p <%2$s> ?plabel .\n"
                        + "?o <%2$s> ?olabel .\n"
                        + " }",                
                resource,
                labelProperty
            );
    }
    
    public String selectAllIncomingWithLabelsAndTypes(String resource, Set<String> labelProperties, List<String> urisToExclude, String graph){
        
       return String.format(
               "SELECT * FROM <%4$s> \n"+
               "WHERE\n"+
               "{ {\n"+
               " ?o ?p <%1$s>.\n"+
               "<%1$s>  rdf:type ?stype .\n"+
               "<%1$s>  \n%2$s  ?slabel .\n"+
               "OPTIONAL {?p %2$s ?plabel }.\n"+
               "OPTIONAL {?o %2$s  ?olabel }.\n"+
               "OPTIONAL {?o rdf:type ?otype} .\n"+
               "%3$s} \n"+
               "UNION\n"+
               "{ \n"+
               "?o ?p <%1$s> .\n"+
               "<%1$s>  rdf:type ?stype .\n"+
               "<%1$s>  \n%2$s  ?slabel .\n"+
               " OPTIONAL{?o %2$s  ?olabel }.\n"+
               "OPTIONAL {?p %2$s  ?plabel }.\n"+                  
               "  \n"+
               "FILTER(isLiteral(?o))\n"+
               "%3$s} }\n",                             
             resource,
             labelProperties.stream().collect(Collectors.joining("> | <", "<", ">")),
             urisToExclude.stream().collect(Collectors.joining(">)\n FILTER (?p!= <", " FILTER (?p!= <", ">)")),
             graph
           );
       
   }
        
    public String selectAllOutgoingWithLabelsAndTypes(String resource, Set<String> labelProperties){
       
 
       return String.format(
               
               "SELECT *  \n"+
               "WHERE\n"+
               "{ {\n"+
               "<%1$s> ?p ?o .\n"+
               "<%1$s>  rdf:type ?stype .\n"+
               "OPTIONAL {<%1$s>  %2$s  ?slabel }.\n"+
               "OPTIONAL {?p %2$s ?plabel }.\n"+
               "OPTIONAL {?o %2$s ?olabel }.\n"+
               "OPTIONAL {?o rdf:type ?otype} .\n"+
               "} "+
                "UNION\n"+
               "{ \n"+
               "<%1$s> ?p ?o .\n"+
               "<%1$s>  rdf:type ?stype .\n"+
               "OPTIONAL {<%1$s>  %2$s ?slabel }.\n"+
               "OPTIONAL {?o %2$s ?olabel }.\n"+                              
               "OPTIONAL {?p %2$s ?plabel }.\n"+
               "  \n"+
               "FILTER(isLiteral(?o))\n"+
               " }\n}",
               resource,       
               labelProperties.stream().collect(Collectors.joining("> | <", "<", ">"))
               );
       
   }

    
    public String selectLabel(String resource, String labelProperty) {

        return String.format("Select ?label where {<%1$s> <%2$s> ?label .\n" + " }", resource, labelProperty

        );

    }
    
    public String selectType(String resource) {

        return String.format("Select ?type where {<%s> rdf:type ?type .\n" + " }", resource);

    }
    
    public String selectGraph(String resource) {
        
        return String.format("SELECT ?g WHERE {GRAPH ?g { <%s> ?p ?o }}", resource);
        
    }
    
    public String returnLabel(String resource, String labelProperty)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException, Exception {

        String query = selectLabel(resource, labelProperty);

        List<BindingSet> sparqlResults = query(query);

        String label = "";

        for (BindingSet result : sparqlResults) {

            _logger.trace("LABEL: " + result.toString());

            label = result.getBinding("label").getValue().stringValue();

        }

        return label;

    }
     
    public String returnType(String resource)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException, Exception {

        String query = selectType(resource);

        List<BindingSet> sparqlResults = query(query);

        String label = "";

        for (BindingSet result : sparqlResults) {

            label = result.getBinding("type").getValue().stringValue();

        }

        return label;

    }
    
    public Map<Triple, List<Triple>> returnIncomingLinksWithTypes(String resource, Set<String> labelProperty,
            List<String> urisToExclude) throws Exception {
        

        

        Map<Triple, List<Triple>> outgoingLinks = new HashMap<Triple, List<Triple>>();
        
        //get graph
        String query = selectGraph(resource);        
        List<BindingSet> sparqlResults = query(query);  
        
        if(!sparqlResults.isEmpty()) {
            String graph = sparqlResults.get(0).getBinding("g").getValue().stringValue();


            query = selectAllIncomingWithLabelsAndTypes(resource, labelProperty, urisToExclude, graph);
    
            sparqlResults = query(query);
    
            for (BindingSet result : sparqlResults) {
    
                // System.out.println(result.toString());
    
                Triple mapKey = new Triple();
                Triple mapValue = new Triple();
    
                String key_uri = result.getBinding("p").getValue().stringValue();
                String key_label = "NOLABEL";
                // System.out.println("KEY_URI"+key_uri);
                if (result.getBinding("plabel") != null)
                    key_label = result.getBinding("plabel").getValue().stringValue();
    
                // String key_type = result.getBinding("ptype").getValue().stringValue();
                String key_type = "NOTYPE";
                mapKey.setSubject(key_uri);
                mapKey.setLabel(key_label);
                mapKey.setType(key_type);
    
                String value_label = "NOLABEL";
                String value_type = "NOTYPE";
                String value_uri = result.getBinding("o").getValue().stringValue();
    
                if (result.getBinding("olabel") != null)
                    value_label = result.getBinding("olabel").getValue().stringValue();
    
                if (result.getBinding("otype") != null)
                    value_type = result.getBinding("otype").getValue().stringValue();
                mapValue.setSubject(value_uri);
                mapValue.setLabel(value_label);
                mapValue.setType(value_type);
    
                if (outgoingLinks.containsKey(mapKey)) {
    
                    List<Triple> objects = outgoingLinks.get(mapKey);
    
                    objects.add(mapValue);
    
                    outgoingLinks.put(mapKey, objects);
    
                } else {
                    List<Triple> objects = new ArrayList<Triple>();
                    objects.add(mapValue);
                    outgoingLinks.put(mapKey, objects);
                }
            }
        }

        return outgoingLinks;
    }
    
     public Map<Pair,List<Pair>> returnOutgoingLinks(String resource, String labelProperty) 
             throws RepositoryException, MalformedQueryException, QueryEvaluationException, Exception {

        Map<Pair,List<Pair>> outgoingLinks = new HashMap<Pair,List<Pair>>();
        
        String query = selectAllWithLabels(resource,labelProperty);
      
        List<BindingSet> sparqlResults = query(query);
       
        for (BindingSet result : sparqlResults) {
           
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
                List<Pair> objects = new ArrayList<Pair>();
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
                List<Triple> objects = new ArrayList<Triple>();
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
