package com.example.propertyagent;

import java.util.List;

public final class PropertyAgentApplication {
    public static void main(String[] args) {
        InfraiClient client = new InfraiClient(System.getenv("INFRAI_API_KEY"));
        PropertyAgentService service = new PropertyAgentService(client);
        PropertyAgentService.PropertyCase propertyCase = new PropertyAgentService.PropertyCase(
                new PropertyAgentService.MaintenanceRequest("MR-104", "Unit 3B", "leaking tap", false),
                new PropertyAgentService.TenantDocument("lease-3b.pdf", true),
                new PropertyAgentService.InspectionReminder("INS-22", false));
        PropertyAgentService.AgentDecision decision = service.review(propertyCase);
        System.out.println(decision.status() + ": " + decision.explanation());
    }
}
