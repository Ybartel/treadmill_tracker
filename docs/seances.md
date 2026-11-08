# Séances : plan fonctionnel

Enregistrer le compte-rendu de ses séances de tapis et en suivre les statistiques par semaine calendaire.

## Navigation

Barre de navigation en bas : **Tapis** (écran existant) et **Séances**.

## Saisie d'une séance

Écran plein écran, ouvert par le bouton « + » de l'onglet Séances, ou en touchant une séance pour la modifier.

| Ordre | Champ | Obligatoire | Règles |
|---|---|---|---|
| 1 | Durée | Oui | Minutes entières, 1 à 300. Boutons − / + d'une minute (appui long : défilement rapide), raccourcis 30 · 45 · 60 · 90, appui sur la valeur pour la saisir au clavier |
| 2 | Vitesse moyenne | Oui | km/h, une décimale, 0,5 à 25. Boutons − / + de 0,1 (appui long : défilement rapide), appui sur la valeur pour la saisir au clavier |
| 3 | Nombre de pas | Non | Entier, 1 à 100 000. Vide et non sélectionné à l'ouverture, rappel « Ajoutez-les pour obtenir cadence et foulée » |
| 4 | Fin de la séance | Oui | « Terminée maintenant » par défaut, « Modifier » ouvre les sélecteurs de date puis d'heure. Jamais dans le futur |

- Durée et vitesse sont préremplies avec celles de la dernière séance (30 min et 6,0 km/h sans historique).
- L'heure de début n'est pas saisie : fin − durée.
- Quatre tuiles se mettent à jour pendant la saisie : distance, allure, cadence, foulée (« — » tant que les pas manquent).
- Bouton « Enregistrer » pleine largeur en bas. En modification : formulaire prérempli avec la séance, plus « Supprimer » (avec annulation par snackbar).

## Statistiques calculées

Jamais stockées, toujours déduites des saisies.

- Distance = vitesse × durée
- Allure = durée / distance (min/km)
- Cadence = pas / durée (pas/min)
- Foulée = distance / pas (m)

Sur une semaine :
- Temps, distance, nombre de séances : sommes
- Vitesse moyenne = distance totale / temps total
- Pas totaux, cadence et foulée moyennes : uniquement sur les séances avec pas
  (cadence = pas / minutes de ces séances, foulée = distance de ces séances / pas)
- Complétude : « Pas renseignés : n / total »

## Onglet Séances

- Résumé teinté de la semaine calendaire en cours (lundi → dimanche) : séances, temps, distance. Un appui ouvre les Statistiques.
- Semaine en cours vide : « 0 séance » et rappel de la semaine précédente.
- Liste d'une ligne par séance, de la plus récente à la plus ancienne : date, durée, distance. Pastille colorée pour la semaine en cours, grise ensuite.
- Liste vide : invitation à enregistrer une première séance.

## Écran Statistiques

- S'ouvre sur la semaine en cours. Flèches ‹ › et balayage horizontal, bornés à la semaine de la première séance et à la semaine en cours. « Revenir à cette semaine » hors de la semaine en cours.
- Bloc hebdomadaire : séances, temps, distance, vitesse moyenne ; ligne des pas (totaux, cadence, foulée) avec jauge de complétude ; mini-graphique de la distance par jour (L → D).
- Une carte par séance, de la plus récente à la plus ancienne : date, plage horaire, grille durée · vitesse · distance · allure · cadence · foulée. Sans pas, cadence et foulée sont remplacées par « + Ajouter les pas », qui ouvre le formulaire sur le champ Pas.
- Semaine vide : illustration, message encourageant pour la semaine en cours (avec le rappel de la précédente), neutre pour une ancienne semaine, bouton « Enregistrer une séance ».

## Couleurs

Chaque famille de mesures garde sa couleur et son icône partout :
sarcelle (séance, temps), bleu (distance), violet (vitesse, allure), rose (pas, cadence, foulée).
Pas de vert, orange ni rouge : ils sont réservés à l'état des tapis.

## Données

Table Room `workout_session` (id, durée, vitesse, pas facultatifs, date de fin), migration v1 → v2.

## Plus tard

- Lier une séance à un tapis (facultatif), pour repérer les tapis dont la vitesse est mal calibrée.
- Inclinaison (%).
