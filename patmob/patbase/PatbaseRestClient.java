package patmob.patbase;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.StringTokenizer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import patmob.data.ops.ProxyCredentialsDialog;

/**
 * 
 * @author Piotr
 */
public class PatbaseRestClient {
    static DefaultHttpClient httpclient;
    static String userID, password;
    public static boolean isInitialized = false;
    static URIBuilder uriBuilder;
            
    public static String initialize(String patmobProxy, 
            String patbaseUserId, String patbasePassword) {
        String patbaseConnStatus;
        userID = patbaseUserId;
        password = patbasePassword;
        
        //multithreading
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
                new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
        PoolingClientConnectionManager cm = 
                new PoolingClientConnectionManager(schemeRegistry);
        httpclient = new DefaultHttpClient(cm);
        
        if (patmobProxy!=null) {
            StringTokenizer st = new StringTokenizer(patmobProxy, ":");
            HttpHost proxy = new HttpHost(
                    st.nextToken(), Integer.parseInt(st.nextToken()));
            httpclient.getParams().setParameter(
                    ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
        
        uriBuilder = new URIBuilder()
                .setScheme("https")
                .setHost("www.patbase.com")
                .setPath("/rest/api.php");
        
        HttpGet httpGet = new HttpGet(getUri("login",
                new BasicNameValuePair("userid", userID),
                new BasicNameValuePair("password", password)));
        try {
            HttpResponse httpResponse = httpclient.execute(httpGet);
            patbaseConnStatus = httpResponse.getStatusLine().toString();
            
            if (httpResponse.getStatusLine().getStatusCode()==200) {
                patbaseConnStatus = handleInitResponse(httpResponse);
            } else if (httpResponse.getStatusLine().getStatusCode()==407) {
                //proxy needs authentication
                NTCredentials ntc = ProxyCredentialsDialog.getNTCredentials();
                httpclient.getCredentialsProvider().setCredentials(
                        AuthScope.ANY, ntc);
                httpResponse = httpclient.execute(httpGet);
                patbaseConnStatus = httpResponse.getStatusLine().toString();
                
                if (httpResponse.getStatusLine().getStatusCode()==200) {
                    patbaseConnStatus = handleInitResponse(httpResponse);
                }
            }
        } catch (IOException ex) {
            patbaseConnStatus = ex.toString();
        }
        return patbaseConnStatus;
    }
    
    private static String handleInitResponse(HttpResponse httpResponse) {
        String status = "Null response";
        JSONObject jOb = getResponseData(httpResponse);
        if (jOb!=null) {
            if (jOb.has("LOGIN_TO_API") && 
                    jOb.getString("LOGIN_TO_API").equals("OK")) {
                isInitialized = true;
            }
            status = jOb.toString();
        }
        return status;

        /*
        Header[] headers = httpResponse.getAllHeaders();
        for (Header h : headers) System.out.println(h.getName() + ": " + h.getValue());
Cache-Control: no-cache, must-revalidate
Content-Type: application/json; charset=utf-8
Expires: Sat, 26 Jul 1997 05:00:00 GMT
Server: Microsoft-IIS/6.0
Version memcacheCOM: 07062011 16:36
X-Powered-By: PHP/5.4.17
Set-Cookie: SessionFarm_GUID=%7B2BF3B796-92F2-4578-862F-E9FB97CA9655%7D
X-Powered-By: ASP.NET
Date: Sat, 09 May 2015 22:04:39 GMT
Connection: close
OGHopCount: 1
Set-Cookie: OGSession=2114025707; path=/
Set-Cookie: visid_incap_9867=UQwH/IOrRmaFsvC3oxo3X3WETlUAAAAAQUIPAAAAAABsLH1Gio514LSoac7GOznk; expires=Mon, 08 May 2017 14:12:30 GMT; path=/; Domain=.patbase.com
Set-Cookie: nlbi_9867=TVo5XBa7yhRY+I+irjd4awAAAABKSd0W/Y1Nem4/yzIHP3rG; path=/; Domain=.patbase.com
Set-Cookie: incap_ses_221_9867=0mplUaTn0RiS52Sk8iYRA3aETlUAAAAAmnjkq4B7yPhR26fL1RIKGQ==; path=/; Domain=.patbase.com
X-Iinfo: 10-176778394-176778505 NNNN CT(82 249 0) RT(1431209076890 201) q(0 0 4 0) r(4 11) U5
X-CDN: Incapsula        
        // org.apache.http.client.protocol.ResponseProcessCookies
        // Response interceptor that populates the current CookieStore 
        // with data contained in response cookies received in the given the HTTP response.
        */
    }
    
    public static JSONObject getResponseData(HttpResponse httpResponse) {
        JSONObject jOb = null;
        HttpEntity resultEntity = httpResponse.getEntity();
        if (resultEntity!=null) {
            try {
                StringBuilder sb = new StringBuilder();
                try (InputStream is = resultEntity.getContent()) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(is));
                    String line;
                    while ((line=br.readLine())!=null) {
                        sb.append(line);
                    }
                    String s = sb.toString();
                    //PatBase crap (?)
                    s = s.substring(s.indexOf("{"), s.length());
                    jOb = new JSONObject(s);
                }
            } catch (Exception x) {
                System.out.println("PatbaseRestClient.getResponseData: " + x);
            }
        }        
        return jOb;
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
            HttpGet httpGet = new HttpGet(getUri("query", 
                    new BasicNameValuePair("query", query)));
            HttpResponse httpResponse = httpclient.execute(httpGet);
            jOb = getResponseData(httpResponse);
            
            System.out.println(jOb.toString());
            
            String qKey = jOb.getString("QueryKey");
            if (to==null) {
                to = jOb.getString("Results");
            }
            
            httpGet = new HttpGet(getUri("searchresults",
                    new BasicNameValuePair("querykey", qKey),
                    new BasicNameValuePair("from", from),
                    new BasicNameValuePair("to", to),
                    new BasicNameValuePair("sortorder", "2")));                 //priority date desc
            httpResponse = httpclient.execute(httpGet);
            jOb = getResponseData(httpResponse);
            
        } catch (Exception x) {
            System.out.println("PatbaseRestClient.query: " + x);
        }
        return jOb;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // supress invalid cookie warnings
            /*
            May 10, 2015 7:11:29 AM org.apache.http.client.protocol.ResponseProcessCookies processCookies
            WARNING: Invalid cookie header: "Set-Cookie: visid_incap_9867=yW0KWQVPQcKShJxY968up9U8T1UAAAAAQUIPAAAAAABJzIC+iidT+3d5ux4oz1cc; expires=Tue, 09 May 2017 01:37:46 GMT; path=/; Domain=.patbase.com". Invalid 'expires' attribute: Tue, 09 May 2017 01:37:46 GMT
            */
            System.setErr(new PrintStream("patbase_cookies.txt"));
        } catch (FileNotFoundException ex) {
            System.out.println("***setErr: " + ex);
        }
        
        System.out.println(
                "patbaseConnStatus: " + initialize(
                        null, "piotr.masiakowski@sanofi.com", "ip4638"));
        
        String query = "UE=1522 and tac=(FGF21)";
        System.out.println(query(query, "1", null).toString(2));
    }
    
    public static URI getUri(String method, NameValuePair... params) {
        URI uri = null;
        uriBuilder.setParameter("method", method);
        for (NameValuePair param : params) {
            uriBuilder.setParameter(param.getName(), param.getValue());
        }
        try {
            uri = uriBuilder.build();
        } catch (URISyntaxException ex) {
            System.out.println("PatbaseRestClient.getUri: " + ex);
        }
        return uri;
    }
}
