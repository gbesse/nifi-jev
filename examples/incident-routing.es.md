# Ejemplo: incidencias de servicio

Pregunta de demostración: «¿Describe este mensaje una incidencia que requiere intervención humana?». Umbral de confianza: `0.8`. Los valores siguientes son **sintéticos** y no representan resultados Jev medidos.

| FlowFile sintético | Probabilidad ficticia | Relación esperada | Acción del operador |
| --- | ---: | --- | --- |
| «El servicio lleva 10 minutos sin funcionar» | 0.93 | `yes` | Crear un ticket tras las comprobaciones habituales |
| «Gracias por su ayuda» | 0.06 | `no` | Continuar el flujo normal |
| «Algunas pantallas parecen lentas» | 0.55 | `review` | Enviar a la cola humana |
| Contenido vacío | — | `review` | Solicitar contenido utilizable |
| Error de red | — | `failure` | Reintentar según la política de incidentes |

Conecte las cuatro relaciones. `review` y `failure` no significan `no`. Filtre los datos sensibles y establezca límites de llamadas antes de usar el proveedor real.
