# Example: service incidents

Demo question: “Does this message describe an outage requiring human intervention?” Confidence threshold: `0.8`. The values below are **synthetic**, not measured Jev output.

| Synthetic FlowFile | Fabricated probability | Expected relationship | Operator action |
| --- | ---: | --- | --- |
| “The service has been unavailable for 10 minutes” | 0.93 | `yes` | Create a ticket after normal checks |
| “Thanks for your help” | 0.06 | `no` | Continue the normal flow |
| “Some screens seem slow” | 0.55 | `review` | Send to the human queue |
| Empty content | — | `review` | Request usable content |
| Network error | — | `failure` | Retry under the outage policy |

Connect all four relationships. `review` and `failure` do not mean `no`. Filter sensitive data and set call limits before using the real provider.
