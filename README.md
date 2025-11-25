# Anichive

Anime Archive (Anichive, formerly Artist Alley Database), is an app in two parts. It is an
UNOFFICIAL client for [AniList.co](https://anilist.co), which allows the user to search and track
anime/manga.

__If you're looking for artistalley.pages.dev, see `./modules/alley-app/README.md`__

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

## Build

Instructions assume Windows 11 environment using the latest Android Studio Canary build.

### Secrets

Anichive expects some secrets at a `./secrets.properties` file in the project root. This should
contain these values:
```properties
# ID generated at https://anilist.gitbook.io/anilist-apiv2-docs/overview/oauth/getting-started#using-oauth
aniListClientId=1234

# Optional test account to automatically grant full access
aniListTestAccountUserId=1234

# If integrating with Unity Ads, the associated game project ID
unityGameId=EXAMPLE

# The specific ad unit to load
unityBannerAdUnitId=EXAMPLE

# Discord server link for user feedback
discordServerInviteLink=https://example.org

# Privacy policy link
privacyPolicyLink=https://example.org
```

After creating this file with valid values (only `aniListClientId` is truly required), you'll also
need to run `./gradlew downloadAniListApolloSchemaFromIntrospection` to sync the AniList GraphQL
schema.

Then a normal `./gradlew :app:installInternal` will install the full access internal build of
Anichive.

### Gradle

This project attempts to store the Gradle home and build caches directly inside the project root
under `/gradle-home` and `/build-cache`. This allows easy configurable of anti-virus scanning
exclusions and caching efforts, as all data is ideally read from just the project folder.

A minor benefit of this is that by deleting the two folders (along with `.gradle`), a true clean
build can be tested, where Gradle cannot reference anything it used previously.

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
