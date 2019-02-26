package gr.ics.forth.rdfvisualizer.api.core.utils;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import gr.ics.forth.rdfvisualizer.webapp.GetConfigProperties;

/*
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
public class JsonTools {
    
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

    public static final synchronized JSONObject createJsonFile(Map<Triple, List<Triple>> outgoingLinks, String subjectLabel, String subjectType, String subject, boolean invert)
            throws RepositoryException, MalformedQueryException, QueryEvaluationException {


        JSONObject subjectlist = new JSONObject();
        subjectlist.put("type", subjectType);
        subjectlist.put("label", subjectLabel);
        subjectlist.put("subject", subject);

        JSONObject result = new JSONObject();
        JSONArray objects = new JSONArray();

        GetConfigProperties pred_app = new GetConfigProperties();
        Properties pred_props = pred_app.getConfig("predicates.properties");

        for(Map.Entry<Triple, List<Triple>> entry : outgoingLinks.entrySet()) {

            for (Triple valueTriple : entry.getValue()) {

                JSONObject object = new JSONObject();
                String propval = pred_props.getProperty((entry.getKey().getLabel()).replaceAll(" ", "_"));

                if ((propval == null) || propval.equals("")) {
                    object.put("predicate", entry.getKey().getLabel());
                } 
                else {
                    object.put("predicate", pred_props.getProperty((entry.getKey().getLabel()).replaceAll(" ", "_")));
                }
                object.put("predicate_uri", entry.getKey().getSubject());
                object.put("predicate_type", entry.getKey().getType());
                object.put("label", valueTriple.getLabel());
                object.put("uri", valueTriple.getSubject());
                object.put("type", valueTriple.getType());
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
    
    public static final synchronized JSONObject mergeJson(JSONObject o1, JSONObject o2, String subjectLabel, String subjectType, String subject)
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

}
