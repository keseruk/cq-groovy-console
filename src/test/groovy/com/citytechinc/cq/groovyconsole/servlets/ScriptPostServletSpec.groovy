package com.citytechinc.cq.groovyconsole.servlets

import com.citytechinc.cq.groovy.extension.services.OsgiComponentService
import com.day.cq.replication.Replicator
import com.day.cq.wcm.api.PageManager
import groovy.json.JsonSlurper
import org.apache.sling.api.SlingHttpServletRequest
import org.apache.sling.api.SlingHttpServletResponse
import org.apache.sling.api.request.RequestParameter
import org.osgi.framework.BundleContext
import spock.lang.Shared

import static com.citytechinc.cq.groovyconsole.servlets.ScriptPostServlet.ENCODING
import static com.citytechinc.cq.groovyconsole.servlets.ScriptPostServlet.SCRIPT_PARAM

class ScriptPostServletSpec extends AbstractGroovyConsoleSpec {

    @Shared servlet

    @Shared script

    @Shared writer

    def setupSpec() {
        servlet = new ScriptPostServlet()

        servlet.session = session
        servlet.resourceResolver = resourceResolver
        servlet.pageManager = Mock(PageManager)
        servlet.replicator = Mock(Replicator)
        servlet.componentService = Mock(OsgiComponentService)
        servlet.bundleContext = Mock(BundleContext)

        script = getScriptAsString("Script")

        writer = new StringWriter()
    }

    def "run script"() {
        setup: "mock request with script parameter"
        def request = mockRequest()
        def response = mockResponse()

        when: "post to servlet"
        servlet.doPost(request, response)

        then: "script is executed"
        assertJsonResponse()
    }

    void assertJsonResponse() {
        def json = new JsonSlurper().parseText(writer.toString())

        assert !json.executionResult
        assert json.outputText == "BEER\n"
        assert !json.stacktraceText
        assert json.runningTime
    }

    def mockRequest() {
        def request = Mock(SlingHttpServletRequest)
        def requestParameter = Mock(RequestParameter)

        requestParameter.getString(ENCODING) >> script
        request.getRequestParameter(SCRIPT_PARAM) >> requestParameter

        request
    }

    def mockResponse() {
        def response = Mock(SlingHttpServletResponse)

        response.writer >> new PrintWriter(writer)

        response
    }
}