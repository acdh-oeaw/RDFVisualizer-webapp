/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ics.forth.rdfvisualizer.webapp;

import gr.ics.forth.rdfvisualizer.api.core.impl.AbstractRDFManager;
import gr.ics.forth.rdfvisualizer.api.core.impl.BlazegraphManager;
import gr.ics.forth.rdfvisualizer.api.core.impl.RDFfileManager;
import gr.ics.forth.rdfvisualizer.api.core.impl.TripleStoreManagerWorking;
import gr.ics.forth.rdfvisualizer.api.core.utils.Triple;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.util.BasicAuthentication;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import org.json.JSONArray;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cpetrakis
 */
public class GetData extends HttpServlet {
    private final static Logger _logger = LoggerFactory.getLogger(GetData.class);

    /**
     * ************************ Create Json File *****************************
     * @param outgoingLinks
     * @param subjectLabel
     * @param subjectType
     * @param subject
     * @return
     * @throws org.openrdf.repository.RepositoryException
     * @throws org.openrdf.query.MalformedQueryException
     * @throws org.openrdf.query.QueryEvaluationException
     */
    public JSONObject createJsonFile(Map<Triple, List<Triple>> outgoingLinks, String subjectLabel, String subjectType, String subject)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException {

        Iterator<Map.Entry<Triple, List<Triple>>> iter = outgoingLinks.entrySet().iterator();

        JSONObject subjectlist = new JSONObject();
        subjectlist.put("type", subjectType);
        subjectlist.put("label", subjectLabel);
        subjectlist.put("subject", subject);

        JSONObject result = new JSONObject();
        JSONArray objects = new JSONArray();

        GetConfigProperties pred_app = new GetConfigProperties();
        Properties pred_props = pred_app.getConfig("predicates.properties");

        while (iter.hasNext()) {

            Map.Entry<Triple, List<Triple>> entry = iter.next();
            List<Triple> l = new ArrayList<Triple>();

            l = entry.getValue();

            for (int i = 0; i < l.size(); i++) {

                JSONObject object = new JSONObject();
                String propval = pred_props.getProperty((entry.getKey().getLabel()).replaceAll(" ", "_"));

                if ((propval == null) || propval.equals("")) {
                    object.put("predicate", entry.getKey().getLabel());
                } else {
                    object.put("predicate", pred_props.getProperty((entry.getKey().getLabel()).replaceAll(" ", "_")));
                }
                object.put("predicate_uri", entry.getKey().getSubject());
                object.put("predicate_type", entry.getKey().getType());
                object.put("label", entry.getValue().get(i).getLabel());
                object.put("uri", entry.getValue().get(i).getSubject());
                object.put("type", entry.getValue().get(i).getType());
                objects.put(object);
            }
            result.put("Objects", objects);
        }

        result.put("Subject", subjectlist);
        return result;
    }
    
    /**
     * ************************ Create Inverse properties Json File *****************************
     * @param outgoingLinks
     * @param subjectLabel
     * @param subjectType
     * @param subject
     * @return
     * @throws org.openrdf.repository.RepositoryException
     * @throws org.openrdf.query.MalformedQueryException
     * @throws org.openrdf.query.QueryEvaluationException
     */

    public JSONObject createInvertJsonFile(Map<Triple, List<Triple>> outgoingLinks, String subjectLabel, String subjectType, String subject)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException {

        Iterator<Map.Entry<Triple, List<Triple>>> iter = outgoingLinks.entrySet().iterator();

        JSONObject subjectlist = new JSONObject();
        subjectlist.put("type", subjectType);
        subjectlist.put("label", subjectLabel);
        subjectlist.put("subject", subject);

        JSONObject result = new JSONObject();
        JSONArray objects = new JSONArray();

        GetConfigProperties pred_app = new GetConfigProperties();
        Properties pred_props = pred_app.getConfig("predicates.properties");

        while (iter.hasNext()) {

            Map.Entry<Triple, List<Triple>> entry = iter.next();

            List<Triple> l = new ArrayList<Triple>();

            l = entry.getValue();

            for (int i = 0; i < l.size(); i++) {

                JSONObject object = new JSONObject();
                String propval = pred_props.getProperty((entry.getKey().getLabel()).replaceAll(" ", "_"));

                if ((propval == null) || propval.equals("")) {
                    object.put("predicate", entry.getKey().getLabel());
                } else {
                    object.put("predicate", pred_props.getProperty((entry.getKey().getLabel()).replaceAll(" ", "_")));
                }
                object.put("predicate_uri", entry.getKey().getSubject());
                object.put("predicate_type", entry.getKey().getType());
                object.put("label", entry.getValue().get(i).getLabel());
                object.put("uri", entry.getValue().get(i).getSubject());
                object.put("type", entry.getValue().get(i).getType());
                object.put("invert", true);
                objects.put(object);
            }
            result.put("Objects", objects);
        }

        result.put("Subject", subjectlist);
        return result;
    }

