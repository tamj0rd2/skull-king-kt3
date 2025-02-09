create table lobby_events
(
    lobbyId  uuid not null,
    payload  jsonb not null,
    revision integer not null
)
