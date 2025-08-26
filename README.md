# skull-king-kt3

## Notes to self:

### OTEL

To make OTEL work, set these environment variables before running the application:

- `JAVA_TOOL_OPTIONS` set to `-javaagent:adapters/web/server/build/agent/opentelemetry-javaagent.jar`
- `OTEL_SERVICE_NAME` set to `skull-king-kt3`

### Mikado

- It would actually be much better if the Player wasn't so concerned about notifications. In the real game, the player
  won't exactly be notified. They'll just see the state change. So, the Player object should be more concerned with how
  to interpret the current state, rather than notifications.
