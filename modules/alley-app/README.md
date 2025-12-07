# Artist Alley Directory

Any disclaimers/licensing/build notes from the root README still apply.

This module represents the site build for https://artistalley.pages.dev, implemented using Compose
Multiplatform for Web.

## Build

Instructions assume Windows 11 environment using the latest Android Studio Canary build.

### Secrets

The AA Directory expects some secrets at both `/modules/alley/secrets.properties` and
`/modules/alley-app/secrets.properties`. These should contain these values:

`/modules/alley/secrets.properties`
```properties
# Latest convention year's sheet
sheetLink=https://example.org
# Author information, redacted from repo
authorOneName=First Last
authorOneUrl=https://example.org
authorTwoName=First Last
authorTwoUrl=https://example.org
authorTwoUsername=@username
authorAnycOneName=First Last
authorAnycTwoName=First Last
authorAnycThreeName=First Last
authorAnycThreeUrl=https://example.org
authorAnycHistoricalOneName=First Last
# Discord server for user feedback
serverName=Exmaple Discord
serverUrl=https://example.org
serverChannel=#example
# Form links which are shown in headers/settings
artistFormLink=https://example.org
feedbackFormLink=https://example.org
feedbackFormLinkAnimeNyc2025=https://example.org
```

`/modules/alley-app/secrets.properties`
```properties
# If running the editor backend, the Cloudflare R1 database ID to use
artistAlleyDatabaseId=EXAMPLE
```

### Database

The backing database also needs to be synced. In the latest version, data is synced from an editor
backend, but historical data can be downloaded from the
[Google Drive folder](https://drive.google.com/drive/u/0/folders/1FYonpq0gjCMHyeHqBSHqvpmNWTea0jck).

Each `.sql` file needs to be added to `/modules/alley/data/input` inside respective `artist`,
`stampRallies`, and `tags` folders.

This will not include images, which have to be manually copied from the Drive folders
linked in each spreadsheet to `catalogs` and `rallies` folders under
`/modules/alley/data/input/images/${convention}`.

### Running

`./gradlew :modules:alley-app:run` will launch the desktop JVM version of the site, for local
testing. Note that you may need hit a SQLite driver error, which can only be fixed by re-running
until it succeeds. Restarting the Gradle daemon by running `./gradlew --stop` first may help.

For developing the site, use `./gradlew -PwasmDebug=true :modules:alley-app:webRelease`, which will
generate a dev build `/modules/alley-app/build/dist/web/developmentExecutable`. You'll need
Cloudflare's Wrangler installed, and then you can invoke `wrangler pages dev --local ./` from that
folder to host the site locally.

For publishing the site remove the debug property and just run
`./gradlew :modules:alley-app:webRelease`, which will output to
`/modules/alley-app/build/dist/web/productionExecutable`. This can be hosted directly on Cloudflare
Pages.
