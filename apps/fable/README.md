# Fable

Transform your social media into a cinematic storybook. Fable connects to your Instagram via the Meta Graph API, analyzes your posts and photos with GPT-4o, and generates a themed narrative — Romantic Comedy, Horror, or Sci-Fi — that can be downloaded as a PDF or ordered as a printed book via Lulu.

## Architecture

Two sub-apps:

- `service/` — Clojure JVM REST API (Pedestal + Component + PostgreSQL)
- `web/` — ClojureScript PWA (Reagent + Re-frame, shadow-cljs browser build)

The web app is a 6-step wizard: **Connect → Select Media → Choose Theme → Generate → Preview → Export**

---

## Prerequisites

- [Clojure CLI](https://clojure.org/guides/install_clojure)
- [Node.js](https://nodejs.org/) (v18+)
- [Docker](https://www.docker.com/) (for local PostgreSQL)
- A Meta developer app with Instagram permissions
- An OpenAI API key (GPT-4o access)

---

## Configuration

Copy the secrets template and fill in your credentials:

```bash
cp apps/fable/service/resources/secrets.edn.example apps/fable/service/resources/secrets.edn
```

`service/resources/secrets.edn`:

```edn
{:fable-db  {:username "fable"
             :password "changeme"
             :host     "localhost"}
 :meta      {:app-id     "YOUR_META_APP_ID"
             :app-secret "YOUR_META_APP_SECRET"}
 :openai    {:api-key "YOUR_OPENAI_API_KEY"}
 :lulu      {:client-id     "YOUR_LULU_CLIENT_ID"
             :client-secret "YOUR_LULU_CLIENT_SECRET"}
 :session   {:secret "CHANGE_ME_32_CHAR_SECRET_KEY_HERE"}
 :s3        {:bucket "your-fable-pdfs-bucket"}}
```

> `secrets.edn` is gitignored. Never commit it.

---

## Start Developing

### 1. Start PostgreSQL

```bash
cd apps/fable/service
docker-compose up postgres -d
```

### 2. Run database migrations

```bash
# From apps/fable/service/
clj -M:migratus migrate
```

### 3. Start the service

```bash
# From apps/fable/service/
clj -M:start
# Starts on http://localhost:8888
```

### 4. Install web dependencies

```bash
cd apps/fable/web
npm install
```

### 5. Start the web dev server

```bash
# From apps/fable/web/
clj -M:watch
# Starts on http://localhost:3000
```

Open [http://localhost:3000](http://localhost:3000) to see the wizard.

---

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/health` | — | Healthcheck |
| `GET` | `/auth/meta/init` | — | Begin Meta OAuth (redirects to Meta) |
| `GET` | `/auth/meta/callback` | — | OAuth callback, sets session cookie |
| `POST` | `/auth/logout` | ✓ | Clear session |
| `GET` | `/auth/status` | ✓ | `{:connected? bool :user-name str}` |
| `POST` | `/media/fetch` | ✓ | Fetch Instagram posts |
| `POST` | `/story/generate` | ✓ | Start async generation → `{:job-id}` |
| `GET` | `/story/job/:job-id` | ✓ | Poll job status + progress |
| `GET` | `/story/:id` | ✓ | Get completed storybook |
| `GET` | `/story/:id/pdf` | ✓ | Generate PDF, redirect to presigned S3 URL |
| `POST` | `/story/:id/print` | ✓ | Submit Lulu print order |
| `GET` | `/story/:id/print-status` | ✓ | Poll Lulu order status |

---

## Build & Deploy

### Service

```bash
# From apps/fable/service/
clj -M:uberdeps          # Build uberjar → target/app.jar
clj -M:push-image        # Push Docker image to ECR
clj -M:release           # Deploy to ECS
```

### Web

```bash
# From apps/fable/web/
clj -M:build             # Compile minified ClojureScript
clj -M:release           # Deploy to S3
```

### Docker (service only)

```bash
cd apps/fable/service
docker build -t fable:latest .
docker run -d -p 8888:8888 fable:latest
```

---

## Project Structure

```
apps/fable/
├── service/
│   ├── deps.edn
│   ├── Dockerfile
│   ├── docker-compose.yml
│   ├── resources/
│   │   ├── config.edn          # aero profile-based config
│   │   ├── secrets.edn         # gitignored credentials
│   │   └── migrations/
│   └── src/fable/
│       ├── core.clj            # -main, component start/stop
│       ├── system.clj          # component/system-map
│       ├── server.clj          # Pedestal Jetty server
│       ├── routes.clj          # route table
│       ├── middleware.clj      # session-auth + JSON interceptors
│       ├── auth/               # Meta OAuth + buddy-sign sessions
│       ├── meta/               # Instagram Graph API client
│       ├── ai/                 # OpenAI GPT-4o text + vision
│       ├── storybook/          # assembly, spec, PDF generation
│       ├── print/              # Lulu print API
│       ├── db/                 # next.jdbc queries
│       └── handlers/           # route handlers
└── web/
    ├── deps.edn
    ├── shadow-cljs.edn
    ├── package.json
    ├── resources/public/
    │   ├── index.html
    │   └── manifest.json       # PWA manifest
    └── src/cljs/fable/
        ├── app.cljs            # init, stylefy, mount
        ├── routes.cljs         # bidi routes, defmulti views
        ├── events.cljs         # :initialize-app, app-db shape
        ├── effects.cljs        # :navigate-to, :open-url
        ├── http.cljs           # http-fx helpers
        ├── wizard/             # step state machine
        ├── auth/               # connect screen
        ├── media/              # post grid selector
        ├── theme/              # genre picker
        ├── generate/           # progress screen + polling
        ├── preview/            # page-flip viewer
        ├── export/             # PDF download + print form
        ├── ui/                 # shared components + styles
        └── sw.cljs             # Workbox service worker
```
