package patmob.plugin.patbase;

import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import patmob.data.PatentTreeNode;

/**
 * Handles the querying of PatBase, with the connection details being
 * handled by PatBaseHttpClient.
 * @author Piotr
 */
public final class PatBaseAPI extends PatBaseHttpClient{

    // More sophisticated techiques for handling stale connections:
    // http://stackoverflow.com/questions/10558791/apache-httpclient-interim-error-nohttpresponseexception
    // http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d5e659
    private static HttpResponse submitRequest(HttpPost httpPost, int resubCount) {
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpclient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode()==200) {
                return httpResponse;
            } else {
                System.out.println(
                        "PatBaseAPI.submitRequest httpResponse.getStatusLine(): "
                        + httpResponse.getStatusLine());
            }
        } catch (SocketTimeoutException | NoHttpResponseException sx) {
            //connection went "stale"?
            if (resubCount<3) {
                httpResponse = submitRequest(httpPost, ++resubCount);
                System.out.println("PatBaseAPI RESUBMIT " + resubCount + ": " + sx);
            }
        } catch (Exception ex) {
            System.out.println("PatBaseAPI.submitRequest: " + ex);
        }
        return httpResponse;
    }
    
    /**
     * Performs PatBase Query2AP operation using the provided arguments.
     * @param cmd
     * @param fromRecord
     * @param toRecord
     * @return PatMOb PatentCollectionList of the retrieved PatBase families
     * and their publication members.
     */
//    public static PatentTreeNode query2AP(
//            String cmd, String fromRecord, String toRecord) {
//        PatentTreeNode results = null;
//        HttpPost httpPost = prepareQuery2AP(cmd, fromRecord, toRecord);
//        if (httpPost!=null) {
//            results = submitRequest(httpPost, cmd, 0);
//        } else {
//            System.out.println("PatBaseAPI.query2AP: null HttpPost");
//        }
//        return results;
//    }

    /**
     * Performs PatBase "Query" operation.
     * @param cmd
     * @param fromRecord
     * @param toRecord
     * @param sort
     * @return PatMOb PatentCollectionList of the retrieved PatBase families
     * and their publication members.
     */
    public static PatentTreeNode query(
            String cmd, String fromRecord, String toRecord, String sort,
            PatBaseQueryResultFormat format) {
        
        PatentTreeNode results = null;
        HttpPost httpPost = prepareQueryRequest(cmd, fromRecord, toRecord, sort);
        if (httpPost!=null) {
            HttpResponse httpResponse = submitRequest(httpPost, 0);
            if (httpResponse!=null) {
                PatBaseParser parser = new PatBaseParser(format);                     //20141228: separate parser
                results = parser.parseQueryResponse(httpResponse);
                results.setDescription(
                        results.getDescription() + " for query:\n" + cmd);
            }
        } else {
            System.out.println("PatBaseAPI.query: null HttpPost");
        }
        return results;
    }

    private static HttpPost prepareQueryRequest(
            String cmd, String fromRecord, String toRecord, String sort) {
        HttpPost httpPost = null;
        try {
            httpPost = new HttpPost(PATBASE_API_URL + QUERY_URL);
            StringEntity queryEntity = new StringEntity(
                    "PatbaseCmd=" + cmd     //urlEncode(cmd)
                    + "&UserId=" + userID
                    + "&Password=" + password
                    + "&from_record=" + fromRecord
                    + "&to_record=" + toRecord
                    + "&sort=" + sort);
            httpPost.setEntity(queryEntity);
            httpPost.setHeader(
                    "Content-Type", "application/x-www-form-urlencoded");
        } catch (UnsupportedEncodingException ex) {
            System.out.println("PatBaseAPI.prepareQueryRequest: " + ex);
        }
        return httpPost;
    }
    
    // get chemical structure
    // for cl and desc, "1" is yes, "0" is no
    private static HttpPost prepareGetMemberRequest(
            String fn, String pn_kd, String cl, String desc) {
        HttpPost httpPost = null;
        try {
            httpPost = new HttpPost(PATBASE_API_URL + GETMEMBER_URL);
            StringEntity queryEntity = new StringEntity(
                    "FamilyNumber=" + fn
                    + "&PatentNumKD=" + pn_kd
                    + "&Claims=" + cl
                    + "&Description=" + desc
                    + "&UserId=" + userID
                    + "&Password=" + password);
            httpPost.setEntity(queryEntity);
            httpPost.setHeader(
                    "Content-Type", "application/x-www-form-urlencoded");
        } catch (UnsupportedEncodingException ex) {
            System.out.println("PatBaseAPI.prepareGetMemberRequest: " + ex);
        }
        return httpPost;
    }
    
//    private static HttpPost prepareQuery2AP(
//            String cmd, String fromRecord, String toRecord) {
//        HttpPost httpPost = null;
//        try {
//            httpPost = new HttpPost(PATBASE_API_URL + QUERY2AP_URL);
//            StringEntity queryEntity = new StringEntity(
//                    "PatbaseCmd=" + cmd     //urlEncode(cmd)
//                    + "&UserId=" + userID
//                    + "&Password=" + password
//                    + "&from_record=" + fromRecord
//                    + "&to_record=" + toRecord);
//            httpPost.setEntity(queryEntity);
//            httpPost.setHeader(
//                    "Content-Type", "application/x-www-form-urlencoded");
//        } catch (UnsupportedEncodingException ex) {
//            System.out.println("PatBaseAPI.prepareQuery2AP: " + ex);
//        }
//        return httpPost;
//    }
    
    /**
     * Simple version for making PatBase table.
     * @param fn
     * @param pn_kd
     * @return 
     */
    public static String[] getMember(String fn, String pn_kd) {
        String[] data = null;
        HttpPost httpPost = prepareGetMemberRequest(fn, pn_kd, "1", "0");
        if (httpPost!=null) {
            HttpResponse httpResponse = submitRequest(httpPost, 0);
            if (httpResponse!=null) {
                PatBaseParser parser = new PatBaseParser();
                
                data = parser.parseGetMemberResponse(httpResponse);
//                for (int i=0; i<data.length; i++) {
//                    System.out.println(data[i]);
//                }
            }
        }

        return data;
    }
    
    public static void main(String args[]) {
        System.out.println(PatBaseAPI.initialize(
                        null, "sapmas", "765sanofi245"));
        // get member
        HttpPost httpPost = prepareGetMemberRequest("50817575", "EP2816034A2", "1", "0");
//        HttpPost  = prepareQueryRequest(cmd, fromRecord, toRecord, sort);
        if (httpPost!=null) {
            HttpResponse httpResponse = submitRequest(httpPost, 0);
            if (httpResponse!=null) {
                PatBaseParser parser = new PatBaseParser();
                
                String[] data = parser.parseGetMemberResponse(httpResponse);
                for (int i=0; i<data.length; i++) {
                    System.out.println(data[i]);
                }
            }
        }
    }
    
}
