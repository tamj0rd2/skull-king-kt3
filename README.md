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
        - even better for now, just check that Cammy sees that the game is not empty
            - starting even simpler - cammy is creating a game. for now, let's just check that game is available in the
              list of games.
          - now, check to make sure the game was created by cammy
              - implement findAll
