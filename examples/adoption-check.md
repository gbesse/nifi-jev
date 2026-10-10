# nifi-jev — contrôle d’adoption · adoption check · comprobación de adopción

## Français

Point de départ local, après la préparation indiquée dans le README :

```sh
mvn -B verify
```

Dans un flux d’incidents, raccordez séparément `review` et `failure`. Un score proche de 0,5 demande une revue humaine ; une erreur de transport ne doit pas passer en `yes`.

## English

Local starting point, after the setup described in the README:

```sh
mvn -B verify
```

In an incident flow, connect `review` and `failure` separately. A score near 0.5 needs human review; a transport error must not enter `yes`.

## Español

Punto de partida local, después de la preparación descrita en el README:

```sh
mvn -B verify
```

En un flujo de incidentes, conecte `review` y `failure` por separado. Una puntuación cercana a 0,5 requiere revisión humana; un error de transporte no debe ir a `yes`.
## Variante synthétique · Synthetic variation · Variante sintética

```text
jev.probability=0.50 -> review; http_timeout -> failure
```

FR : adaptez une copie de la fixture locale à cette situation, puis vérifiez le comportement décrit ci-dessus. Les valeurs sont illustratives, pas des résultats Jev mesurés.

EN: adapt a copy of the local fixture to this situation, then check the behavior described above. Values are illustrative, not measured Jev output.

ES: adapte una copia de la fixture local a esta situación y compruebe el comportamiento descrito arriba. Los valores son ilustrativos, no resultados Jev medidos.

## Second cas · Second case · Segundo caso

```text
maximum_calls=100; calls_used=100; next_flowfile=1
```

**FR :** Quand le budget d’appels est épuisé, le FlowFile suivant doit rejoindre `review`, pas `yes`. Réservez un chemin opérationnel à cette file.

**EN:** Once the call budget is exhausted, the next FlowFile should go to `review`, not `yes`. Give that queue an operational path.

**ES:** Cuando se agota el presupuesto de llamadas, el siguiente FlowFile debe ir a `review`, no a `yes`. Dé a esa cola una ruta operativa.
