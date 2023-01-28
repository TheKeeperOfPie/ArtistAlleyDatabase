# Artist Alley Database

This app is designed to catalog art prints obtained from anime conventions in an easily searchable
format with support for tagging metadata like the series, characters, and artists of each piece.

This app includes an integration with the
[AniList API](https://anilist.gitbook.io/anilist-apiv2-docs/) to allow easy searching of
media/characters, and to provide images. Note that is app is **UNOFFICIAL** and is not supported or
endorsed by AniList.co whatsoever.

## Disclaimer

This app does not yet implement a toggle for AniList's `isAdult` flag, and all results are returned.
By using this application, you attest you are 18+ and comply with all AniList's content guidelines.
No downloads are supplied, and the app must be built from source.

There is no guarantee that imports and exports will work across versions until the format is
stabilized, nor that the data is safe from corruption or bugs. Use and update at your own risk.

## Build

1. Clone project with `git clone https://github.com/TheKeeperOfPie/ArtistAlleyDatabase.git`
2. Fetch the AniList GraphQL schema via  
  `./gradlew :app:downloadAniListApolloSchemaFromIntrospection`
3. Get a copy of any SNAPSHOT dependencies and place it into `libs`, currently:
   ```https://s01.oss.sonatype.org/content/repositories/snapshots/com/mxalbert/sharedelements/shared-elements/0.1.0-SNAPSHOT/shared-elements-0.1.0-20221204.093513-11.aar```
4. Install like any other Android application via `./gradlew :app:installDebug`

## Features

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
(If you draw anime art and are willing, please contact my GitHub email.)

## License

Eventually an open source license will be added to this repository, but for now you are only allowed
to clone and build the application as described above. No warranty, no support, etc.