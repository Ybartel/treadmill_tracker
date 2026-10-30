# Tapis de course

App Android personnelle pour noter l'état des tapis de course d'une salle et choisir le bon en arrivant.
Hors ligne, sans compte. Kotlin, Compose, Hilt, Room, DataStore ; minSdk 26.

## Commandes

```bash
./gradlew installDebug                # installer sur l'appareil connecté
./gradlew testDebugUnitTest           # tests JUnit 5 (domaine, ViewModel)
./gradlew connectedDebugAndroidTest   # tests Room et Compose (appareil requis)
```

`JAVA_HOME` peut pointer vers le JDK d'Android Studio :
`/Applications/Android Studio.app/Contents/jbr/Contents/Home`.

## Structure

- `domain` : modèles, interfaces de repository, cas d'usage, `reviewFreshness` (fonction pure)
- `data` : Room (`treadmill`, `treadmill_review`) et DataStore
- `ui` : écrans Compose et ViewModels

L'état d'un tapis n'est jamais stocké : il se déduit de son dernier avis. Un tapis retiré reste en base
(`isActive = false`) et retrouve ses avis quand il est rajouté.