    /**
     * ************************ Merge two Json objects into one *****************************
     * @param o1
     * @param o2
     * @param subjectLabel
     * @param subjectType
     * @param subject
     * @return
     * @throws org.openrdf.repository.RepositoryException
     * @throws org.openrdf.query.MalformedQueryException
     * @throws org.openrdf.query.QueryEvaluationException 
     */
    
    public JSONObject mergeJson(JSONObject o1, JSONObject o2, String subjectLabel, String subjectType, String subject)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException {

        JSONObject result = new JSONObject();

        JSONObject subjectlist = new JSONObject();
        subjectlist.put("type", subjectType);
        subjectlist.put("label", subjectLabel);
        subjectlist.put("subject", subject);

        JSONArray objects1 = new JSONArray();
        if (o1.has("Objects")) {
            objects1 = o1.getJSONArray("Objects");
        }

        JSONArray objects2 = new JSONArray();

        if (o2.has("Objects")) {
            objects2 = o2.getJSONArray("Objects");
        }

        JSONArray objs = new JSONArray();

        for (int i = 0; i < objects1.length(); i++) {
            objs.put(objects1.get(i));
        }
        for (int i = 0; i < objects2.length(); i++) {
            objs.put(objects2.get(i));
        }

        if (objs.length() > 0) {
            result.put("Objects", objs);
        }
        result.put("Subject", subjectlist);

        return result;

    }
       
    /**
     * ************************** Virtuoso Case ******************************
     * @param resource
     * @return
     * @throws org.openrdf.repository.RepositoryException
     * @throws org.openrdf.query.MalformedQueryException
     * @throws org.openrdf.query.QueryEvaluationException
     */
    public JSONObject virtuosocase(String resource) throws RepositoryException, MalformedQueryException, QueryEvaluationException {

        GetConfigProperties app = new GetConfigProperties();
        Properties props = app.getConfig("config.properties");

        String db_url = props.getProperty("db_url").trim();
        String db_port = props.getProperty("db_port").trim();
        String db_username = props.getProperty("db_username").trim();
        String db_password = props.getProperty("db_password").trim();
        String db_graphname = props.getProperty("db_graphname").trim();        
        String label = props.getProperty("schema_label").trim();
        String pref_labels = props.getProperty("pref_labels").trim();
        
       
        String exclude_inverse = props.getProperty("exclude_inverse").trim();        
        List<String> exclusions = Arrays.asList(exclude_inverse.split("\\s*,\\s*"));

        String subject = resource;

        //TripleStoreManager manager = new TripleStoreManager();
        TripleStoreManagerWorking manager = new TripleStoreManagerWorking();
        manager.openConnectionToVirtuoso(db_url, db_port, db_username, db_password);

        subject = subject.replaceAll(" |\\r|\\n|\"", "");

        if (subject.length() > 2000) {
            subject = subject.substring(0, 500);
        }
       
        String subjectLabel = manager.returnLabel(subject, label);
        String subjectType = manager.returnType(subject);
                       
        String[] pref_lbls = pref_labels.split(",");

        if ((subjectLabel.isEmpty()) && (pref_lbls.length > 0)) {
            subjectLabel = manager.returnLabel(subject, pref_lbls[0]);
        }

        Map<Triple, List<Triple>> outgoingLinks = new HashMap<Triple, List<Triple>>();

        Set<String> labels = new TreeSet();

        labels.add(label);
        if (pref_lbls[0].length() > 0) {
            for (int i = 0; i < pref_lbls.length; i++) {                
                labels.add(pref_lbls[i]);
            }
        }
        
        outgoingLinks = manager.returnOutgoingLinksWithTypes(subject, labels, db_graphname);  
        JSONObject result = createJsonFile(outgoingLinks, subjectLabel, subjectType, subject);

      //  Map<Triple, List<Triple>> incomingLinks = new HashMap<Triple, List<Triple>>();
      //  incomingLinks = manager.returnIncomingLinksWithTypes(subject, labels, db_graphname, exclusions);
      //  JSONObject result0 = createInvertJsonFile(incomingLinks, subjectLabel, subjectType, subject);

        //merge json shows inverse labels otherwise only outgoing links 
        //return mergeJson(result, result0, subjectLabel, subjectType, subject);//result;
        return result;

    }
    
    /**
     * **************************Blazegraph Case ***************************
     * @param resource
     *
     * @return
     * @throws Exception 
     * @throws org.openrdf.repository.RepositoryException
     * @throws org.openrdf.query.MalformedQueryException
     * @throws org.openrdf.query.QueryEvaluationException
     */
    
