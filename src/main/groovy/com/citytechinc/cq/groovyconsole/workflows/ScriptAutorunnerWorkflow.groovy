package com.citytechinc.cq.groovyconsole.workflows

import groovy.util.logging.Slf4j

import org.apache.felix.scr.annotations.Component
import org.apache.felix.scr.annotations.Reference
import org.apache.felix.scr.annotations.Service
import org.apache.sling.api.resource.ResourceResolver
import org.apache.sling.api.resource.ResourceResolverFactory

import com.adobe.granite.workflow.WorkflowException
import com.adobe.granite.workflow.WorkflowSession
import com.adobe.granite.workflow.exec.WorkItem
import com.adobe.granite.workflow.exec.WorkflowProcess
import com.adobe.granite.workflow.metadata.MetaDataMap
import com.citytechinc.cq.groovyconsole.services.GroovyConsoleService

@Service(WorkflowProcess)
@Component
@Slf4j("LOG")
class ScriptAutorunnerWorkflow implements WorkflowProcess{

    @Reference
    private ResourceResolverFactory resourceResolverFactory

    @Reference
    private GroovyConsoleService groovyConsoleService

    @Override
    public void execute(WorkItem item, WorkflowSession session, MetaDataMap args)
    throws WorkflowException {
        def workflowData = item.getWorkflowData()
        def script = workflowData.getPayload().toString()
        LOG.info "ScriptAutorunnerWorkflow payload type = {}", script

        try {
            def resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null)
            groovyConsoleService.runScriptAtPath(resourceResolver, script)
            resourceResolver.close()
        } catch (Exception e) {
            throw new WorkflowException("Could not auto-run script: " + script, e);
        }
    }
}
