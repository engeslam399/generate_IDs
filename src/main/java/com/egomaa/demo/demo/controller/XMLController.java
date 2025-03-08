package com.egomaa.demo.demo.controller;

import com.egomaa.demo.demo.service.XMLParserService;
import com.egomaa.demo.demo.service.XMLSAXParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class XMLController {

    private final XMLParserService xmlParserService;

    private final XMLSAXParserService xmlSAXParserService;

    @GetMapping("/objects")
    public List<String> getObjects() {
        return xmlParserService.processXml();
    }

    @GetMapping("/objects/sax")
    public List<String> getObjectsUsingSAX() {
        return xmlSAXParserService.processXml();
    }

}
