# RouteWithJev for Apache NiFi

NiFi 2.12 processor that routes a text FlowFile using a TypeSafe Jev semantic yes/no question. Four relationships: `yes`, `no`, `review`, and `failure`. Results close to 0.5 go to `review`, as do empty, oversized, or over-budget inputs. API errors go to `failure`.

## Build and install

Java 21 and Maven 3.9.6 or newer:

```sh
mvn -B verify
```

Copy `nar/target/nifi-jev-nar-0.1.2.nar` into NiFi's `extensions` directory, then reload or restart NiFi as configured. Add `RouteWithJev` to a flow.

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
