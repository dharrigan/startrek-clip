--
-- STARSHIPs
--
-- NOTE: PostgreSQL 13 or above is required (for the gen_random_uuid() function)
--

CREATE TABLE IF NOT EXISTS starship (
    starship_id BIGINT PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY,
    uuid        UUID                     NOT NULL DEFAULT gen_random_uuid(),
    created     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    captain     TEXT                     NOT NULL,
    affiliation TEXT                     NOT NULL,
    launched    SMALLINT                 NOT NULL,
    class       TEXT                     NOT NULL,
    registry    TEXT                     NOT NULL,
    image       TEXT,
    UNIQUE (captain, affiliation, launched, class, registry)
);

CREATE INDEX IF NOT EXISTS starship_created_idx ON starship USING btree (created);
CREATE INDEX IF NOT EXISTS starship_captain_idx ON starship USING btree (captain);
CREATE INDEX IF NOT EXISTS starship_affiliation_idx ON starship USING btree (affiliation);
CREATE INDEX IF NOT EXISTS starship_launched_idx ON starship USING btree (launched);
CREATE INDEX IF NOT EXISTS starship_class_idx ON starship USING btree (class);
CREATE INDEX IF NOT EXISTS starship_registry_idx ON starship USING btree (registry);

INSERT INTO starship (uuid, captain, affiliation, launched, class, registry, image)
VALUES ('a1aa4887-d21b-4ef3-9c85-e864641b18b0', 'Jonathan Archer', 'United Federation of Planets', 2151, 'NX', 'NX-01', 'nx01.jpg'),
       ('a253bda2-fd15-479d-a780-85271a364107', 'James T. Kirk', 'United Federation of Planets', 2245, 'Constitution', 'NCC-1701', 'ncc1701.jpg'),
       ('ba91976e-a01f-4165-957a-f5d5d50ea7a3', 'James T. Kirk', 'United Federation of Planets', 2286, 'Constitution', 'NCC-1701-A', 'ncc1701-a.jpg'),
       ('e8d715d0-f024-4f2a-a30a-e470b6a3115c', 'John Harriman', 'United Federation of Planets', 2293, 'Constitution', 'NCC-1701-B', 'ncc1701-b.jpg'),
       ('9d5b09e7-edc0-4fd1-94de-c19ea0a28198', 'Rachel Garrett', 'United Federation of Planets', 2332, 'Constitution', 'NCC-1701-C', 'ncc1701-c.jpg'),
       ('67e21206-85bc-415f-9cc3-fc9a981a6248', 'Jean-Luc Picard', 'United Federation of Planets', 2363, 'Constitution', 'NCC-1701-D', 'ncc1701-d.jpg'),
       ('75ef1df5-38a6-455a-829e-f4266c6e3e47', 'Jean-Luc Picard', 'United Federation of Planets', 2372, 'Constitution', 'NCC-1701-E', 'ncc1701-e.jpg');

--
-- END
--
