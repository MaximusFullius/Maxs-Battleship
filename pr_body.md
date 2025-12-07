Update `pom.xml` to target Java 21 and add OpenRewrite plugin for optional code migrations.

Changes:
- `pom.xml`: set `maven.compiler.source`/`target` and `<release>` to `21`; add `rewrite-maven-plugin`.
- `UPGRADE_TO_JAVA21.md`: verification instructions.
- `.github/appmod/code-migration/summary.md`: migration summary.

Verification:
1. Set `JAVA_HOME` to `C:\Users\Max\.jdk\jdk-21.0.8(1)` and run `mvn -B -DskipTests=true clean compile`.
2. Run `mvn -B test`.

Notes:
- OpenRewrite was run locally; no additional source edits were required.
- No unit tests were present; consider adding tests to validate behavior.
