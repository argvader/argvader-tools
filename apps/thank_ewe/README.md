## Thank-ewe Bot

Slack companion for sharing gratitude. The service is inspired by the
`contentful` and `ranchify` apps in this repo and compiles ClojureScript to
Node.js with `shadow-cljs`.

### Slash commands

- `/thank-ewe` → same as `/thank-ewe leaderboard`
- `/thank-ewe leaderboard` → renders a table-based leaderboard
- `/thank-ewe list date [YYYY-MM-DD|now]` → shows entries for a date (defaults to today)
- `/thank-ewe @slack-profile reason` → records a new thank-you with the reason

### Local workflows

```
cd apps/thank_ewe
clj -M:watch                 # shadow cljs watch build
cd serverless && npx sls offline
```

Set `THANK_EWE_SLACK_SECRET` locally to make signature validation succeed.
