# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This is a Clojure/ClojureScript monorepo of experimental tools and services. Apps live under `apps/`, shared libraries under `libs/`, and shared build/deployment tooling under `tools/`.

## App Types & Their Commands

There are two main app patterns:

### 1. ClojureScript Lambda Apps (acronym, contentful, guild, ranchify)
These compile to Node.js for AWS Lambda via shadow-cljs, deployed with Serverless Framework.

```bash
# From the app directory (e.g., apps/acronym/):
clj -M:watch          # Hot-reload ClojureScript during development
sls offline           # Run serverless locally (restart after each change)
clj -M:build          # Compile minified ClojureScript for prod
clj -M:deploy         # Deploy via Serverless Framework
```

> Note: serverless offline does NOT support dynamic code reloading — must restart after changes.

### 2. Clojure JVM Services (code_churn, team_tree, translation)
These are Pedestal/GraphQL HTTP services using Stuart Sierra's Component pattern.

```bash
# From the service directory (e.g., apps/code_churn/service/):
clj -M:start          # Start server in dev mode
clj -M:prod           # Start server in prod mode
clj -M:nrepl          # Start nREPL server

# Build & deploy to AWS ECS:
clj -M:uberdeps       # Build uberjar
clj -M:push-image     # Push Docker image to ECR
clj -M:release        # Deploy to ECS

# Docker:
docker build -t <app>:latest .
docker run -d -p 8888:8888 <app>
```

## Shared Infrastructure (libs/ and tools/)

### libs/
Shared code placed directly on the classpath of each app via `:paths` in `deps.edn`:
- `git_api/` — GitHub data access component (REST + GraphQL, exposes `git-api.core`)
- `repository/` — DB access layers (`code-churn-db`, `team-tree-db`)
- `router/` — ClojureScript routing (re-frame events)
- `rx_animation/` — ClojureScript animation utilities
- `promise/` — Cross-platform (`.cljc`) promise helpers
- `themes/` — Shared palette and typography (`.cljc`)

### tools/
Also on the classpath. Contains reusable build scripts invoked via `clj -M:<alias>`:
- `tools/build/cljs/` — shadow-cljs compilation (watch, minified build)
- `tools/build/serverless/` — Serverless Framework deploy/offline wrappers
- `tools/build/ecr/`, `tools/build/ecs/` — AWS container registry & ECS deployment
- `tools/build/s3/`, `tools/build/cloudfront/` — S3/CloudFront deployment
- `tools/env/` — Config loading via `aero` (reads `resources/config.edn` with profile keyword)

Apps include both directories in their `deps.edn` `:paths`:
```clojure
:paths ["src" "resources" "../../../libs" "../../../tools"]
```

## Key Architectural Patterns

- **Component lifecycle**: JVM services use `com.stuartsierra/component`. The system map is defined in `system.clj`, started in `core.clj` with `component/start-system`.
- **Config**: Apps use `aero` to read `resources/config.edn`, selecting a profile (`:dev`, `:prod`) passed as a CLI argument.
- **ClojureScript Lambdas**: shadow-cljs compiles to `:node-library` target, exporting a single `handler` fn. Output goes to `serverless/`.
- **Slack integration**: Several Lambda apps (acronym, contentful, ranchify) are Slack slash-command handlers — they receive a Slack payload and return Block Kit formatted responses.
- **GraphQL**: `code_churn` and `team_tree` expose GraphQL APIs via `lacinia-pedestal`.

## Other Apps

- `checkmarx_pdf` — CLI tool to generate PDF reports from Checkmarx security scan output
- `dev_love` — Full-stack app using Electric framework + XTDB
- `footsteps` — React Native pedometer mobile app (ClojureScript + shadow-cljs + Expo)
- `flutter_steps` — Flutter step counter app using ClojureDart
- `advent_of_code` — Advent of Code solutions
