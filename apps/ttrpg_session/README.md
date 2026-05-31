# TTRPG Session Recorder

A Discord bot and processing service that records tabletop RPG voice sessions, transcribes them with OpenAI Whisper, summarises them with GPT-4, and publishes a static archive site to GitHub Pages.

## How it works

1. `/scribe start` — bot joins the voice channel and begins recording all participants
2. `/scribe stop` — recording ends and the pipeline runs automatically:
   - Each speaker's audio is downsampled (48kHz stereo → 16kHz mono) and transcribed via Whisper
   - Transcripts are merged chronologically and summarised by GPT-4
   - A session page (narrative, key events, notable quotes, full transcript) is published to GitHub Pages
   - The archive index is updated

---

## Local Development

### Prerequisites

- [Clojure CLI](https://clojure.org/guides/install_clojure)
- [Docker](https://docs.docker.com/get-docker/) (for PostgreSQL)
- A Discord bot token and server where you have admin access
- An OpenAI API key
- A GitHub Personal Access Token (PAT) with `Contents: Read and Write` on your site repo

### 1. Discord bot setup

1. Go to [discord.com/developers](https://discord.com/developers) and create a new application
2. Under **Bot**, enable **Server Members Intent** and reset the token — copy it
3. Under **OAuth2 → URL Generator**, select scopes `bot` and `applications.commands`
4. Bot permissions required: `Connect`, `Speak`, `Use Slash Commands`
5. Use the generated URL to invite the bot to your test server

### 2. GitHub Pages repo setup

Create a repo for your site (e.g. `city_of_exiles`) and initialise a `gh-pages` branch:

```bash
git clone https://github.com/YOUR_USERNAME/YOUR_SITE_REPO
cd YOUR_SITE_REPO
git checkout --orphan gh-pages
git rm -rf .
echo "<h1>placeholder</h1>" > index.html
git add index.html && git commit -m "init gh-pages"
git push origin gh-pages
```

Then enable GitHub Pages in the repo settings: **Pages → Source → gh-pages branch**.

Create a fine-grained PAT at [github.com/settings/tokens](https://github.com/settings/tokens) with `Contents: Read and Write` on the site repo.

### 3. Configure secrets

Edit `service/resources/secrets.edn` — this file is gitignored and stays on your machine only:

```edn
{:ttrpg-db {:username "ttrpg_session"
            :password "changeme"
            :host     "localhost"}
 :discord  {:token      "YOUR_BOT_TOKEN"
             :bot-secret "any-string-you-choose"}
 :openai   {:api-key "YOUR_OPENAI_KEY"}
 :github   {:token "YOUR_GITHUB_PAT"
             :owner "YOUR_GITHUB_USERNAME"
             :repo  "YOUR_SITE_REPO"}}
```

The `:bot-secret` is used to authenticate calls to the REST API — pick any string.

### 4. Configure the campaign roster

Edit `service/resources/roster.edn` to map Discord usernames to character names:

```edn
{:campaign "Your Campaign Name"
 :players  {"DiscordUsername1" "Character Name 1"
             "DiscordUsername2" "Character Name 2"}}
```

### 5. Start PostgreSQL and run migrations

From `apps/ttrpg_session/service/`:

```bash
docker compose up postgres -d
clj -M:migrate
```

### 6. Start the service

```bash
clj -M:start
```

The service starts on port 8888. The Discord bot will connect and register slash commands automatically.

### 7. Test a recording

1. Join a voice channel in your Discord server
2. Run `/scribe start` — the bot joins and begins recording
3. Talk for a minute or two
4. Run `/scribe stop` — the bot leaves and the pipeline runs in the background
5. Check the console output; when complete the session page will be live on GitHub Pages

---

## REST API

All endpoints except `/health` require the header `X-Bot-Secret: <your-bot-secret>`.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/health` | Returns `OK` |
| `GET` | `/sessions` | List all sessions |
| `GET` | `/sessions/:id` | Get session details and summary |
| `POST` | `/sessions/:id/publish` | Re-generate and re-publish a completed session |
| `POST` | `/roster/reload` | Reload `roster.edn` without restarting |

---

## Database schema

PostgreSQL is used to track session state through the pipeline and store AI outputs for republishing without re-calling the APIs.

| Table | Purpose |
|-------|---------|
| `recording_sessions` | Lifecycle of each recording (`recording` → `processing` → `done` / `error`) |
| `speaker_tracks` | Per-participant Whisper transcript JSON |
| `session_summaries` | GPT narrative, key events, notable quotes, published path |

---

## Production Deployment

> TODO: document the production deployment process

The service is containerised. The Dockerfile produces an uberjar image based on `eclipse-temurin:21-jre`.

In production all secrets are supplied as environment variables — `secrets.edn` is not used. The following variables must be set:

| Variable | Description |
|----------|-------------|
| `DISCORD_TOKEN` | Discord bot token |
| `BOT_SECRET` | Secret for REST API auth |
| `OPENAI_API_KEY` | OpenAI API key |
| `DB_HOST` | PostgreSQL host |
| `DB_USER` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `GITHUB_TOKEN` | GitHub PAT for publishing |
| `GITHUB_OWNER` | GitHub username or org |
| `GITHUB_REPO` | Site repository name |

```bash
# Build the image (run from the monorepo root)
docker build -f apps/ttrpg_session/service/Dockerfile -t ttrpg-session:latest .

# Run with environment variables
docker run -d -p 8888:8888 \
  -e DISCORD_TOKEN=... \
  -e OPENAI_API_KEY=... \
  -e GITHUB_TOKEN=... \
  ttrpg-session:latest
```
