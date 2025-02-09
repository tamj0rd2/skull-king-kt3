create table lobby_revisions
(
    lobbyId         uuid not null primary key,
    latest_revision integer not null
)
