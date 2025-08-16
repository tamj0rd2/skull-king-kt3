# skull-king-kt3

## Notes to self:

### OTEL

To make OTEL work, set these environment variables before running the application:

- `JAVA_TOOL_OPTIONS` set to `-javaagent:adapters/web/server/build/agent/opentelemetry-javaagent.jar`
- `OTEL_SERVICE_NAME` set to `skull-king-kt3`

### Mikado

- I wanted to write a test to prove that when Ellis joins the game, Cammy can see himself and Ellis in the game.
    - I did this, but there are way too many code changes. Start by checking that once Cammy joins the game, he can seem
      himself.
        - I need GameState to update once Cammy joins the game.
            - I need a mechanism to notify the player when something happens in the game.
