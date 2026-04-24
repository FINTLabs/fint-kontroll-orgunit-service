# Arkitektur, Struktur Og Flyt

Dette dokumentet beskriver hvordan tjenesten er bygget opp, hvilke komponenter som har ansvar for hva, og hvordan data flyter gjennom systemet.

## Oversikt

Tjenesten har tre hovedansvar:

1. Ta inn organisasjonsdata fra Kafka og gjøre dem om til interne `OrgUnit`-entiteter.
2. Gjøre `OrgUnit` tilgjengelig både via Kafka og REST API.
3. Beregne og publisere avstandsrelasjoner (`OrgunitDistance`) i organisasjonshierarkiet.

## Filstruktur

Koden ligger under `src/main/kotlin/no/novari/fintkontrollorgunitservice` og er delt inn slik:

### Root

- `Application.kt`
  Starter Spring Boot og aktiverer scheduling.
- `CacheConfiguration.kt`
  Oppretter cacher for `OrganisasjonselementResource`, `OrgUnit` og `OrgunitDistance`.
- `FintLinkUtils.kt`
  Hjelpemetoder for å lese og normalisere FINT-lenker.
- `OpenApiConfig.kt`
  Swagger/OpenAPI-konfigurasjon.

### `organisasjonsenhet`

Innkommende FINT-organisasjonselementer.

- `OrganisasjonsenhetConsumer`
  Kafka-konsument for topic `administrasjon-organisasjon-organisasjonselement`.
- `OrganisasjonsenhetHandler`
  Validerer meldingen, mapper til `OrgUnit`, lagrer og publiserer videre.
- `OrganisasjonselementService`
  Leser parent, children og self-link fra FINT-ressursen.
- `NoSuchLinkException`
  Feiltype brukt når forventede FINT-lenker mangler.

### `orgunit`

Kjernemodellen og API-et.

- `OrgUnit`
  JPA-entiteten som lagres i databasen.
- `OrgUnitRepository`
  Databaseoperasjoner og hierarkioppslag.
- `OrgUnitMappingService`
  Mapping mellom FINT-ressurs, databaseentitet, Kafka DTO og API DTO.
- `OrgUnitService`
  Forretningslogikk for API og scope-filtrering.
- `OrgUnitController`
  REST-endepunkter under `/api/orgunits`.
- `OrgUnitResponsService`
  Bygger standardiserte API-responser.
- `OrgUnitPublishingService`
  Publiserer `OrgUnitKafkaDTO` til Kafka.
- `OrgUnitConsumer`
  Leser publiserte `OrgUnit` tilbake inn i cache.
- `OrgUnitSubOrgUnitService`
  Finner alle underordnede orgunits rekursivt i minne.
- `OrgUnitApiDTO`, `OrgUnitKafkaDTO`, `OrgUnitType`
  DTO-er og enum brukt av API og Kafka.

### `orgunitdistance`

Beregning og publisering av avstand i organisasjonshierarkiet.

- `OrgUnitDistancePublishingComponent`
  Scheduler som bygger og publiserer alle avstander.
- `OrgUnitDistanceService`
  Leser `OrgUnit` fra cache og bygger `OrgunitDistance`.
- `OrgUnitDistanceProducerService`
  Publiserer `OrgunitDistance` til Kafka.
- `OrgUnitDistanceConsumer`
  Leser publiserte avstander tilbake inn i cache.
- `OrgunitDistance`
  Datamodellen som publiseres.

### `kafka`

Gjenbrukbar Kafka-oppsett.

- `KafkaContainerFactory`
  Felles fabrikk for Kafka listener-containere som:
  - bygger topic-navn
  - setter listener-oppsett
  - lagrer konsumerte verdier i cache
  - sender videre til eventuell handler

## Komponentflyt

### 1. Inngående organisasjonselementer

```text
Kafka topic: administrasjon-organisasjon-organisasjonselement
    -> OrganisasjonsenhetConsumer
    -> organisasjonselementCache
    -> OrganisasjonsenhetHandler
    -> OrgUnitMappingService
    -> OrgUnitRepository
    -> OrgUnitPublishingService
    -> Kafka topic: orgunit
```

Detaljer:

- `OrganisasjonsenhetConsumer` leser meldingen fra Kafka.
- Meldingen lagres først i `organisasjonselementCache`.
- `OrganisasjonsenhetHandler` sjekker at `organisasjonsId.identifikatorverdi` finnes.
- `OrgUnitMappingService` bygger eller oppdaterer en `OrgUnit`.
- `OrgUnitRepository.save(...)` persisterer den i databasen.
- `OrgUnitPublishingService` publiserer en `OrgUnitKafkaDTO` til topic `orgunit`.

