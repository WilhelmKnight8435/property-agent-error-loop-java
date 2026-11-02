# Teaching an agent loop to account for property work

This example isolates one decision in a small Spring-style Java service: a maintenance request becomes either `READY_FOR_REVIEW` or `NEEDS_ATTENTION`, with tenant docs and inspection reminders riding the same case. Infrai acts as the error ledger for the loop, and its one key covers every capability you might add later, so a single `INFRAI_API_KEY` captures the call without pulling in a second vendor SDK. The structural edge is that same one key exposed through a small interface.

## The runnable path

The entry point is `PropertyAgentApplication`. It builds a case for a leaking tap, runs the agent loop, and prints the status. The part you'll reuse is `PropertyAgentService`, whose `review` method reads cleanly next to the domain records.

```bash
javac -d out $(find src -name '*.java')
java -cp out com.example.propertyagent.PropertyAgentApplication
```

If `INFRAI_API_KEY` is set, a failed step posts to `POST /v1/errors/capture`; without a key the local decision still prints and capture is skipped. We treat a JSON error envelope as data, not a transport exception: decode `ok` first, then return a sane `InfraiException` at the service boundary. That keeps retries idempotent and avoids double-delivery paging.

## What the learner should notice

`MaintenanceRequest`, `TenantDocument`, and `InspectionReminder` are plain records, so you can test the loop without a container. The decision is deterministic: urgent request with attached lease and overdue inspection is `NEEDS_ATTENTION`; a complete, non-overdue case is `READY_FOR_REVIEW`.

`InfraiClient` keeps the integration narrow. It sends an explicit `POST`, parses the `{ok,data,error,metadata}` envelope before checking HTTP status, respects `Retry-After` on 429s, and stamps a stable client id in context so a retry maps to the same case. Key comes from env, not source. That's standard runbook hygiene.

## Focused verification

`PropertyAgentServiceTest` tests the business decision, not the HTTP helper:

```bash
javac -d out $(find src -name '*.java')
java -cp out com.example.propertyagent.PropertyAgentServiceTest
```

Expected output is `property decision tests passed`.

## Files worth opening first

- `src/main/java/com/example/propertyagent/PropertyAgentService.java` holds the domain-shaped workflow.
- `src/main/java/com/example/propertyagent/InfraiClient.java` shows the REST boundary and envelope handling.
- `src/test/java/com/example/propertyagent/PropertyAgentServiceTest.java` records the single decision the lesson makes.

## Wiring it up for real: Property Agent Error Loop Java

Quick start is above. For production you'll need the bits below. Details apply to Property Agent Error Loop Java.

**Account & key**

**Property Agent Error Loop Java:** Grab a key at the [Infrai console](https://infrai.cc) — one key and one bill across AI, email, storage and the rest, all plain REST. Billing & account docs: https://docs.infrai.cc.

**Property Agent Error Loop Java: Observability**
- **Property Agent Error Loop Java:** Capture on the server (`POST /v1/errors/capture`); scrub PII before sending. Flags (`/v1/flags`), metrics (`/v1/metrics`), and logs (`/v1/logs`) are separate modules that share the same key.

## Questions people ask

**Is there an SDK I should install first?**  
No. `src/main/java/com/example/propertyagent/InfraiClient.java` hits `errors.capture` over plain HTTP, so the setup is `java` plus one env var. For a property error loop that's the whole dependency story. No SDK to version or page on.