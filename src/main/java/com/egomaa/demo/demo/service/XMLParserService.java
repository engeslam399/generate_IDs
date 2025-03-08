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

        // Try combinations of increasing size until a unique key is found
        for (int comboSize = 2; comboSize <= paramCount; comboSize++) {
            List<String> uniqueKey = tryCombination(className, parameterList, objectList, comboSize);
            if (uniqueKey != null) {
                return String.join("_", uniqueKey);
            }
        }

        // If no unique combination is found, return "ANTENNAPORT_UNKNOWN"
        return className.equals("ANTENNAPORT") ? "ANTENNAPORT_UNKNOWN" : className + "_UNKNOWN";
    }

    private List<String> tryCombination(String className, NodeList parameterList, NodeList objectList, int comboSize) {
        // Generate all combinations of parameters of size comboSize
        List<List<Element>> combinations = generateCombinations(parameterList, comboSize);

        for (List<Element> combo : combinations) {
            // Build the composite key
            List<String> keyParts = new ArrayList<>();
            keyParts.add(className);
            for (Element param : combo) {
                keyParts.add(param.getAttribute("name"));
                keyParts.add(param.getAttribute("value"));
            }

            // Check if the composite key is unique
            if (isCompositeKeyUnique(combo, objectList)) {
                return keyParts;
            }
        }

        return null;
    }

    private List<List<Element>> generateCombinations(NodeList parameterList, int comboSize) {
        List<List<Element>> combinations = new ArrayList<>();
        generateCombinationsHelper(parameterList, comboSize, 0, new ArrayList<>(), combinations);
        return combinations;
    }

    private void generateCombinationsHelper(NodeList parameterList, int comboSize, int start, List<Element> current, List<List<Element>> combinations) {
        if (current.size() == comboSize) {
            combinations.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < parameterList.getLength(); i++) {
            current.add((Element) parameterList.item(i));
            generateCombinationsHelper(parameterList, comboSize, i + 1, current, combinations);
            current.remove(current.size() - 1);
        }
    }

    private boolean isCompositeKeyUnique(List<Element> combo, NodeList objectList) {
        int count = 0;
        for (int i = 0; i < objectList.getLength(); i++) {
            Element objectElement = (Element) objectList.item(i);
            NodeList parameterList = objectElement.getElementsByTagName("parameter");

            boolean allMatch = true;
            for (Element comboParam : combo) {
                boolean paramMatch = false;
                for (int j = 0; j < parameterList.getLength(); j++) {
                    Element parameterElement = (Element) parameterList.item(j);
                    if (comboParam.getAttribute("name").equals(parameterElement.getAttribute("name")) &&
                            comboParam.getAttribute("value").equals(parameterElement.getAttribute("value"))) {
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
