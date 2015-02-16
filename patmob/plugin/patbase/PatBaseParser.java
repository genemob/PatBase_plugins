package patmob.plugin.patbase;

import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import patmob.data.NetFeature;
import patmob.data.PatentCollectionList;
import patmob.data.PatentDocument;
import patmob.data.PatentTreeNode;

/**
 * 12/28/2014
 * @author Piotr
 */
public class PatBaseParser {
    //URL for accessing individual doc in PatBase Express Family Explorer
    String docURL_pn = "https://www.patbase.com/patbase_xslt/index.php?pn=",
            docURL_kd = "&kd=", docURL_fn = "&id=";
    static XPath xPath = null;
    PatBaseQueryResultFormat resultFormat;
    
    public PatBaseParser() {
        if (xPath==null) {
            xPath = XPathFactory.newInstance().newXPath();
        }
    }
    
    public PatBaseParser(PatBaseQueryResultFormat format) {
        this();
        resultFormat = format;
    }
    
    /**
     * 
     * @param httpResponse
     * @return String[] with the following data:
     * memberData[0]: patent title,
     * memberData[1]: patent assignee,
     * memberData[2]: ID to retrieve 1st image in claims (chemical structure),
     * memberData[3]: patent abstract,
     */
    public String[] parseGetMemberResponse(HttpResponse httpResponse) {
//        String tit = "", ass = "", imgID = "", abs = "";
        String[] memberData = null;
        HttpEntity resultEntity = httpResponse.getEntity();
        if (resultEntity!=null) {
            try {
                //first get the PatBase "XML" as a text node
                try (InputStream is = resultEntity.getContent()) {
                    StreamSource source = new StreamSource(is);
                    Element root = PatBaseHttpClient.getRootElement(source);
                    String xmlString = root.getTextContent();
                    //then get the records XML element
                    StringReader sr = new StringReader(xmlString);
                    source = new StreamSource(sr);
                    Element elRoot = PatBaseHttpClient.getRootElement(source);
                    Element title = (Element) xPath.evaluate(
                            "//Title", elRoot, XPathConstants.NODE),
                            assignee = (Element) xPath.evaluate(
                            "//PatentApplicantsNS", elRoot, XPathConstants.NODE),
                            claims = (Element) xPath.evaluate(
                            "//Claims", elRoot, XPathConstants.NODE),
                            absNode = (Element) xPath.evaluate(
                            "//Abstract", elRoot, XPathConstants.NODE);
                    memberData = new String[4];
                    memberData[0] = title.getTextContent();
                    memberData[1] = assignee.getTextContent();
                    
                    String claimsTxt = claims.getTextContent();
                    int start = claimsTxt.indexOf("[FTIMG");
                    if (start>-1) {
                        int end = claimsTxt.indexOf("]", start);
                        memberData[2] = claimsTxt.substring(start+7, end);
                    }
                    
                    memberData[3] = absNode.getTextContent();
                }
            } catch (Exception x) {
                System.out.println("PatBaseParser.parseGetMemberResponse" + x);
            }
        }
        
        return memberData;
    }
    
