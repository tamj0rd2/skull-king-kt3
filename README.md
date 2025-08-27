# skull-king-kt3

## Notes to self:

### OTEL

To make OTEL work, set these environment variables before running the application:

- `JAVA_TOOL_OPTIONS` set to `-javaagent:adapters/web/server/build/agent/opentelemetry-javaagent.jar`
- `OTEL_SERVICE_NAME` set to `skull-king-kt3`

### Playwright

Playwright jvm is synchronous. Waiting on the main thread will cause playwright's event loop to pause. Causing pain.
So, being able to effectively react to things happening on the page (such as events) is a no-go. Don't bother.

### Mikado

- I want to try something out. Rather than the game notifications essentially being event notifications, I want to try
  making them Event Carried State Transfer. An annoying thing about the way the game currently works is that
  notification
  consumers need to rebuild the full state of the world on their side. What if notifications just gave small bit of
  information about what happened, but also passed along the full state? That would reduce a lot of annoying state
  rebuilding.
