package org.eclipse.xpanse.modules.nflow.definition;

import static io.nflow.engine.workflow.definition.WorkflowStateType.end;

import io.nflow.engine.workflow.curated.State;
import io.nflow.engine.workflow.definition.NextAction;
import io.nflow.engine.workflow.definition.StateExecution;
import io.nflow.engine.workflow.definition.StateVar;
import io.nflow.engine.workflow.definition.WorkflowDefinition;
import io.nflow.engine.workflow.definition.WorkflowState;
import io.nflow.engine.workflow.definition.WorkflowStateType;
import io.nflow.rest.config.RestConfiguration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.modules.deployment.DeployService;
import org.eclipse.xpanse.modules.models.service.deploy.CreateRequest;
import org.eclipse.xpanse.modules.nflow.utils.WorkflowUtils;
import org.eclipse.xpanse.modules.orchestrator.deployment.DeployTask;
import org.eclipse.xpanse.modules.orchestrator.deployment.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 *
 */
@Slf4j
@Component
@ConditionalOnClass(RestConfiguration.class)
public class ServiceMigrationWorkflow extends WorkflowDefinition {

    public static final String NEW_ID = "newId";
    public static final String DEPLOY_REQUEST = "deployRequest";
    public static final String OLD_ID = "oldId";

    public static final String TYPE = "serviceMigrationWorkflow";

    private static final WorkflowState START =
            new State("startMigrationWorkflow", WorkflowStateType.start,
                    "Start migration workflow");

    private static final WorkflowState DEPLOY_SERVICE =
            new State("deployService", WorkflowStateType.normal,
                    "deploy service");

    private static final WorkflowState DESTROY_SERVICE =
            new State("destroyService", WorkflowStateType.normal,
                    "destroy service");

    private static final WorkflowState DONE = new State("done", end,
            "migration process finished");

    private static final WorkflowState ERROR = new State("error", WorkflowStateType.manual,
            "manual processing of failed migration");


    private final DeployService deployService;

    @Autowired
    public ServiceMigrationWorkflow(
            DeployService deployService) {
        super(TYPE, START, ERROR, WorkflowUtils.getWorkflowSettings());
        this.deployService = deployService;
        permit(START, DEPLOY_SERVICE);
        permit(DEPLOY_SERVICE, DESTROY_SERVICE);
        permit(DESTROY_SERVICE, DONE);
    }

    public NextAction startMigrationWorkflow(StateExecution execution) {
        log.warn("Starting migration process,businessKey: {}, workflow instance id: {}",
                execution.getBusinessKey(), execution.getWorkflowInstanceId());
        return NextAction.moveToState(DEPLOY_SERVICE, "start migration workflow");
    }

    public NextAction deployService(StateExecution execution,
            @StateVar(value = NEW_ID) UUID newId,
            @StateVar(value = DEPLOY_REQUEST) CreateRequest request) {
        log.warn("Starting managed service with name {}, version {}, csp {}",
                request.getServiceName(),
                request.getVersion(), request.getCsp());
        DeployTask deployTask = new DeployTask();
        request.setId(newId);
        deployTask.setId(newId);
        deployTask.setCreateRequest(request);
        Deployment deployment = deployService.getDeployHandler(deployTask);
        deployService.deployService(deployment, deployTask);
        return NextAction.moveToState(DESTROY_SERVICE, "deploy service");
    }

    public NextAction destroyService(StateExecution execution,
            @StateVar(value = OLD_ID) String oldId) {
        log.warn("Stopping managed service with id {}", oldId);
        DeployTask deployTask = new DeployTask();
        deployTask.setId(UUID.fromString(oldId));
        Deployment deployment = this.deployService.getDestroyHandler(deployTask);
        deployService.destroyService(deployment, deployTask);
        return NextAction.moveToState(DONE, "destroy service");
    }

    public void done(StateExecution execution) {
        log.warn("migrate workflow ended, businessKey: {}, workflow instance id: {}",
                execution.getBusinessKey(), execution.getWorkflowInstanceId());
    }

    public void error(StateExecution execution) {
        log.warn("migrate workflow error, businessKey: {}, workflow instance id: {}",
                execution.getBusinessKey(), execution.getWorkflowInstanceId());
    }

}
