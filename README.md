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

- start the game
    - introcue a player websocket to reduce duplication between join and create ws handlers.
