# RouteWithJev pour Apache NiFi

Processeur NiFi 2.12 qui achemine un FlowFile texte selon une question sémantique oui/non posée à TypeSafe Jev. Quatre relations : `yes`, `no`, `review`, `failure`. Les résultats proches de 0,5 vont vers `review`, tout comme les entrées vides, trop grandes ou dépassant le budget. Les erreurs API vont vers `failure`.

## Construire et installer

Java 21 et Maven 3.9.6 ou plus récent :

```sh
mvn -B verify
```

Copiez `nar/target/nifi-jev-nar-0.1.2.nar` dans le répertoire `extensions` de NiFi, puis chargez ou redémarrez NiFi selon votre configuration. Ajoutez le processeur `RouteWithJev` au flux.

Réglez `API Key` comme propriété sensible, `Endpoint` à `https://api.typesafe.ai/v1/systemone`, `Model` à une version fixée telle que `jev-1.13.0`, et `Condition` à une question comme « Ce message décrit-il une panne qui nécessite une intervention humaine ? ». `Confidence Threshold` vaut par exemple `0.8` : `yes` pour p ≥ 0,8, `no` pour p ≤ 0,2, `review` entre les deux. Configurez également les limites d'entrée, de délai et d'appels.

Le processeur ajoute `jev.probability`, `jev.model` et `jev.state.sha256` au FlowFile évalué. Il ne stocke ni clé ni texte dans ces attributs. Les descriptions des propriétés et des relations sont fournies en français, anglais et espagnol dans NiFi.

## Exemple de flux

[Exemple : budget d'un seul appel et revue humaine](examples/call-budget.md).

[Exemple d'acheminement d'incidents](examples/incident-routing.md) : un petit jeu de cas synthétiques montre les quatre relations, avec une file de revue humaine et un circuit d'erreur distincts. Les probabilités sont fictives ; l'exemple ne lance aucun appel réseau.

## Limites

- Le contenu du FlowFile est envoyé à TypeSafe. Sélectionnez d'abord les flux qui peuvent quitter votre système.
- Chaque FlowFile admissible déclenche un appel API. Réduisez le volume en amont avec des filtres déterministes et fixez `Maximum Calls`.
- Les résultats probabilistes ne remplacent ni des règles d'autorisation ni une revue humaine lorsque l'enjeu est élevé.
- Le circuit `failure` doit être raccordé. Les erreurs réseau et les réponses inattendues n'envoient jamais un FlowFile vers `yes`.

Licence MIT. Intégration communautaire indépendante, sans affiliation avec Apache NiFi ou TypeSafe AI.

[English](README.en.md) · [Español](README.es.md)
