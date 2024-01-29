import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DataProcessingTest {

    @Test
    public void jsonTest() throws IOException {
        File file = new File("src/test/resources/data.json");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonData = objectMapper.readValue(file, JsonNode.class);

        List.of("Corners", "Fouls", "GoalKicks", "ThrowIns", "Goals").forEach(
                e -> System.out.println(e +
                        objectMapper.convertValue(jsonData.get(e).get("Score"), Map.class))

        );

        ((ObjectNode) jsonData).put("FixtureId", 1000);
        ((ObjectNode) jsonData).put("CustomerId", 1);
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        ((ObjectNode) jsonData.get("StartTimes"))
                .put("FirstHalf", formatter.format(ZonedDateTime.now().minusMinutes(30)));
        ((ObjectNode) jsonData.get("StartTimes"))
                .put("SecondHalf", formatter.format(ZonedDateTime.now().minusMinutes(90)));

        System.out.println(jsonData);

    }

    @Test
    public void XMLTest() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, TransformerException {
        File file = new File("src/test/resources/data.xml");

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(file);

        XPath xpath = XPathFactory.newInstance().newXPath();

        //Extract & print to console: (sportsBookReference, transactionId, outcomeId, totalStake)
        List<String> list = List.of("sportsBookReference", "transactionId", "outcomeId", "totalStake");

        for (String s : list) {
            NodeList nodes = (NodeList) xpath.evaluate("//" + s,
                    doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                System.out.println(s + " : " + element.getTextContent());
            }
        }

        //modify and print modified payload
        Element betDescriptionElement = (Element) xpath.evaluate("//betDescription",
                doc, XPathConstants.NODE);
        String randomString = RandomStringUtils.random(100, true, true);
        betDescriptionElement.setTextContent(randomString);

        Element totalStakeElement = (Element) xpath.evaluate("//totalStake",
                doc, XPathConstants.NODE);
        totalStakeElement.setTextContent(String.valueOf(10));

        Element transactionIdElement = (Element) xpath.evaluate("//transactionId",
                doc, XPathConstants.NODE);
        transactionIdElement.setTextContent(String.valueOf(1));

        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        String xmlString = result.getWriter().toString();
        System.out.println(xmlString);
    }

}