    public JSONObject blazegraphcase(String resource) throws Exception{
       

        String subject = resource;

        GetConfigProperties app = new GetConfigProperties();
        Properties props = app.getConfig("config.properties");

        String blazegraph_url = props.getProperty("blazegraph_url").trim();
        String blazegraph_user = props.getProperty("blazegraph_user").trim();
        String blazegraph_password = props.getProperty("blazegraph_password").trim();
        
        String label = props.getProperty("schema_label").trim();
        String pref_labels = props.getProperty("pref_labels").trim();
        
        JSONObject result = null;
 
        try(BlazegraphManager manager = new BlazegraphManager(blazegraph_url, blazegraph_user, blazegraph_password)){
            subject = subject.replaceAll(" |\\r|\\n|\"", "");

            if (subject.length() > 2000) {
                subject = subject.substring(0, 500);
            }

            String subjectLabel = manager.returnLabel(subject, label);
            String subjectType = manager.returnType(subject);

            String[] pref_lbls = pref_labels.split(",");
            

            if ((subjectLabel.isEmpty()) && (pref_lbls.length > 0)) {
                subjectLabel = manager.returnLabel(subject, pref_lbls[0]);
            }
            
            if (subjectLabel.isEmpty()) {
                subjectLabel = "no label";
            }
                    
            Map<Triple, List<Triple>> outgoingLinks = new HashMap<Triple, List<Triple>>();
            // Map<Triple, List<Triple>> incomingLinks = new HashMap<Triple, List<Triple>>();

            Set<String> labels = new TreeSet<String>();

            labels.add(label);
            if (pref_lbls[0].length() > 0) {
                for (int i = 0; i < pref_lbls.length; i++) {
                    labels.add(pref_lbls[i]);
                }
            }

            outgoingLinks = manager.returnOutgoingLinksWithTypes(subject, labels); 

            result = createJsonFile(outgoingLinks, subjectLabel, subjectType, subject);

            
        }
        catch (Exception ex) {
            throw ex;
        }




        return result;
    }


    /**
     * **************************File Case******************************
     *
     * @param resource
     * @param filename
     * @return
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     * @throws Exception
     */
    
    public JSONObject filecase(String resource, String filename) throws RepositoryException, MalformedQueryException, QueryEvaluationException, Exception {

        GetConfigProperties app = new GetConfigProperties();
        Properties props = app.getConfig("config.properties");

        String defaultfolder = props.getProperty("default_folder").trim();
        String filepath = props.getProperty("filename").trim();
        String label = props.getProperty("schema_label").trim();
        String pref_labels = props.getProperty("pref_labels").trim();

        String subject = resource;

        RDFfileManager manager = new RDFfileManager();
        File inputFile = new File(filepath);

        if (inputFile.exists()) {
            manager.readFile(inputFile, "TURTLE");
        } else {
            filename = defaultfolder + System.getProperty("file.separator") + filename;
            inputFile = new File(filename);
            if (inputFile.exists()) {
                manager.readFile(inputFile, "TURTLE");
            }
        }

        subject = subject.replaceAll(" |\\r|\\n|\"", "");

        if (subject.length() > 2000) {
            subject = subject.substring(0, 500);
        }

        String subjectLabel = manager.returnLabel(subject, label);
        String subjectType = manager.returnType(subject);

        String[] pref_lbls = pref_labels.split(",");

        if ((subjectLabel.isEmpty()) && (pref_lbls.length > 0)) {
            subjectLabel = manager.returnLabel(subject, pref_lbls[0]);
        }

        Map<Triple, List<Triple>> outgoingLinks = new HashMap<Triple, List<Triple>>();
        // Map<Triple, List<Triple>> incomingLinks = new HashMap<Triple, List<Triple>>();

        Set<String> labels = new TreeSet();

        labels.add(label);
        if (pref_lbls[0].length() > 0) {
            for (int i = 0; i < pref_lbls.length; i++) {                
                labels.add(pref_lbls[i]);
            }
        } 
                

        outgoingLinks = manager.returnOutgoingLinksWithTypes(subject, labels);
        JSONObject result = createJsonFile(outgoingLinks, subjectLabel, subjectType, subject);
                       
        return result;

    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     * @throws org.openrdf.repository.RepositoryException
     * @throws org.openrdf.query.MalformedQueryException
     * @throws org.openrdf.query.QueryEvaluationException
     */
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, RepositoryException, MalformedQueryException, QueryEvaluationException, Exception {
        

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            String resource = request.getParameter("resource");
            String filename = request.getParameter("folderpath");

            GetConfigProperties app = new GetConfigProperties();
            Properties props = app.getConfig("config.properties");

            String database = props.getProperty("database").trim();

            switch (database) {
                case "virtuoso":
                    out.println(virtuosocase(resource));
                    break;
                case "blazegraph":
                    out.println(blazegraphcase(resource));
                    break;
                case "file":
                    out.println(filecase(resource, filename));
                    break;
                default:
                    out.println("check_configuration");
                    break;
            }

        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (RepositoryException ex) {
            _logger.error("", ex);
        } catch (MalformedQueryException ex) {
            _logger.error("", ex);
        } catch (QueryEvaluationException ex) {
            _logger.error("", ex);
        } catch (Exception ex) {
            _logger.error("", ex);
        }
    }
}
