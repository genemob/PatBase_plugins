package patmob.patbase;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.StringTokenizer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
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
    public static final String 
            PATBASE_API_URL = "https://www.patbase.com/rest/api.php?",
            LOGIN_URL_1 = "method=login&userid=",
//            + "piotr.masiakowski@sanofi.com"
            LOGIN_URL_2 = "&password=";
//            + "ip4638";
            
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
        
        HttpGet httpGet = new HttpGet(PATBASE_API_URL +
                LOGIN_URL_1 + userID + LOGIN_URL_2 + password);
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
        String iRes = "hello";
        isInitialized = true;
        
//        Header[] headers = httpResponse.getAllHeaders();
//        for (Header h : headers) System.out.println(h.getName() + ": " + h.getValue());
        /*
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
        */
//        Header header = httpResponse.getFirstHeader("Set-Cookie");
//        System.out.println("Set-Cookie Header: " + header.getValue());
        // org.apache.http.client.protocol.ResponseProcessCookies
        // Response interceptor that populates the current CookieStore 
        // with data contained in response cookies received in the given the HTTP response.
                
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
//                        System.out.println("HttpEntity: " + line);
                    }
                    iRes = sb.toString();
                }
            } catch (Exception x) {
                System.out.println(
                        "PatbaseRestClient.handleInitResponse: " + x);
            }
        }
        return iRes;
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
//        System.out.println(
//                "patbaseConnStatus: " + 
//                PatbaseRestClient.initialize(
//                        null, "piotr.masiakowski@sanofi.com", "ip4638"));
        String s = PatbaseRestClient.initialize(
                null, "piotr.masiakowski@sanofi.com", "ip4638");
        // ***PatBase CRAP***
        // org.json.JSONException: A JSONObject text must begin with '{' at 1 [character 2 line 1]
        s = s.substring(s.indexOf("{"), s.length());
        printJson(s);
        
        try {
            HttpGet httpGet = new HttpGet("https://www.patbase.com/rest/api.php?method=query&query=pcsk9");
            System.out.println("    >>>> EXECUTING: " + httpGet);
            HttpResponse httpResponse = httpclient.execute(httpGet);
            s = PatbaseRestClient.handleInitResponse(httpResponse);
            s = s.substring(s.indexOf("{"), s.length());
            printJson(s);
            
            JSONObject jOb = new JSONObject(s);
            String qKey = jOb.getString("QueryKey");
            String getURL = "https://www.patbase.com/rest/api.php?method=searchresultsBIB&querykey=" + 
                    qKey + "&from=1&to=5";
            getURL = getURL.replace("{", "%7b").replace("}", "%7d");
            httpGet = new HttpGet(getURL);
            System.out.println("    >>>> EXECUTING: " + httpGet);
            httpResponse = httpclient.execute(httpGet);
            s = PatbaseRestClient.handleInitResponse(httpResponse);
            s = s.substring(s.indexOf("{"), s.length());
            printJson(s);
        } catch (Exception x) {System.out.println("main: " + x);}
    }
    
    static void printJson(String json) {
        JSONObject jOb = new JSONObject(json);
        System.out.println(jOb.toString(2));
    }
    
}
