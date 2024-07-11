# Anichive

Anime Archive (Anichive, formerly Artist Alley Database), is an app in two parts. It is an
UNOFFICIAL client for [AniList.co](https://anilist.co), which allows the user to search and track
anime/manga.

It is also an app to catalog art prints obtained from anime conventions in an easily searchable
format with support for tagging metadata like the series, characters, and artists of each piece.

## Disclaimers

Use of this app requires usage of the
[AniList GraphQL API](https://anilist.gitbook.io/anilist-apiv2-docs/), and by using it, the user
accepts the AniList [terms of service](https://anilist.co/terms).

Release builds designed to be shipped to stores filter based on AniList's `isAdult` flag, but this
is disabled when building debug/internal from source.

There is no guarantee that imports and exports will work across versions until the format is
stabilized, nor that the data is safe from corruption or bugs. Use and update at your own risk.

All source is open and available on GitHub except signing keys and API secrets.

## Features

### AniList client
Free features:
- Full featured anime/manga search and filter
- View details of anime, manga, characters, staff, studios, users, etc.
- Get news through AnimeNewsNetwork and Crunchyroll News RSS integrations
- See global user activity

Features if optional ads are enabled:
- Log in to AniList account
- Edit and rate anime/manga entries
- Search characters, staff, studios, users, activity
- Watching/reading and user lists
- Requires persistent banner ad on bottom of screen

Features under paid subscription:
- Completely remove ads
- Databasing features used to track art prints, CDs, or merch in general from anime conventions
- Import/export support for database
- Comprehensive integration with AniList API to tag art/CDs with characters and staff
- Search to quickly pull up art at conventions to prevent duplicates
- These features are experimental

### Databasing
- Image associated with entry, which is copied into the application's private data dir
- Fields including:
  - Series (manga, anime, custom text)
  - Characters (from AniList API, custom text)
  - Source (unknown, convention, custom text)
  - Artist
  - Tags (generic text entries)
  - Physical size (width by length in millimeters)
  - Notes (generic long form text)
- Ability to lock fields to prevent them from being accidentally edited
- Rudimentary search by field
- Browse by artist, series, characters, and tag
- Export and import of entire database with images to/from a (mostly) user readable .zip file

## Screenshots

TODO: Need to find an artist willing to license their work to be displayed in this repository.
(If you draw anime art and are willing, please contact my GitHub email)

## Build

Instructions assume Windows 11 environment using the latest Android Studio Canary build.

1. Clone project with `git clone https://github.com/TheKeeperOfPie/ArtistAlleyDatabase.git`
2. Get a copy of any local dependencies and place it into `libs`:
   - SNAPSHOT version, so can't use stable version code for dependency verification.  
   ```https://s01.oss.sonatype.org/content/repositories/snapshots/com/mxalbert/sharedelements/shared-elements/0.1.0-SNAPSHOT/shared-elements-0.1.0-20221204.093513-11.aar```
3. Generate an AniList API client by following the API instructions [here](https://anilist.gitbook.io/anilist-apiv2-docs/overview/oauth/getting-started#using-oauth)
4. Create `/secrets.properties` and insert the client ID as a property:
    ```
    aniList.clientId=$CLIENT_ID
    ```
   TODO: Missing other properties
5. Install like any other Android application via `./gradlew :app:installDebug`

### Gradle

This project attempts to store the Gradle home and build caches directly inside the project root
under `/gradle-home` and `/build-cache`. This allows easy configurable of anti-virus scanning
exclusions and caching efforts, as all data is ideally read from just the project folder.

A minor benefit of this is that by deleting the two folders (along with `.gradle`), a true clean
build can be tested, where Gradle cannot reference anything it used previously.

## Dependencies

### Useful commands

- Check for updates:  
  `./gradlew dependencyUpdates`
- Verify declarations (included in git commit hook):  
  `./gradlew buildHealth`

### Regenerate verification-metadata.xml

This must be done each time a dependency is added/changed. Disabling dependency verification can be
done by deleting [`./gradle/verification-metadata.xml`](gradle/verification-metadata.xml).

[//]: # (TODO: Full clean build is starting to get annoying, need better way to regenerate metadata)

`./gradlew --write-verification-metadata pgp,sha256 generateVerificationMetadata`

### :modules:dependencies

This module serves as a way to generate verification metadata for artifacts which are used by
Android Studio but aren't used in the app build. For things like instrumentation testing that
require additional dependencies.

## Releasing
1. Rename the top entry of [`changelog.md`](changelog.md) to the next release version name
2. Create a new entry in [`changelog.md`](changelog.md) for the `Next ($versionCode)` release
3. Increment the version code and name in [`app/build.gradle.kts`](app/build.gradle.kts)
4. Studio toolbar > Build > Generate Signed App Bundle > auth for keystore > create `release`
5. Upload release from [`/app/release`](app/release)

## Licensing

TODO: Add a real license
All rights reserved, no warranty or support provided, no commercial derivatives allowed. Individuals
are allowed to edit and build the app for personal use, but cannot distribute copies. If you build
this app to avoid ads/monetization, please consider donating. 
