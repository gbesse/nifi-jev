# RouteWithJev para Apache NiFi

Procesador NiFi 2.12 que enruta un FlowFile de texto mediante una pregunta semántica sí/no de TypeSafe Jev. Cuatro relaciones: `yes`, `no`, `review` y `failure`. Los resultados cercanos a 0,5 van a `review`, igual que las entradas vacías, demasiado grandes o que exceden el presupuesto. Los errores de API van a `failure`.

## Compilar e instalar

Java 21 y Maven 3.9.6 o posterior:

```sh
mvn -B verify
```

Copie `nar/target/nifi-jev-nar-0.1.0.nar` al directorio `extensions` de NiFi y recargue o reinicie NiFi según su configuración. Añada `RouteWithJev` al flujo.

Configure `API Key` como propiedad sensible, `Endpoint` como `https://api.typesafe.ai/v1/systemone`, `Model` con una versión fijada como `jev-1.13.0` y `Condition` con una pregunta como «¿Describe este mensaje una incidencia que requiere intervención humana?». Por ejemplo, con `Confidence Threshold` en `0.8`, p ≥ 0,8 va a `yes`, p ≤ 0,2 a `no` y los valores intermedios a `review`. Configure también los límites de entrada, plazo y llamadas.

El procesador añade `jev.probability`, `jev.model` y `jev.state.sha256` a los FlowFiles evaluados. No coloca la clave ni el texto en estos atributos. Las descripciones de propiedades y relaciones aparecen en español, francés e inglés en NiFi.

## Límites

- El contenido del FlowFile se envía a TypeSafe. Seleccione primero qué flujos pueden salir de su sistema.
- Cada FlowFile admisible genera una llamada de API. Reduzca el volumen antes con filtros deterministas y configure `Maximum Calls`.
- Los resultados probabilísticos no sustituyen las reglas de autorización ni la revisión humana en decisiones importantes.
- Conecte la vía `failure`. Los errores de red y las respuestas inesperadas nunca envían un FlowFile a `yes`.

Licencia MIT. Integración comunitaria independiente, sin afiliación con Apache NiFi ni TypeSafe AI.

[Français](README.md) · [English](README.en.md)
