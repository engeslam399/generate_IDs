package com.egomaa.demo.demo.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

@Service
public class XMLParserService {



    public List<String> processXml() {
        List<String> result = new ArrayList<>();
        try {
            File xmlFile = new File("src/main/resources/config.xml"); // Ensure XML exists
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList classList = doc.getElementsByTagName("class");
            for (int i = 0; i < classList.getLength(); i++) {
                Element classElement = (Element) classList.item(i);
                String className = classElement.getAttribute("name");
                NodeList objectList = classElement.getElementsByTagName("object");

                for (int j = 0; j < objectList.getLength(); j++) {
                    Element objectElement = (Element) objectList.item(j);
                    NodeList parameterList = objectElement.getElementsByTagName("parameter");

                    // Step 1: Try to find a unique parameter
                    String uniqueKey = findUniqueParameter(className, parameterList, objectList);

                    // Step 2: If no unique parameter is found, create a composite key
                    if (uniqueKey == null) {
                        uniqueKey = findUniqueCompositeKey(className, parameterList, objectList);
                    }

                    result.add(uniqueKey);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String findUniqueParameter(String className, NodeList parameterList, NodeList objectList) {
        for (int k = 0; k < parameterList.getLength(); k++) {
            Element paramElement = (Element) parameterList.item(k);
            String paramName = paramElement.getAttribute("name");
            String paramValue = paramElement.getAttribute("value");

            if (isParameterUnique(paramName, paramValue, objectList)) {
                return className + "_" + paramName + "_" + paramValue;
            }
        }
        return null;
    }

    private boolean isParameterUnique(String paramName, String paramValue, NodeList objectList) {
        int count = 0;
        for (int i = 0; i < objectList.getLength(); i++) {
            Element objectElement = (Element) objectList.item(i);
            NodeList parameterList = objectElement.getElementsByTagName("parameter");

            for (int j = 0; j < parameterList.getLength(); j++) {
                Element parameterElement = (Element) parameterList.item(j);
                if (paramName.equals(parameterElement.getAttribute("name")) &&
                        paramValue.equals(parameterElement.getAttribute("value"))) {
                    count++;
                }
            }
        }
        return count == 1;
    }

    private String findUniqueCompositeKey(String className, NodeList parameterList, NodeList objectList) {
        int paramCount = parameterList.getLength();
        for (int i = 0; i < paramCount; i++) {
            Element param1 = (Element) parameterList.item(i);
            String param1Name = param1.getAttribute("name");
            String param1Value = param1.getAttribute("value");

            for (int j = i + 1; j < paramCount; j++) { // Try pairing with a different parameter
                Element param2 = (Element) parameterList.item(j);
                String param2Name = param2.getAttribute("name");
                String param2Value = param2.getAttribute("value");

                if (isCompositeKeyUnique(param1Name, param1Value, param2Name, param2Value, objectList)) {
                    return className + "_" + param1Name + "_" + param1Value + "_" + param2Name + "_" + param2Value;
                }
            }
        }
        return className + "_UNKNOWN";
    }

    private boolean isCompositeKeyUnique(String param1Name, String param1Value, String param2Name, String param2Value, NodeList objectList) {
        int count = 0;
        for (int i = 0; i < objectList.getLength(); i++) {
            Element objectElement = (Element) objectList.item(i);
            NodeList parameterList = objectElement.getElementsByTagName("parameter");

            boolean param1Match = false, param2Match = false;
            for (int j = 0; j < parameterList.getLength(); j++) {
                Element parameterElement = (Element) parameterList.item(j);
                String name = parameterElement.getAttribute("name");
                String value = parameterElement.getAttribute("value");

                if (name.equals(param1Name) && value.equals(param1Value)) {
                    param1Match = true;
                }
                if (name.equals(param2Name) && value.equals(param2Value)) {
                    param2Match = true;
                }
            }
            if (param1Match && param2Match) {
                count++;
            }
        }
        return count == 1;
    }


}
