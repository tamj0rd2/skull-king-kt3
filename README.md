# skull-king-kt3

## Notes to self:

Hamkrest cheat sheet - https://blog.gypsydave5.com/posts/2024/4/21/a-quick-guide-to-hamkrest/

### OTEL

To make OTEL work, set these environment variables before running the application:

- `JAVA_TOOL_OPTIONS` set to `-javaagent:adapters/web/server/build/agent/opentelemetry-javaagent.jar`
- `OTEL_SERVICE_NAME` set to `skull-king-kt3`

### Playwright

Playwright jvm is synchronous. Waiting on the main thread will cause playwright's event loop to pause. Causing pain.
So, being able to effectively react to things happening on the page (such as events) is a no-go. Don't bother.

### Testing

I have flip flopped between hamkrest and strikt for a while. So thought I'd start documenting the reasons why!

- Most recently, I switched from strikt to hamkrest because I didn't like that I couldn't specify custom error messages wherever I wanted.
- Now I'm switching back to strikt from hamkrest because while it's incredibly powerful, it's really hard and annoying to write. The compiler errors are awful, so even if the problem is down to user error, you can only fix things through trial and error. For example:

```kotlin
sealed interface OtherPlayerBids {
    data class Hidden(val playersWhoHavePlacedBids: Set<PlayerId>) : OtherPlayerBids
    data class Visible(val bids: Map<PlayerId, Bid>) : OtherPlayerBids
}

// somewhere else
assertThat(
    gameState.otherPlayerBids, isA<OtherPlayerBids.Hidden>() and has(OtherPlayerBids.Hidden::playersWhoHavePlacedBids, hasElement(player.id))
)
```

The error message over `assertThat`:
```
None of the following candidates is applicable:
fun <T> assertThat(actual: T, criteria: Matcher<T>, message: () -> String = ...): Unit
fun <T> assertThat(actual: T, criteria: KFunction1<T, Boolean>, message: () -> String = ...): Unit
```

It's possible to write the assertion in another way, but the output is not ideal. When has is used, it gives much better output. But without has, the output is on the same level as strikt.

```kotlin
assertThat(
    (gameState.otherPlayerBids as OtherPlayerBids.Hidden).playersWhoHavePlacedBids,
    hasElement(player.id),
) { "${id.value} does not see any indication that ${player.id.value} has bid" }
```

```
Ellis does not see any indication that Cammy has bid: expected: a value that contains PlayerId(value=Cammy)
but was {}
```

The custom error message tells me exactly what's wrong, but the rest is just weird cruft that doesn't make much sense.

That said, I think things would be fine if I used custom matchers. But if I'm going to use custom matchers, I might as well use strikt because I prefer its api and discoverability. So, I'm going to do exactly that. Switch back to strikt and write custom matchers where necessary.

### Mikado

- place bids
