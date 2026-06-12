### Starting the dev server

- `./gradlew -PwasmDebug=true :modules:alley-discord:webRelease` to build the Worker code
- Navigate to `build/dist/web/development`
- Launch the Worker using `bunx wrangler dev`
- Use https://ngrok.com with `ngrok http 8787` to proxy to the local server
- Use the test Discord server to send commands to the Canary bot
- Send `http://localhost:8787/database` with the body being a valid social link for testing

### Syncing commands
- With the local dev server running, make a request using https://hoppscotch.io to localhost:8787
for `/commands`
- This will have to be manually repeated for the production bot credentials

### Publishing

- `./gradlew :modules:alley-discord:webRelease` without the debug flag
- Copy `build/dist/web/production` to the `ArtistAlleyDiscordBot` repo
- `git add -A` and make a new commit with the message pointing to this repo's commit SHA
- Wait for Cloudflare to upload the Worker and then test it in the prod Discord server

### Authorizing
- Open the Discord developer portal for the corresponding bot
- Select the following scopes
  - `connections`
  - `bot`
  - `messages.read`
  - `applications.commands`
- Enable the following permissions in the forum/public channels
  - `Manage Roles` 
  - `Send Messages` 
  - `Create Public Threads`
  - `Send Messages in Threads`
  - `Manage Messages`
  - `Pin Messages`
  - `Manage Threads`
  - `Embed Links`
  - `Attach Files`
  - `Read Message History`
  - `Add Reactions`
