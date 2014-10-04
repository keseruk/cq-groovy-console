package com.citytechinc.cq.groovyconsole.services

import java.util.Map;

import org.apache.sling.api.SlingHttpServletRequestimport org.apache.sling.api.resource.ResourceResolver;


interface GroovyConsoleService {

    Map<String, String> runScript(SlingHttpServletRequest request)
    
    Map<String, String> runScript(ResourceResolver resourceResolver, String script)

    Map<String, String> saveScript(SlingHttpServletRequest request)
}
