# ADR 0001: Adopt JDA 6 + libdave-jvm for Discord DAVE voice encryption

- **Status:** Accepted
- **Date:** 2026-06-01

## Context

The bot joins Discord voice channels and records each participant's audio for
transcription. As of **2026-03-01**, Discord enforces the **DAVE protocol**
(Discord Audio & Video End-to-End Encryption, based on MLS) for all **non-stage**
voice connections. Clients that do not support DAVE are rejected by the voice
gateway.

Our pinned dependency, **JDA 5.2.1**, has no DAVE support. In practice this
produced an endless reconnect loop on every voice connection:

```
Close code: 4017   Reason: E2EE/DAVE protocol required
Close code: 4006   Reason: Session is no longer valid.
```

Because the bot's entire purpose is to *receive and record* voice, this is a hard
blocker, not a cosmetic warning. Two options were considered:

- **Option A — Record in a Stage channel.** Stage channels are explicitly exempt
  from DAVE (they use transport encryption only). This needs no code changes but
  forces a different, broadcast-style UX and is a permanent workaround rather than
  a fix.
- **Option B — Add DAVE support.** DAVE support landed in **JDA 6.3.0+** and
  requires a `DaveSessionFactory` implementation. Two implementations exist:
  - **JDAVE** — requires **Java 25** (FFM API).
  - **libdave-jvm** — JNI bindings to the official C++ `libdave`, **Java 8+**.

The service runs on **Java 21** (Temurin), which rules out JDAVE.

## Decision

Adopt **Option B**. Specifically:

- Upgrade `net.dv8tion/JDA` from **5.2.1 → 6.4.1**.
- Add **libdave-jvm** from `https://maven.lavalink.dev/snapshots`, pinned to commit
  `ce725965e` (the modules version in lockstep):
  - `moe.kyokobot.libdave/adapter-jda` — JDA ↔ DAVE bridge
  - `moe.kyokobot.libdave/impl-jni` — JNI Java bindings
  - `moe.kyokobot.libdave/natives-linux-x86-64` — the native `.so` (not pulled
    transitively; must be declared explicitly per platform)
- Wire DAVE into the `JDABuilder` in `discord/bot.clj` via
  `.setAudioModuleConfig(new AudioModuleConfig().withDaveSessionFactory(
  new LDJDADaveSessionFactory(new NativeDaveFactory())))`, with
  `NativeDaveFactory/ensureAvailable` as a fail-fast guard at startup.

`libdave-jvm` was chosen over JDAVE solely because of the Java 21 runtime; if the
service moves to Java 25+, JDAVE becomes a viable alternative.

## Consequences

**Positive**

- The bot can record standard (non-stage) voice channels again, past the mandatory
  enforcement date.
- The native binding was verified to load on `linux-x86-64` (glibc), which covers
  both WSL2 development and the Temurin Docker image; the uberjar bundles the
  natives artifact.
- No application code beyond `bot.clj` changed — the JDA 6 audio-receive and
  slash-command APIs are source-compatible with our usage.

**Negative / Risks**

- `libdave-jvm` is **early-stage** software pinned to a git commit hash. Versions
  must be bumped manually, and all four artifacts (adapter, impl, api, natives)
  must move to the same hash together.
- DAVE decryption on the **receive** path is the least-proven area across DAVE
  client libraries. End-to-end recording over a live encrypted call has **not**
  been verified; empty transcripts would be the symptom of a receive-side
  decryption failure.
- The native dependency is **platform-specific**. A non-glibc/non-x86-64 target
  (e.g. an Alpine/musl base image or ARM host) requires swapping in the matching
  `natives-*` artifact.

## References

- [A/V E2EE Enforcement for Non-Stage Voice Calls — Discord](https://support.discord.com/hc/en-us/articles/38749827197591-A-V-E2EE-Enforcement-for-Non-Stage-Voice-Calls)
- [JDA releases (DAVE support from 6.3.0)](https://github.com/discord-jda/JDA/releases)
- [libdave-jvm](https://github.com/KyokoBot/libdave-jvm)
