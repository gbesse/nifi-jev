# Exemple : incidents de service

Question de démonstration : « Ce message décrit-il une panne nécessitant une intervention humaine ? » Seuil de confiance : `0.8`. Les valeurs ci-dessous sont **synthétiques** et ne sont pas des résultats Jev mesurés.

| FlowFile synthétique | Probabilité fictive | Relation attendue | Suite opérateur |
| --- | ---: | --- | --- |
| « Le service est indisponible depuis 10 minutes » | 0.93 | `yes` | Créer un ticket après les contrôles habituels |
| « Merci pour votre aide » | 0.06 | `no` | Continuer le flux normal |
| « Certains écrans semblent lents » | 0.55 | `review` | Envoyer à la file humaine |
| Contenu vide | — | `review` | Demander un contenu exploitable |
| Erreur réseau | — | `failure` | Réessayer selon la politique de panne |

Raccordez chacune des quatre relations. `review` et `failure` ne signifient pas `no`. Filtrez les données sensibles et fixez les limites d'appels avant d'utiliser le fournisseur réel.
