package com.example.propertyagent;

public final class PropertyAgentServiceTest {
    public static void main(String[] args) {
        PropertyAgentService service = new PropertyAgentService(new InfraiClient(null));
        PropertyAgentService.PropertyCase complete = new PropertyAgentService.PropertyCase(
                new PropertyAgentService.MaintenanceRequest("MR-1", "1A", "broken latch", false),
                new PropertyAgentService.TenantDocument("lease.pdf", true),
                new PropertyAgentService.InspectionReminder("INS-1", false));
        check("READY_FOR_REVIEW".equals(service.review(complete).status()), "complete case should be ready");
        PropertyAgentService.PropertyCase urgent = new PropertyAgentService.PropertyCase(
                new PropertyAgentService.MaintenanceRequest("MR-2", "1A", "smoke alarm", true),
                new PropertyAgentService.TenantDocument("lease.pdf", true),
                new PropertyAgentService.InspectionReminder("INS-2", false));
        check("NEEDS_ATTENTION".equals(service.review(urgent).status()), "urgent case needs attention");
        System.out.println("property decision tests passed");
    }
    private static void check(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
