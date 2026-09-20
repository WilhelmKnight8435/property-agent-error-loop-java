package com.example.propertyagent;

import java.util.List;

public final class PropertyAgentService {
    private final InfraiClient infrai;

    public PropertyAgentService(InfraiClient infrai) {
        this.infrai = infrai;
    }

    public AgentDecision review(PropertyCase propertyCase) {
        try {
            MaintenanceRequest request = propertyCase.request();
            boolean missingLearningEvidence = !propertyCase.tenantDocument().verified();
            boolean overdueInspection = propertyCase.inspectionReminder().overdue();
            if (request.urgent() || missingLearningEvidence || overdueInspection) {
                return new AgentDecision("NEEDS_ATTENTION", "agent asks a property manager to review the case");
            }
            return new AgentDecision("READY_FOR_REVIEW", "agent prepared a complete case for the next lesson");
        } catch (RuntimeException ex) {
            infrai.capture("property-agent", "case-review", ex, propertyCase);
            throw ex;
        }
    }

    public record PropertyCase(MaintenanceRequest request, TenantDocument tenantDocument,
                               InspectionReminder inspectionReminder) {}
    public record MaintenanceRequest(String id, String unit, String summary, boolean urgent) {}
    public record TenantDocument(String name, boolean verified) {}
    public record InspectionReminder(String id, boolean overdue) {}
    public record AgentDecision(String status, String explanation) {}
}
