create table events
(
    id bigserial primary key,
    aggregate_id  uuid not null,
    payload  jsonb not null,
    revision integer not null
)
