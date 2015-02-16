package patmob.plugin.patbase;

import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import patmob.data.ops.ProxyCredentialsDialog;


/**
 *
 * @author Piotr
 */
public class PatBaseHttpClient {
    static DefaultHttpClient httpclient;
    static String userID, password;
    public static boolean isInitialized = false;
    public static final String 
            PATBASE_API_URL = "http://api.patbase.com/",
            QUERY2AP_URL = "PatBaseAPI.asmx/Query2AP",
            QUERY_URL = "PatBaseAPI.asmx/Query",
            GETPATBASEWEEK_URL = "PatBaseAPI.asmx/GetPatbaseWeek",
            GETMEMBER_URL = "PatBaseAPI.asmx/GetMember";
    
    public static String initialize(String patmobProxy, 
            String patbaseUserId, String patbasePassword) {
        String patbaseConnStatus;
        userID = patbaseUserId;
        password = patbasePassword;
        
        //multithreading
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
                new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
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
        
        HttpPost httpPost;
        httpPost = new HttpPost(PATBASE_API_URL + GETPATBASEWEEK_URL);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        try {
            HttpResponse httpResponse = httpclient.execute(httpPost);
            patbaseConnStatus = httpResponse.getStatusLine().toString();
            
            if (httpResponse.getStatusLine().getStatusCode()==200) {
                patbaseConnStatus = handleInitResponse(httpResponse);
            } else if (httpResponse.getStatusLine().getStatusCode()==407) {
                //proxy needs authentication
                NTCredentials ntc = ProxyCredentialsDialog.getNTCredentials();
                httpclient.getCredentialsProvider().setCredentials(
                        AuthScope.ANY, ntc);
                httpResponse = httpclient.execute(httpPost);
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
        Element root;
        StreamSource source;
        String iRes = "";
        isInitialized = true;
        HttpEntity resultEntity = httpResponse.getEntity();
        if (resultEntity!=null) {
            try {
                try (InputStream is = resultEntity.getContent()) {
                    source = new StreamSource(is);
                    root = getRootElement(source);
                    iRes = root.getTextContent();
                }
            } catch (Exception x) {
                System.out.println(
                        "PatBaseHttpClient.handleInitResponse: " + x);
            }
        }
        return iRes;
    }
    
    static Element getRootElement(StreamSource source) {
        Element root = null;
        try {
            DOMResult result = new DOMResult();
            Transformer transformer = 
                    TransformerFactory.newInstance().newTransformer();
            transformer.transform(source, result);
            Document document = (Document) result.getNode();
            root = document.getDocumentElement();
        } catch (TransformerFactoryConfigurationError | TransformerException x) {
            System.out.println("PatBaseHttpClient.getRootElement(HttpEntity): " + x);
        }
        return root;
    }
}
