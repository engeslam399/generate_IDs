package com.egomaa.demo.demo.controller;

import com.egomaa.demo.demo.service.XMLParserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class XMLController {

    private final XMLParserService xmlParserService;

    public XMLController(XMLParserService xmlParserService) {
        this.xmlParserService = xmlParserService;
    }

    @GetMapping("/objects")
    public List<Map<String, String>> getObjects() {
        return xmlParserService.parseXML();
    }
}
