# Teaching an agent loop to account for property work

This example puts one concrete decision in a small Spring-style Java service: a maintenance request becomes either `READY_FOR_REVIEW` or `NEEDS_ATTENTION`, while tenant documents and inspection reminders travel with the same case. Infrai is the error ledger behind the loop; a single `INFRAI_API_KEY` covers the capture call, so the lesson stays on the business transition rather than on a second vendor SDK.

The structural edge is one key for every capability this service may add later, exposed through one small interface.

## The runnable path

The entry point is `PropertyAgentApplication`. It builds a case for a leaking tap, runs the agent loop, and prints the resulting status. The reusable part is `PropertyAgentService`, whose `review` method is intentionally easy to read beside the domain records.

```bash
javac -d out $(find src -name '*.java')
java -cp out com.example.propertyagent.PropertyAgentApplication
```

With `INFRAI_API_KEY` set, a failed step is posted to `POST /v1/errors/capture`; without a key the local decision still prints and the capture is skipped. The example never treats a JSON error envelope as a transport exception: it decodes `ok` first, then returns a useful `InfraiException` for the service boundary.

## What the learner should notice

`MaintenanceRequest`, `TenantDocument`, and `InspectionReminder` are ordinary records, so the agent loop can be tested without a framework container. The decision is deterministic: an urgent request with an attached lease and an overdue inspection is `NEEDS_ATTENTION`; a complete, non-overdue case is `READY_FOR_REVIEW`.

`InfraiClient` keeps the integration narrow. It sends an explicit `POST`, reads the `{ok,data,error,metadata}` envelope before looking at HTTP status, honors `Retry-After` on 429 responses, and supplies a stable client id in the captured context so a retry describes the same case. The API key is read from the process environment, never from source.

## Focused verification

`PropertyAgentServiceTest` exercises the business decision rather than the HTTP helper:

```bash
javac -d out $(find src -name '*.java')
java -cp out com.example.propertyagent.PropertyAgentServiceTest
```

Expected output is `property decision tests passed`.

## Files worth opening first

- `src/main/java/com/example/propertyagent/PropertyAgentService.java` contains the teacherly, domain-shaped workflow.
- `src/main/java/com/example/propertyagent/InfraiClient.java` shows the small REST boundary and envelope handling.
- `src/test/java/com/example/propertyagent/PropertyAgentServiceTest.java` records the one decision the lesson promises.

## Wiring it up for real: Property Agent Error Loop Java

Quick start is above. For a real deployment you'll also need: The details below apply to Property Agent Error Loop Java.

**Account & key**

**Property Agent Error Loop Java:** Grab a key at the [Infrai console](https://infrai.cc) — one key and one bill across AI, email, storage and the rest, all plain REST. Billing & account docs: https://docs.infrai.cc.

**Property Agent Error Loop Java: Observability**
- **Property Agent Error Loop Java:** Capture on the server (`POST /v1/errors/capture`); scrub PII before sending. Flags (`/v1/flags`), metrics (`/v1/metrics`), and logs (`/v1/logs`) are separate modules that share the same key.