### 2. Publiserte orgunits tilbake i cache

```text
Kafka topic: orgunit
    -> OrgUnitConsumer
    -> publishedOrgunitCache
```

Dette gjør at tjenesten kan bruke publiserte orgunits som et raskt oppslag i minne, særlig for beregning av `OrgunitDistance`.

### 3. Beregning av orgunitdistance

```text
Scheduler
    -> OrgUnitDistancePublishingComponent
    -> OrgUnitDistanceService.getAllOrgUnits()
    -> publishedOrgunitCache
    -> bygg OrgunitDistance for hver orgunit
    -> OrgUnitDistanceProducerService
    -> Kafka topic: orgunitdistance
```

Detaljer:

- En scheduler kjører `publishOrgUnitDistance()`.
- Alle `OrgUnit` leses fra `publishedOrgunitCache`.
- For hver orgunit går tjenesten oppover i parent-kjeden.
- Det lages én `OrgunitDistance` for hvert nivå:
  - avstand `0` til seg selv
  - avstand `1` til parent
  - avstand `2` til grandparent
  - osv.
- Resultatet publiseres til topic `orgunitdistance`.

### 4. Publiserte orgunit-avstander tilbake i cache

```text
Kafka topic: orgunitdistance
    -> OrgUnitDistanceConsumer
    -> publishedOrgunitDistanceCache
```

## API-flyt

REST API-et ligger i `OrgUnitController`.

### `GET /api/orgunits`

```text
HTTP request
    -> OrgUnitController.getAllOrgUnits()
    -> OrgUnitService.findOrgunitsByScope()
    -> OrgUnitRepository
    -> OrgUnitMappingService
    -> OrgUnitResponsService
    -> HTTP response
```

Detaljer:

- Endepunktet støtter `search` og paging.
- `OrgUnitService` henter tillatte orgunits fra `AuthorizationClient`.
- Dersom brukeren har scope `ALLORGUNITS`, søkes det i alle orgunits.
- Hvis ikke, begrenses søket til orgunits i brukerens scope.

### `GET /api/orgunits/{id}`

Henter én `OrgUnit` direkte fra databasen via `OrgUnitService.findOrgUnitById()`.

### `GET /api/orgunits/{id}/parents`

Bruker en rekursiv SQL-query i `OrgUnitRepository.findParentOrgUnitsByOrganisationUnitId(id)` for å finne alle overordnede enheter.

### `GET /api/orgunits/{id}/children`

Bruker en rekursiv SQL-query i `OrgUnitRepository.findChildrenOrgUnitsByOrganisationUnitId(id)` for å finne alle underordnede enheter.

## Datakilder Og Lagring

### Database

`OrgUnit` er den eneste JPA-entiteten i databasen i denne tjenesten. Flyway-migreringer ligger i `src/main/resources/db/migration`.

### Cache

Tjenesten bruker tre cacher:

- `organisasjonselementCache`
  Holder sist konsumerte FINT-organisasjonselementer.
- `publishedOrgunitCache`
  Holder publiserte orgunits for raskt oppslag i minne.
- `publishedOrgunitDistanceCache`
  Holder publiserte avstander.

Cache brukes som arbeidsminne og støtte for Kafka-drevne flyter, ikke som primærkilde for API-data.

## Konfigurasjon

Viktige konfigurasjonsområder i `application.yaml`:

- `fint.*`
  FINT-identitet og autorisasjon.
- `novari.kafka.*`
  Kafka topic-prefix og application-id.
- `novari.cache.*`
  Cache TTL og størrelse.
- `spring.security.oauth2.resourceserver.jwt`
  JWT-validering.

Scheduling for orgunit-avstander styres også av properties brukt i `OrgUnitDistancePublishingComponent`:

- `fint.kontroll.orgunitdistance.publishing.initial-delay`
- `fint.kontroll.orgunitdistance.publishing.fixed-delay`

## Designvalg

### Kafka er integrasjonsbussen

Innlesning og videre distribusjon skjer via Kafka. Tjenesten både konsumerer eksterne data og republiserer interne representasjoner.

### Database er kilde for API-oppslag

REST API-et leser `OrgUnit` fra databasen, ikke fra cache.

### Cache støtter intern flyt

Cache brukes for:

- å holde konsumerte meldinger tilgjengelige
- å bygge opp hierarki i minne
- å gjøre periodiske avstandsberegninger billigere

### Mapping er skilt ut

`OrgUnitMappingService` samler oversettelsen mellom:

- FINT-modellen
- intern databaseentitet
- Kafka DTO
- API DTO
