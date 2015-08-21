package patmob.patbase;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Piotr
 */
public class PatbaseRestApi extends PatbaseRestClient{
    public static final String GETWEEK = "GetWeek";
    public static final String GETMONTH = "GetMonth";
    public static final String QUERY = "Query";
    public static final String SEARCHRESULTS = "SearchResults";
    public static final String SEARCHRESULTSBIB = "SearchResultsBIB";
    public static final String SEARCHRESULTSFN = "SearchResultsFN";
    public static final String GETRECORD = "GetRecord";
    public static final String GETMEMBER = "GetMember";
    public static final String GETFAMILY = "GetFamily";
    
    public static Object[][] tableQuery(String query, String from, String to) {
System.out.println(query +" :: "+ from +" :: "+ to);
        Object[][] result;
        JSONObject jOb = query(query, from, to);
        JSONArray famArray = jOb.getJSONArray("Families");
        result = new Object[famArray.length()][];
        for (int i=0; i<famArray.length(); i++) {
            JSONObject o = famArray.getJSONObject(i);
            result[i] = new Object[]{
                false,
                o.getString("Title"),
                o.getString("ProbableAssignee"),
                o.getString("EarliestPubDate"),
                o.getString("PatentNumber"),
                o.getString("Abstract")
            };
        }
        return result;
    }

    /**
     * Run the query, get the QueryKey, and retrieve desired number of hits.
     * @param query
     * @param from
     * @param to - if null, retrieve ALL hits
     * @return
     */
    public static JSONObject query(String query, String from, String to) {
        JSONObject jOb = null;
        try {
            jOb = runMethod(QUERY, new BasicNameValuePair("query", query));
            System.out.println(jOb.toString());
            String qKey = jOb.getString("QueryKey");
            if (to == null) {
                to = jOb.getString("Results");
            }
            jOb = runMethod(SEARCHRESULTS, 
                    new BasicNameValuePair("querykey", qKey), 
                    new BasicNameValuePair("from", from), 
                    new BasicNameValuePair("to", to),
                    new BasicNameValuePair("sortorder", "2"));
        } catch (Exception x) {
            System.out.println("PatbaseRestApi.query: " + x);
        }
        return jOb;
    }
}
