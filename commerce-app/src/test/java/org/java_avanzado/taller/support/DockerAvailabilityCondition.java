package org.java_avanzado.taller.support;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

class DockerAvailabilityCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        try {
            boolean dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
            if (dockerAvailable) {
                return ConditionEvaluationResult.enabled("Docker daemon is available");
            }
        }
        catch (RuntimeException exception) {
            return ConditionEvaluationResult.disabled("Docker daemon unavailable: " + exception.getMessage());
        }
        return ConditionEvaluationResult.disabled("Docker daemon is unavailable");
    }
}
