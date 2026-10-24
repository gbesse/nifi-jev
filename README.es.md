# RouteWithJev para Apache NiFi

## Los atributos de enrutamiento pertenecen al paso actual

Desde v0.1.3, cada FlowFile recibe `jev.route` (`yes`, `no`, `review` o `failure`). Antes de cada nuevo control el procesador elimina sus atributos propios: `jev.probability`, `jev.model`, `jev.state.sha256`, `jev.reason`, `jev.error` y `jev.route`. Un `yes` anterior ya no deja una probabilidad engañosa cuando el nuevo paso falla, agota su presupuesto o rechaza una entrada vacía/demasiado grande. Los otros atributos, incluido `jev.custom`, se conservan.

Una ruta `yes`/`no` conserva la nueva probabilidad, modelo y huella; la revisión por incertidumbre añade `jev.reason: uncertain`. Los rechazos previos a inferencia tienen su nuevo motivo y ninguna probabilidad anterior. Un error remoto lleva `jev.error: remote_failure`. `jev.route` describe la relación de salida actual; este recibo no demuestra corrección semántica ni entrega posterior a NiFi. Las pruebas usan NiFi TestRunner y un servidor HTTP local sintético, sin llamadas TypeSafe reales.

Procesador NiFi 2.12 que enruta un FlowFile de texto mediante una pregunta semántica sí/no de TypeSafe Jev. Cuatro relaciones: `yes`, `no`, `review` y `failure`. Los resultados cercanos a 0,5 van a `review`, igual que las entradas vacías, demasiado grandes o que exceden el presupuesto. Los errores de API van a `failure`.

## Compilar e instalar

Java 21 y Maven 3.9.6 o posterior:

```sh
mvn -B verify
```

Copie `nar/target/nifi-jev-nar-0.1.3.nar` al directorio `extensions` de NiFi y recargue o reinicie NiFi según su configuración. Añada `RouteWithJev` al flujo.

Configure `API Key` como propiedad sensible, `Endpoint` como `https://api.typesafe.ai/v1/systemone`, `Model` con una versión fijada como `jev-1.13.0` y `Condition` con una pregunta como «¿Describe este mensaje una incidencia que requiere intervención humana?». Por ejemplo, con `Confidence Threshold` en `0.8`, p ≥ 0,8 va a `yes`, p ≤ 0,2 a `no` y los valores intermedios a `review`. Configure también los límites de entrada, plazo y llamadas.

El procesador añade `jev.probability`, `jev.model` y `jev.state.sha256` a los FlowFiles evaluados. No coloca la clave ni el texto en estos atributos. Las descripciones de propiedades y relaciones aparecen en español, francés e inglés en NiFi.

## Ejemplo de flujo

[Ejemplo de presupuesto de una llamada y revisión humana](examples/call-budget.md).

[Ejemplo de enrutamiento de incidencias](examples/incident-routing.es.md): un pequeño conjunto de casos sintéticos muestra las cuatro relaciones, con una cola de revisión humana y una ruta de error separadas. Las probabilidades son ficticias; el ejemplo no realiza llamadas de red.

## Límites

- El contenido del FlowFile se envía a TypeSafe. Seleccione primero qué flujos pueden salir de su sistema.
- Cada FlowFile admisible genera una llamada de API. Reduzca el volumen antes con filtros deterministas y configure `Maximum Calls`.
- Los resultados probabilísticos no sustituyen las reglas de autorización ni la revisión humana en decisiones importantes.
- Conecte la vía `failure`. Los errores de red y las respuestas inesperadas nunca envían un FlowFile a `yes`.

Licencia MIT. Integración comunitaria independiente, sin afiliación con Apache NiFi ni TypeSafe AI.

[Français](README.md) · [English](README.en.md)

## Comprobación de adopción

[Pruebe un caso concreto y compruebe sus límites](examples/adoption-check.md).

## Texto y transporte limitados

Bytes UTF-8 incorrectos pasan a `review` con `jev.reason=invalid_utf8`, sin llamar al proveedor ni sustituir caracteres silenciosamente. El hash de texto válido describe así los bytes realmente decodificados. Respuestas HTTP limitadas a 100 000 bytes durante la recepción, antes de acumular todo y analizarlo; un exceso pasa a `failure` con `remote_failure`. Se siguen borrando atributos de decisiones anteriores. Las regresiones usan NiFi TestRunner y servidor local; no acceden a NiFi de producción ni cuentas Jev reales.

Las respuestas deben indicar el modelo esperado y exactamente la pregunta decision; claves JSON duplicadas y documentos adicionales van a failure sin probabilidad utilizable.

El endpoint HTTP requiere host, sin credenciales incrustadas ni fragmento y con puerto válido; se conservan HTTPS o HTTP local. Los errores nunca repiten el valor suministrado.
