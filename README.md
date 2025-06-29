# Coding Style Guide

Dieses Dokument beschreibt die Coding-Style-Regeln für das Android-Projekt. Ziel ist ein einheitlicher, gut lesbarer und wartbarer Code.

---

## 1. Projektstruktur & Architektur

- **Architektur-Pattern:**  
  Verwendet MVVM in Kombination mit Clean Architecture (Domainschichten: `data`, `domain`, `presentation`).
- **Verzeichnisstruktur:**
  ```
  app/
    src/
      main/
        java/com/example/app/
          data/           # Datenquelle, DTOs, Repositories
          domain/         # Business-Logik, UseCases, Entities
          presentation/   # ViewModels, UI-Elemente (Activities/Fragments)
        res/
          layout/
          drawable/
          values/
      test/               # Unit-Tests
      androidTest/        # Instrumentation-Tests
  ```

---

## 2. Sprache & Dateien

- **Sprache:** Kotlin (min. Version 1.8)  
- **Dateiendungen:**  
  `.kt` für Kotlin  
  `.xml` für Layouts und Ressourcen  
  `.gradle.kts` für Gradle-Skripte  

---

## 3. Formatierung & Einrückung

- **Einrückung:** 4 Spaces, kein Tab  
- **Maximale Zeilenlänge:** 120 Zeichen  
- **Klammerstil:**  
  ```kotlin
  // Klassen, Funktionen, Lambdas
  class Foo {
      fun bar() {
          items.forEach { item ->
              // ...
          }
      }
  }
  ```
- **Leerzeilen:**  
  - 1 Leerzeile zwischen Funktionen  
  - 2 Leerzeilen zwischen Klassen  

---

## 4. Namenskonventionen

| Element         | Konvention                                     |
|-----------------|------------------------------------------------|
| Packages        | `com.example.app.feature` (klein, punktgetrennt) |
| Klassen         | `PascalCase` (z. B. `UserRepository`)          |
| Interfaces      | `PascalCase` (keine „I“-Prefix)                |
| Funktionen      | `camelCase` (z. B. `fetchUserData()`)          |
| Variablen       | `camelCase`                                    |
| Konstanten      | `UPPER_SNAKE_CASE`                             |
| XML-Ressourcen  | `snake_case` (z. B. `activity_main.xml`)       |
| IDs             | `snake_case` (z. B. `btn_submit`)              |

---

## 5. Dokumentation

- **KDoc:**  
  - Klassen und öffentliche Methoden immer dokumentieren.
  - Tags: `@param`, `@return`, `@throws`.
  ```kotlin
  /**
   * Lädt die Daten für den Benutzer.
   *
   * @param userId ID des Benutzers
   * @return LiveData mit dem Ergebnis
   * @throws IOException bei Netzwerkfehler
   */
  suspend fun loadUser(userId: String): LiveData<Result<User>>
  ```
- **Kommentare:**  
  Nur bei komplexer Logik, nicht offensichtliches Verhalten erläutern.

---

## 6. Linting & Static Analysis

- **EditorConfig:**  
  Legt grundlegende Regeln fest, z. B. Einrückung, Zeilenlänge.  
- **ktlint:**  
  - Automatische Formatierung (`./gradlew ktlintFormat`)  
  - Überprüfung im CI (`./gradlew ktlintCheck`)
- **detekt:**  
  - Regeln in `detekt.yml` anpassen  
  - CI-Check via `./gradlew detekt`

---

## 7. Testing

- **Unit-Tests:**  
  - Framework: JUnit5 + MockK  
  - Namensschema: `ClassName_MethodName_StateUnderTest`  
- **Instrumentation-Tests:**  
  - Framework: Espresso  
  - Keep-It-Simple: nur UI-Kernflüsse

---

## 8. Git & Commits

- **Branching:**  
  - `main` für stabile Releases  
  - Feature-Branches: `feature/<kurz-beschreibung>`  
  - Bugfix-Branches: `bugfix/<kurz-beschreibung>`
- **Commit-Message:** Conventional Commits  
  ```
  feat(login): implementiere OAuth-Flow
  fix(onboarding): korrigiere NullPointerException
  docs(readme): aktualisiere Coding Style Guidelines
  ```
- **PR-Review:**  
  - Min. 1 Reviewer  
  - CI-Checks müssen grünen Status haben

---

## 9. CI/CD

- **Build:** Gradle-Wrapper (`./gradlew clean build`)  
- **Checks:** ktlint, detekt, Unit-Tests, Lint  
- **Deployment:** Automatisiert via GitHub Actions / Bitrise / Jenkins

---

## 10. Weitere Hinweise

- **Resourcen-Optimierung:**  
  PNG nur, wenn notwendig; sonst WebP  
- **Accessibility:**  
  Content-Descriptions für Icons, `android:labelFor` in XML  
- **Internationalisierung:**  
  Strings nur in `res/values/strings.xml`, keine hardcodierten Texte  

---

> **Tipp:** Bindet die Lint-Tasks direkt in den Gradle-Build ein, damit bei jedem Push sofort Feedback erfolgt.

---

*Diese Guidelines können bei Bedarf erweitert oder angepasst werden.*  
