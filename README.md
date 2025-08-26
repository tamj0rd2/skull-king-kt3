# skull-king-kt3

## Notes to self:

### OTEL

To make OTEL work, set these environment variables before running the application:

- `JAVA_TOOL_OPTIONS` set to `-javaagent:adapters/web/server/build/agent/opentelemetry-javaagent.jar`
- `OTEL_SERVICE_NAME` set to `skull-king-kt3`

### Mikado

- Rather than creating some kind of player specific state in the tests, I want to try promoting that to a use case. It
  doesn't seem unreasonable to want to figure out what the game looks like to a specific player.
