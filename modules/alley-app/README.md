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
# IDs backing the data for each convention year
sheetIdAnimeExpo2024=1_cSqgsAtQRdyv0-toWIdLRfpwinQuTA1R51BORSOpCs
sheetIdAnimeExpo2023=1lVlD-cTgYX8QbScYe8vjOmLcRbn_HKSrab_-hUMClm4
sheetIdAnimeExpo2025=1tb3BCegOGhi-uITWEOBqSMob7YNkqncL7ZgO-wrzazQ
sheetIdAnimeNyc2024=1IVQzkygDNDGl6_kSX18CLdQZQLL3ZrapCL8mjfjMxag
sheetIdAnimeNyc2025=1jvC9ImNEDik8LJTBxTK60tYmVM_Efv_BgIP4HvIONAs

# Latest convention year's sheet
sheetLink=https://docs.google.com/spreadsheets/d/1jvC9ImNEDik8LJTBxTK60tYmVM_Efv_BgIP4HvIONAs/view

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

After adding these 2 files, you'll need to sync the database locally by running
`/modules/alley/data/scripts/syncSheets.main.kts`. This is a Kotlin script and requires manually
invoking the Kotlin compiler. Alternatively, if the `/modules/alley/data/input` folder is manually
created, this can be synced in Android Studio and run in the right click menu on the script file. 
Note that syncing can take a very long time if the database has to be regenerated.

This database sync will not include images, which have to be manually copied from the Drive folders
linked in each spreadsheet to `catalogs` and `rallies` folders under the respective convention
folders.

Running `./gradlew :modules:alley-app:run` will launch the desktop JVM version of the site, for
local testing. Note that you may need hit a SQLite driver error, which can only be fixed by
re-running until it succeeds. Restarting the Gradle daemon by running `./gradlew --stop` first may
help.

For developing the site, use `./gradlew -PwasmDebug=true :modules:alley-app:webRelease`, which will
generate a dev build `/modules/alley-app/build/dist/web/developmentExecutable`. You'll need
Cloudflare's Wrangler installed, and then you can invoke `wrangler pages dev --local ./` from that
folder to host the site locally.

For publishing the site remove the debug property and just run
`./gradlew :modules:alley-app:webRelease`, which will output to
`/modules/alley-app/build/dist/web/productionExecutable`. This can be hosted directly on Cloudflare
Pages.
