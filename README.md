# skull-king-kt3

## Notes to self:

### OTEL

To make OTEL work, set these environment variables before running the application:

- `JAVA_TOOL_OPTIONS` set to `-javaagent:adapters/web/server/build/agent/opentelemetry-javaagent.jar`
- `OTEL_SERVICE_NAME` set to `skull-king-kt3`

### Mikado

- I wanted to write a test to prove that when Ellis joins the game, Cammy can see himself and Ellis in the game.
    - check that when Ellis joins the game, he can seem himself and cammy.
        - when player joins, I need it to act like a catchup subscription so that they also receive relevant
          notifications from before they joined
            - the game needs to expose events so that they can easily be translated into notifications for catchup
              subscription
