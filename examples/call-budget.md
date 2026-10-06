# Budget d'appels · Call budget · Presupuesto de llamadas

Configurez `Maximum Calls` à `1` pour une période de planification, puis envoyez deux FlowFiles texte non vides. Le premier peut appeler le service. Le second doit aller vers `review` avec `jev.reason=budget`, sans nouvel appel distant. La relation `failure` reste réservée aux erreurs du service ou de configuration. Reliez les quatre relations avant tout essai réel et utilisez uniquement des textes autorisés.

Set `Maximum Calls` to `1` for one processor scheduling period, then send two nonempty text FlowFiles. The first may call the service. The second must reach `review` with `jev.reason=budget`, without another remote call. `failure` remains for service or configuration errors. Connect all four relationships before a live trial and use only permitted text.

Configure `Maximum Calls` en `1` durante un periodo de programación del procesador y envíe dos FlowFiles de texto no vacíos. El primero puede llamar al servicio. El segundo debe ir a `review` con `jev.reason=budget`, sin otra llamada remota. `failure` queda para errores del servicio o de configuración. Conecte las cuatro relaciones antes de una prueba real y use solo textos autorizados.

Ce parcours décrit le comportement du processeur ; il ne fournit aucune mesure de débit. / This walkthrough describes processor behavior; it provides no throughput measurement. / Este recorrido describe el comportamiento del procesador; no aporta una medida de rendimiento.
