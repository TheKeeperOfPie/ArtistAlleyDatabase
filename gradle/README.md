To regenerate verification-metadata:

1. Delete `<trusted-keys>` and `<components>` sections from `verification-metadata.xml`
2. `./gradlew --write-verification-metadata pgp,sha256 clean`
3. `./gradlew --write-verification-metadata pgp,sha256 downloadAniListApolloSchemaFromIntrospection`
4. `./gradlew --write-verification-metadata pgp,sha256 assemble buildHealth`