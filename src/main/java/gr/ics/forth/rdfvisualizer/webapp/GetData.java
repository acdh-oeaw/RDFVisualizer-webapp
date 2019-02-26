/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.ics.forth.rdfvisualizer.webapp;


import gr.ics.forth.rdfvisualizer.api.core.impl.AbstractRDFManager;
import gr.ics.forth.rdfvisualizer.api.core.impl.BlazegraphManager;
import gr.ics.forth.rdfvisualizer.api.core.impl.VirtuosoManager;
import gr.ics.forth.rdfvisualizer.api.core.utils.JsonTools;
import gr.ics.forth.rdfvisualizer.api.core.utils.Triple;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cpetrakis
 */
public class GetData extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    private final static Logger _logger = LoggerFactory.getLogger(GetData.class);
    

    private static String _database;    
    private static String _url;
    private static String _user;
    private static String _password;    
    private static String _label;
    private static String _pref_labels ;    
    private static String _exclude_inverse;        
    private static List<String> _exclusions;
    
    static {
        try {
            GetConfigProperties app = new GetConfigProperties();
            Properties props = app.getConfig("config.properties");
    
            _database = props.getProperty("database").trim();
            
            _url = props.getProperty("triplestore_url").trim();
            _user = props.getProperty("triplestore_user").trim();
            _password = props.getProperty("triplestore_password").trim();
            
            _label = props.getProperty("schema_label").trim();
            _pref_labels = props.getProperty("pref_labels").trim();
            
            _exclude_inverse = props.getProperty("exclude_inverse").trim();        
            _exclusions = Arrays.asList(_exclude_inverse.split("\\s*,\\s*"));
        }
        catch(Exception ex) {
            _logger.error("couldn't initialize static varaibles", ex);     
        }
    
        
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

        try (AbstractRDFManager manager = getRDFManager()) {

            String resource = request.getParameter("resource");
            
            String subject = resource.replaceAll(" |\\r|\\n|\"", "");

            if (subject.length() > 2000) {
                subject = subject.substring(0, 500);
            }

            String subjectLabel = manager.returnLabel(subject, _label);
            String subjectType = manager.returnType(subject);

            String[] pref_lbls = _pref_labels.split(",");
            

            if ((subjectLabel.isEmpty()) && (pref_lbls.length > 0)) {
                subjectLabel = manager.returnLabel(subject, pref_lbls[0]);
            }
            
/*            if (subjectLabel.isEmpty()) {
                subjectLabel = "no label";
            }*/
                    
            Map<Triple, List<Triple>> outgoingLinks = new HashMap<Triple, List<Triple>>();
            // Map<Triple, List<Triple>> incomingLinks = new HashMap<Triple, List<Triple>>();

            Set<String> labels = new TreeSet<String>();

            labels.add(_label);
            if (pref_lbls[0].length() > 0) {
                for (int i = 0; i < pref_lbls.length; i++) {
                    labels.add(pref_lbls[i]);
                }
            }

            outgoingLinks = manager.returnOutgoingLinksWithTypes(subject, labels); 

            JSONObject result = JsonTools.createJsonFile(outgoingLinks, subjectLabel, subjectType, subject, false);
            

            Map<Triple, List<Triple>> incomingLinks = new HashMap<Triple, List<Triple>>();
            incomingLinks = manager.returnIncomingLinksWithTypes(subject, labels, _exclusions);
            JSONObject result0 = JsonTools.createJsonFile(incomingLinks, subjectLabel, subjectType, subject, true);

            //merge json shows inverse labels otherwise only outgoing links 
            response.getWriter().println(JsonTools.mergeJson(result, result0, subjectLabel, subjectType, subject));//result;

        }
        catch(Exception ex) {
            _logger.error("", ex);
        }
    }
    
    private AbstractRDFManager getRDFManager() throws Exception{
        switch (_database) {
        case "virtuoso":
            return new VirtuosoManager(_url, _user, _password);

        case "blazegraph":
            return new BlazegraphManager(_url, _user, _password);
        default:
            throw new Exception();
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
