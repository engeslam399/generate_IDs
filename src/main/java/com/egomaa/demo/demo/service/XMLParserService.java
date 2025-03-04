package com.egomaa.demo.demo.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.*;

@Service
public class XMLParserService {

    public List<Map<String, String>> parseXML() {
        List<Map<String, String>> resultList = new ArrayList<>();
        Set<String> uniqueIds = new HashSet<>();

        try {
            // Load and parse XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ClassPathResource("config.xml").getInputStream());
            doc.getDocumentElement().normalize();

            NodeList classNodes = doc.getElementsByTagName("class");

            for (int i = 0; i < classNodes.getLength(); i++) {
                Element classElement = (Element) classNodes.item(i);
                String parentID = classElement.getAttribute("name");

                NodeList objectNodes = classElement.getElementsByTagName("object");

                for (int j = 0; j < objectNodes.getLength(); j++) {
                    Element objectElement = (Element) objectNodes.item(j);
                    NodeList parameters = objectElement.getElementsByTagName("parameter");

                    String objectID = null;
                    StringBuilder compositeID = new StringBuilder(parentID); // Start composite key with class name

                    for (int k = 0; k < parameters.getLength(); k++) {
                        Element param = (Element) parameters.item(k);
                        String paramName = param.getAttribute("name");
                        String paramValue = param.getAttribute("value");

                        String tempObjectID = parentID + "_" + paramName + "_" + paramValue;

                        if (!uniqueIds.contains(tempObjectID)) {
                            objectID = tempObjectID;
                            uniqueIds.add(objectID);
                            break; // Use first unique parameter as object ID
                        }

                        // Append all params to composite key
                        compositeID.append("_").append(paramName).append("_").append(paramValue);
                    }

                    // If no unique ID was found, use composite ID
                    if (objectID == null) {
                        objectID = compositeID.toString();
                        uniqueIds.add(objectID);
                    }

                    // Store the result
                    Map<String, String> objectMap = new HashMap<>();
                    objectMap.put("parentID", parentID);
                    objectMap.put("objectID", objectID);
                    resultList.add(objectMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }
}
