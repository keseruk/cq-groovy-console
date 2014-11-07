package com.citytechinc.cq.groovyconsole.services.impl

import static com.citytechinc.cq.groovyconsole.services.impl.DefaultGroovyConsoleService.PARAMETER_DRYRUN
import static com.citytechinc.cq.groovyconsole.services.impl.DefaultGroovyConsoleService.PARAMETER_FILE_NAME
import static com.citytechinc.cq.groovyconsole.services.impl.DefaultGroovyConsoleService.PARAMETER_SCRIPT
import static com.citytechinc.cq.groovyconsole.services.impl.DefaultGroovyConsoleService.RELATIVE_PATH_SCRIPT_FOLDER

import javax.jcr.RepositoryException

import org.apache.sling.api.adapter.AdapterFactory
import org.apache.sling.api.resource.Resource
import org.apache.sling.api.resource.ResourceResolver
import org.apache.sling.api.resource.ValueMap
import org.osgi.framework.BundleContext

import spock.lang.Shared

import com.citytechinc.aem.groovy.extension.builders.NodeBuilder
import com.citytechinc.aem.prosper.specs.ProsperSpec
import com.citytechinc.cq.groovyconsole.services.ConfigurationService
import com.citytechinc.cq.groovyconsole.services.EmailService
import com.day.cq.commons.jcr.JcrConstants
import com.day.cq.replication.Replicator
import com.day.cq.search.QueryBuilder

class DefaultGroovyConsoleServiceSpec extends ProsperSpec {

    static final def SCRIPT_NAME = "Script"

    static final def SCRIPT_FILE_NAME = "${SCRIPT_NAME}.groovy"

    static final def PATH_FOLDER = "/etc/groovyconsole/$RELATIVE_PATH_SCRIPT_FOLDER"

    static final def PATH_FILE = "$PATH_FOLDER/$SCRIPT_FILE_NAME"

    static final def PATH_FILE_CONTENT = "$PATH_FILE/${JcrConstants.JCR_CONTENT}"

    @Shared consoleService

    @Shared scriptAsString

    @Shared parameterMap

    def setupSpec() {
        consoleService = new DefaultGroovyConsoleService()

        with(consoleService) {
            replicator = Mock(Replicator)
            bundleContext = Mock(BundleContext)
            configurationService = Mock(ConfigurationService)
            queryBuilder = Mock(QueryBuilder)
            emailService = Mock(EmailService)
        }

        this.class.getResourceAsStream("/$SCRIPT_FILE_NAME").withStream { stream ->
            scriptAsString = stream.text
        }

        parameterMap = [(PARAMETER_FILE_NAME): (SCRIPT_NAME), (PARAMETER_SCRIPT): scriptAsString]
    }

    Collection<AdapterFactory> addAdapterFactories() {
        def adapterFactory = new AdapterFactory() {
            @Override
            def <AdapterType> AdapterType getAdapter(Object adaptable, Class<AdapterType> type) {
                def result

                if (adaptable instanceof Resource) {
                    if (type == InputStream) {
                        Resource res = (Resource) adaptable;
                        if ( res.getResourceType() == JcrConstants.NT_FILE) {
                            return res.getChild(JcrConstants.JCR_CONTENT).adaptTo(ValueMap.class).get(JcrConstants.JCR_DATA)
                        } else { //TODO: Implement nt:resource?
                            return null
                        }
                    }
                } else {
                    result = null
                }

                (AdapterType) result
            }
        }

        [adapterFactory]
    }

    def "run script"() {
        setup:
        def script = scriptAsString
        def request = requestBuilder.build {
            parameters = [(PARAMETER_SCRIPT): script, (PARAMETER_DRYRUN): ("false")]
        }

        when:
        def map = consoleService.runScript(request)

        then:
        assertScriptResult(map)
    }

    def "run script at path"() {
        setup:
        new NodeBuilder(session).etc {
            scripts(JcrConstants.NT_FOLDER) {
                script1(JcrConstants.NT_FILE) {
                    "jcr:content"(JcrConstants.NT_RESOURCE, ["jcr:data" : scriptAsString, "jcr:mimeType" : "text/plain"])
                }
            }
        }

        when:
        def map = consoleService.runScriptAtPath(resourceResolver, "/etc/scripts/script1")

        then:
        assertScriptAtPathResult(map)

        cleanup:
        removeAllNodes()
    }


    def "save script"() {
        setup:
        def map = parameterMap
        def request = requestBuilder.build {
            parameters = map
        }

        and:
        nodeBuilder.etc {
            groovyconsole()
        }

        when:
        consoleService.saveScript(request)

        then:
        assertNodeExists(PATH_FOLDER, JcrConstants.NT_FOLDER)
        assertNodeExists(PATH_FILE, JcrConstants.NT_FILE)
        assertNodeExists(PATH_FILE_CONTENT, JcrConstants.NT_RESOURCE, [(JcrConstants.JCR_MIMETYPE):
            "application/octet-stream"])

        assert session.getNode(PATH_FILE_CONTENT).get(JcrConstants.JCR_DATA).stream.text == scriptAsString

        cleanup:
        removeAllNodes()
    }

    def "missing console root node"() {
        setup:
        def map = parameterMap
        def request = requestBuilder.build {
            parameters = map
        }

        when:
        consoleService.saveScript(request)

        then:
        thrown(RepositoryException)
    }

    void assertScriptResult(map) {
        assert !map.executionResult
        assert map.outputText.trim() == "BEER"
        assert !map.stacktraceText
        assert map.runningTime
    }

    void assertScriptAtPathResult(map) {
        assert !map.executionResult
        assert map.outputText.trim() == ""
        assert !map.stacktraceText
        assert map.runningTime
    }
}