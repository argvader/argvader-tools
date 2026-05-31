# Scion — Multi-Agent Development Guide

[Scion](https://github.com/GoogleCloudPlatform/scion) is an open-source orchestration framework from Google Cloud that runs AI coding agents (Claude Code, Gemini CLI, and others) in isolated containers with their own git worktrees. This document covers how to use it within this monorepo.

> **Status**: Scion local mode is relatively stable. Hub (multi-machine) and Kubernetes support are experimental.

---

## How it fits this monorepo

Each app under `apps/` is independent enough to be worked on by a dedicated agent without touching other apps. Scion's git worktree isolation makes this safe: agent A working on `apps/ttrpg_session` and agent B working on `apps/code_churn` get separate worktrees and cannot conflict with each other or with your main working tree.

Shared code under `libs/` and `tools/` is read-only for agents unless you explicitly task them with modifying it.

---

## Installation

Requires Go.

```bash
go install github.com/GoogleCloudPlatform/scion/cmd/scion@latest
```

**One-time machine setup** (run once per machine):

```bash
scion init --machine
```

This configures the container runtime for your OS:
- macOS → Apple Container (default)
- Linux / Windows → Docker (default)
- Alternative: Podman on Linux

**Project setup** (run once at the monorepo root):

```bash
cd /path/to/argvader-tools
scion init
```

This creates a `.scion/` directory. Add the agents subdirectory to `.gitignore`:

```
# .gitignore
.scion/agents
```

---

## Core concepts

| Concept | What it is |
|---|---|
| **Agent** | A running container with an AI harness (Claude Code, etc.) attached to a git worktree |
| **Worktree** | An isolated checkout of this repo that the agent works in — changes don't touch your branch until you merge |
| **Template** | A named agent role defined with a system prompt and optional skill set |
| **Harness** | The AI tool running inside the container (Claude Code is the harness used in this repo) |
| **Broker** | The machine offering container runtimes to Scion |

---

## Starting an agent

```bash
# Start an agent with a task and immediately attach to watch it
scion start <agent-name> "<task description>" --attach

# Start in the background; attach later
scion start <agent-name> "<task description>"
scion attach <agent-name>

# Start on a specific branch
scion start <agent-name> "<task>" --branch feature/my-branch

# Start with a specific template (see Templates section below)
scion start <agent-name> "<task>" --type <template-name>

# Start with a specific container image
scion start <agent-name> "<task>" --image <image-name>
```

### Examples for this monorepo

```bash
# Work on the ttrpg_session service
scion start ttrpg-dev "Add a retry mechanism to the publisher when GitHub API returns 429" --attach

# Work on a shared library
scion start libs-dev "Add a promise/timeout helper to libs/promise" --attach

# Run two agents in parallel on different apps
scion start churn-dev "Refactor the GraphQL resolvers in apps/code_churn/service" 
scion start tree-dev "Add pagination to the team_tree query"
```

---

## Managing agents

```bash
# List all running agents
scion list
# alias
scion ps

# Send a follow-up message to a running agent
scion message <agent-name> "Also make sure the spec validation covers nil inputs"
# alias
scion msg <agent-name> "..."

# View agent output logs
scion logs <agent-name>

# Attach to an agent's terminal session
scion attach <agent-name>

# Stop an agent (container paused, worktree preserved)
scion stop <agent-name>

# Resume a stopped agent
scion resume <agent-name>

# Delete an agent completely (removes container and worktree)
scion delete <agent-name>
```

---

## Templates

Templates define reusable agent roles with custom system prompts and skill sets. Store them in `.scion/` at the monorepo root.

### Suggested templates for this repo

#### Clojure JVM service agent

Useful for work on `apps/code_churn`, `apps/team_tree`, `apps/translation`, and `apps/ttrpg_session`.

```bash
scion start jvm-dev "Add caching to the whisper transcription step in ttrpg_session" \
  --type clojure-jvm
```

Template definition (`.scion/templates/clojure-jvm.yaml`):

```yaml
name: clojure-jvm
description: Agent for Clojure JVM services using Pedestal, Component, and next.jdbc
system_prompt: |
  You are working on a Clojure JVM service in a monorepo.
  The service uses Pedestal for HTTP, Stuart Sierra Component for lifecycle,
  next.jdbc for database access, and aero for config.
  Follow the project CLAUDE.md conventions exactly:
  - kebab-case naming, side-effect functions end in !
  - Layer namespaces as handler -> service -> db
  - Use ex-info at service boundaries, catch at handler layer
  - All stateful resources must implement component/Lifecycle
  Shared libraries are in libs/ and tools/ on the classpath.
```

#### ClojureScript Lambda agent

Useful for `apps/acronym`, `apps/contentful`, `apps/guild`, `apps/ranchify`.

```yaml
name: clojurescript-lambda
description: Agent for ClojureScript Lambda apps using shadow-cljs and Serverless Framework
system_prompt: |
  You are working on a ClojureScript Lambda app in a monorepo.
  The app compiles to Node.js via shadow-cljs with :target :node-library.
  It is deployed with Serverless Framework (sls deploy).
  Use re-frame for state, reg-event-fx for effects, reg-event-db for pure updates.
  Shared libraries are in libs/ and tools/ on the classpath.
  Never write raw JS in Clojure files. Use js/ prefix and clj->js/js->clj at boundaries.
```

#### Reviewer agent

For reviewing PRs or auditing code across the whole repo without making changes.

```yaml
name: reviewer
description: Read-only agent for code review and audit tasks
system_prompt: |
  You are a code reviewer. Your job is to read code, identify issues, and produce
  a written report. Do not modify any files. Focus on:
  - Correctness and edge cases
  - Security (injection, auth, secrets in code)
  - Adherence to project conventions in CLAUDE.md
  Report findings in markdown with severity levels (high/medium/low).
```

Start a review:

```bash
scion start review-ttrpg "Review the entire ttrpg_session service for security issues" \
  --type reviewer --attach
```

---

## `scion start` flag reference

| Flag | Short | Description |
|---|---|---|
| `--type` | `-t` | Template to use |
| `--image` | `-i` | Container image (overrides template) |
| `--attach` | `-a` | Attach to the agent TTY immediately after start |
| `--branch` | `-b` | Git branch for the agent's worktree |
| `--workspace` | `-w` | Host path to mount as `/workspace` |
| `--harness` | | Named harness config (e.g. `claude-code`) |
| `--harness-auth` | | Auth method: `api-key`, `oauth-token`, `auth-file`, `vertex-ai` |
| `--no-auth` | | Disable auth propagation to the container |
| `--config` | | Path to inline agent config file (YAML/JSON) |
| `--broker` | | Preferred runtime broker ID or name |
| `--enable-telemetry` | | Enable OTEL telemetry for this agent |
| `--disable-telemetry` | | Disable OTEL telemetry for this agent |
| `--no-notify` | | Do not subscribe to notifications for this agent |
| `--upload-template` | | Auto-upload local template to Hub if not found |
| `--template-scope` | | Scope for uploaded template: `global`, `project`, `user` |

---

## Parallel workflows

The primary value in this monorepo is running agents on independent apps simultaneously.

```bash
# Scenario: prepare a release across three services at once
scion start ttrpg-release  "Bump version to 0.2.0 and update CHANGELOG in apps/ttrpg_session"
scion start churn-release  "Bump version to 1.4.0 and update CHANGELOG in apps/code_churn"
scion start tree-release   "Bump version to 1.2.0 and update CHANGELOG in apps/team_tree"

# Check on all of them
scion list

# Send a correction to one
scion msg ttrpg-release "The version should be 0.2.1 not 0.2.0, fix that"

# Attach to review work before merging
scion attach ttrpg-release
```

Each agent's changes live in its own git worktree. Review and merge each independently once done.

---

## Observability (OTEL)

Scion emits normalised OpenTelemetry telemetry across agent harnesses for logging and metrics.

```bash
# Start an agent with telemetry enabled
scion start ttrpg-dev "..." --enable-telemetry
```

Configure your OTEL collector endpoint in `.scion/settings.json` to aggregate across agents.

---

## Settings

Override defaults in `.scion/settings.json` at the repo root:

```json
{
  "runtime": "docker",
  "harness": "claude-code"
}
```

Common runtime values: `docker`, `podman`, `apple-container`, `kubernetes`.

---

## Hub (multi-machine, optional)

Hub is a central control plane for distributing agents across multiple machines. It is experimental (~80% verified). For local development on this monorepo, local mode is sufficient — no Hub setup required.

---

## Workflow summary

```
1. scion init --machine       # once per machine
2. scion init                 # once at repo root
3. scion start <name> "<task>" [--type <template>] [--attach]
4. scion msg <name> "<follow-up>"
5. scion attach <name>        # review output
6. scion delete <name>        # clean up when done
7. git merge <worktree-branch> # bring changes into your branch
```