    /**
     * Parse the response from "Query" operation request.
     * @param httpResponse
     * @return 
     */
    public PatentTreeNode parseQueryResponse(HttpResponse httpResponse) {
        
        PatentCollectionList queryResults = new PatentCollectionList(
                "PatBase Records");
        HttpEntity resultEntity = httpResponse.getEntity();
        if (resultEntity!=null) {
            Element records = getRecordsElement(resultEntity);
            if (records==null) {
                queryResults.setDescription("* NO RECORDS *");
                return queryResults;
            }
            
            String familyExpression = "//Family",
                    patentExpression = "FamilyInformation/FamilyMember",
                    updateExpression = "UEHit",
                    pn_kdExpression = "PN",
                    pdExpression = "PD";
            try {
                queryResults.setDescription("Total records: " +
                        records.getAttribute("totalRecords"));
                NodeList familyNodes = (NodeList) xPath.evaluate(
                        familyExpression, records, XPathConstants.NODESET);
                
                //EXTRACT FAMILIES
                for (int i=0; i<familyNodes.getLength(); i++) {
                    Element famElement = (Element) familyNodes.item(i);
                    String famNumber = famElement.getAttribute("number");
                    String netPN, netKD;
                    NetFeature pbFamily = new NetFeature(
                            "PatBase Family " + famNumber);
                    queryResults.addChild(pbFamily);
                    NodeList pnNodes = (NodeList) xPath.evaluate(
                            patentExpression, famElement, XPathConstants.NODESET);
                    
                    //ANALYZE FAMILY MEMBERS
                    for (int k=0; k<pnNodes.getLength(); k++) {
                        String updateHit = xPath.evaluate(updateExpression, pnNodes.item(k));
                        PatentDocument patent = new PatentDocument(
                                xPath.evaluate(pn_kdExpression, pnNodes.item(k))
                                + " " +
                                xPath.evaluate(pdExpression, pnNodes.item(k)));
                        if (updateHit.equals("True")) {
                            patent.setHilite(PatentTreeNode.HILITE_RED);
                        }
                        int pd = Integer.parseInt(patent.getPublicationDate());
                        
                        if ((resultFormat.allCountries() || 
                                resultFormat.selectCountries().contains(
                                patent.getCountry())) &&
                                (resultFormat.allDates() || 
                                pd >= Integer.parseInt(resultFormat.afterDate()))) {
                        
                            
                        
                        
                    
//                        //TODO: better way to determine patent inclusion
//                        //TODO: NetFeature link to LATEST publication
//                        if (pd >= 20141215 && (
//                                patent.getCountry().equals("US") ||
//                                patent.getCountry().equals("EP") ||
//                                patent.getCountry().equals("WO"))) {
                        pbFamily.addChild(patent);
                        netPN = patent.getCountry() + patent.getNumber();
                        netKD = patent.getKindCode();
                        
                        pbFamily.setDescription(docURL_pn + netPN + 
                                docURL_kd + netKD + docURL_fn + famNumber);
//                        queryResults.addChild(pbFamily);

                        }
                    }
                    
//                    pbFamily.setDescription(docURL_pn + netPN + 
//                            docURL_kd + netKD + docURL_fn + famNumber);
                }
            } catch (XPathExpressionException | DOMException x) {
                System.out.println("PatBaseAPI.parseQueryResponse: " + x);
            } catch (Exception ex) {ex.printStackTrace();}
        }
        return queryResults;
    }
    
    private Element getRecordsElement(HttpEntity resultEntity) {
        Element records = null, root;
        StreamSource source;
        String totalRecords;
        try {
            //first get the PatBase "XML" as a text node
            try (InputStream is = resultEntity.getContent()) {
                source = new StreamSource(is);
                root = PatBaseHttpClient.getRootElement(source);
            }
            String xmlString = root.getTextContent();
            totalRecords = xmlString.substring(
                    xmlString.indexOf("<RESULTS>")+9, 
                    xmlString.indexOf("</RESULTS>"));
            if (!totalRecords.equals("0")) {
                //then get the records XML element
                StringReader sr = new StringReader(
                        xmlString.substring(xmlString.indexOf("</RESULTS>")+10));
                source = new StreamSource(sr);
                records = PatBaseHttpClient.getRootElement(source);
                records.setAttribute("totalRecords", totalRecords);
            }
        } catch (Exception ex) {
            System.out.println("PatBaseAPI.getRecordsElement: " + ex);
        }
        
        return records;
    }

    
//////    private static PatentTreeNode parseQuery2ApResponse(
//////            HttpResponse httpResponse) {
//////        PatentCollectionList queryResults = new PatentCollectionList(
//////                "PatBase Records");
//////        HttpEntity resultEntity = httpResponse.getEntity();
//////        if (resultEntity!=null) {
//////            String familyExpression = "//FN",
//////                    patentExpression = "PUB/PN";
//////            try {
//////                Element records = getRecordsElement(resultEntity);
//////                queryResults.setDescription("Total records: " +
//////                        records.getAttribute("totalRecords"));
//////                NodeList familyNodes = (NodeList) xPath.evaluate(
//////                        familyExpression, records, XPathConstants.NODESET);
//////                for (int i=0; i<familyNodes.getLength(); i++) {
//////                    Element famElement = (Element) familyNodes.item(i);
//////                    PatentCollectionList pbFamily = new PatentCollectionList(
//////                            "PatBase Family " + famElement.getAttribute("ID"));
//////                    queryResults.addChild(pbFamily);
//////                    NodeList pnNodes = (NodeList) xPath.evaluate(
//////                            patentExpression, famElement, XPathConstants.NODESET);
//////                    for (int k=0; k<pnNodes.getLength(); k++) {
//////                        PatentDocument patent = new PatentDocument(
//////                                pnNodes.item(k).getTextContent());
//////                        pbFamily.addChild(patent);
//////                    }
//////                }
//////            } catch (XPathExpressionException | DOMException x) {
//////                System.out.println("PatBaseAPI.parseQuery2ApResponse: " + x);
//////            }
//////        }
//////        return queryResults;
//////    }
}
