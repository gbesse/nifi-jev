# RouteWithJev for Apache NiFi

## Routing attributes belong to the current pass

Since v0.1.3, every FlowFile receives `jev.route` (`yes`, `no`, `review` or `failure`). Before each new check the processor removes its owned attributes: `jev.probability`, `jev.model`, `jev.state.sha256`, `jev.reason`, `jev.error` and `jev.route`. An old `yes` can no longer leave a misleading probability when the new pass fails, reaches its budget or rejects empty/oversized input. Other attributes, including `jev.custom`, remain unchanged.

A `yes`/`no` route keeps the new probability, model and fingerprint; uncertainty review adds `jev.reason: uncertain`. Pre-inference refusals have their new reason and no old probability. A remote failure carries `jev.error: remote_failure`. `jev.route` describes the current outgoing relationship; this receipt does not establish semantic correctness or delivery beyond NiFi. Tests use NiFi TestRunner and a synthetic local HTTP server, with no live TypeSafe call.

NiFi 2.12 processor that routes a text FlowFile using a TypeSafe Jev semantic yes/no question. Four relationships: `yes`, `no`, `review`, and `failure`. Results close to 0.5 go to `review`, as do empty, oversized, or over-budget inputs. API errors go to `failure`.

## Build and install

Java 21 and Maven 3.9.6 or newer:

```sh
mvn -B verify
```

Copy `nar/target/nifi-jev-nar-0.1.3.nar` into NiFi's `extensions` directory, then reload or restart NiFi as configured. Add `RouteWithJev` to a flow.

Set `API Key` as a sensitive property, `Endpoint` to `https://api.typesafe.ai/v1/systemone`, `Model` to a pinned version such as `jev-1.13.0`, and `Condition` to a question such as “Does this message describe an outage requiring human intervention?” For example, with `Confidence Threshold` at `0.8`, p ≥ 0.8 routes to `yes`, p ≤ 0.2 to `no`, and values between those to `review`. Configure input size, timeout, and call budgets too.

The processor adds `jev.probability`, `jev.model`, and `jev.state.sha256` to evaluated FlowFiles. It does not put the key or text in these attributes. Property and relationship descriptions are provided in English, French, and Spanish in NiFi.

## Flow example

[One-call budget and human-review example](examples/call-budget.md).

[Incident routing example](examples/incident-routing.en.md): a small set of synthetic cases shows all four relationships, with a separate human-review queue and failure path. Probabilities are fabricated; the example makes no network calls.

## Limits

- FlowFile content is sent to TypeSafe. First select which flows may leave your system.
- Every eligible FlowFile triggers an API call. Reduce volume upstream with deterministic filters and set `Maximum Calls`.
- Probabilistic results do not replace authorization rules or human review for consequential decisions.
- Connect the `failure` route. Network errors and unexpected answers never route a FlowFile to `yes`.

MIT licensed. Independent community integration, unaffiliated with Apache NiFi or TypeSafe AI.

[Français](README.md) · [Español](README.es.md)

## Adoption check

[Try a concrete case and check its limits](examples/adoption-check.md).

## Bounded text and transport

Malformed UTF-8 bytes now route to `review` with `jev.reason=invalid_utf8`, without a provider call or silent character replacement. Valid text hashes therefore describe the actual decoded bytes. HTTP response bodies are capped at 100,000 bytes while receiving, before full accumulation and parsing; overflow routes to `failure` with `remote_failure`. Old verdict attributes remain cleared. Regressions use NiFi TestRunner and a local server; no production NiFi or live Jev account is accessed.

Responses must name the expected model and exactly the decision question; duplicate JSON keys and trailing JSON documents route to failure without a usable probability.
