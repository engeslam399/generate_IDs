package com.egomaa.demo.demo.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class XMLSAXParserService {
    public List<String> processXml() {
        List<String> result = new ArrayList<>();
        try {
            // ALL_GUL_CAI2710_1693240216.xml
            // config.xml
            File xmlFile = new File("src/main/resources/ALL_GUL_CAI2710_1693240216.xml"); // Ensure XML exists
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

                    // Generate a unique key dynamically
                    String uniqueKey = generateUniqueKey(className, parameterList, objectList);
                    result.add(uniqueKey);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private String generateUniqueKey(String className, NodeList parameterList, NodeList objectList) {
        // Step 1: Collect all parameters for the current object
        Map<String, String> currentParams = new HashMap<>();
        for (int k = 0; k < parameterList.getLength(); k++) {
            Element paramElement = (Element) parameterList.item(k);
            String paramName = paramElement.getAttribute("name");
            String paramValue = paramElement.getAttribute("value");
            currentParams.put(paramName, paramValue);
        }

        // Step 2: Try to find a unique parameter
        for (Map.Entry<String, String> entry : currentParams.entrySet()) {
            if (isParameterUnique(entry.getKey(), entry.getValue(), objectList)) {
                return className + "_" + entry.getKey() + "_" + entry.getValue();
            }
        }

        // Step 3: Try composite keys of increasing size
        List<String> paramNames = new ArrayList<>(currentParams.keySet());
        for (int comboSize = 2; comboSize <= paramNames.size(); comboSize++) {
            List<String> comboKey = tryCombination(className, currentParams, paramNames, comboSize, objectList);
            if (comboKey != null) {
                return String.join("_", comboKey);
            }
        }

        // Step 4: Fallback to UNKNOWN if no unique key can be generated
        return className + "_UNKNOWN";
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

    private List<String> tryCombination(String className, Map<String, String> currentParams, List<String> paramNames, int comboSize, NodeList objectList) {
        // Generate all combinations of parameters of size comboSize
        List<List<String>> combinations = generateCombinations(paramNames, comboSize);

        for (List<String> combo : combinations) {
            // Build the composite key
            List<String> keyParts = new ArrayList<>();
            keyParts.add(className);
            for (String paramName : combo) {
                keyParts.add(paramName);
                keyParts.add(currentParams.get(paramName));
            }

            // Check if the composite key is unique
            if (isCompositeKeyUnique(combo, currentParams, objectList)) {
                return keyParts;
            }
        }

        return null;
    }

    private List<List<String>> generateCombinations(List<String> paramNames, int comboSize) {
        List<List<String>> combinations = new ArrayList<>();
        generateCombinationsHelper(paramNames, comboSize, 0, new ArrayList<>(), combinations);
        return combinations;
    }

    private void generateCombinationsHelper(List<String> paramNames, int comboSize, int start, List<String> current, List<List<String>> combinations) {
        if (current.size() == comboSize) {
            combinations.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < paramNames.size(); i++) {
            current.add(paramNames.get(i));
            generateCombinationsHelper(paramNames, comboSize, i + 1, current, combinations);
            current.remove(current.size() - 1);
        }
    }

    private boolean isCompositeKeyUnique(List<String> combo, Map<String, String> currentParams, NodeList objectList) {
        int count = 0;
        for (int i = 0; i < objectList.getLength(); i++) {
            Element objectElement = (Element) objectList.item(i);
            NodeList parameterList = objectElement.getElementsByTagName("parameter");

            boolean allMatch = true;
            for (String paramName : combo) {
                boolean paramMatch = false;
                for (int j = 0; j < parameterList.getLength(); j++) {
                    Element parameterElement = (Element) parameterList.item(j);
                    if (paramName.equals(parameterElement.getAttribute("name")) &&
                            currentParams.get(paramName).equals(parameterElement.getAttribute("value"))) {
                        paramMatch = true;
                        break;
                    }
                }
                if (!paramMatch) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) {
                count++;
            }
        }
        return count == 1;
    }
}
