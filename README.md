# FINT-KONTROLL-ORGUNIT-SERVICE

Denne tjenesten er en del av FINT-KONTROLL. Den erstatter FINT-KONTROLL-ORGUNIT-FACTORY og FINT-KONTROLL-ORGUNIT-CATALOG

## Beskrivelse
Tjenesten konsumerer organisasjonselementer fra Kafka. Disse er hentet fra administrasjon.organisasjon.organisasjonselement i FINT.
Disse organisasjonselementene blir mappet om til OrgUnit, lagret i en database og publisert på kafka som andre tjenester kan konsumere.

Tjenesten genererer også OrgUnitDistance entiteter og publiserer de ut til Kafka. OrgUnitDistance entiteter beskriver antall ledd i organisasjonsstrukturen mellom de ulike organisasjonsenhentene.


## Teknologi
Tjenesten er bygd på Spring Boot/kotlin og bruker Kafka for kommunikasjon mellom tjenester.

## OrgUnit modell

| Fieldname           | Type           | Nullable |
|---------------------|---------------|----------|
| id                  | Long          | Yes      |
| resourceId          | String        | No       |
| organisationUnitId  | String        | No       |
| name                | String        | No       |
| shortName           | String        | Yes      |
| parentRef           | String        | No       |
| managerRef          | String        | Yes      |
| childrenRef         | List<String>  | Yes      |


## OrgUnitDistance modell
| Fieldname       | Type           | Nullable |
|-----------------|---------------|----------|
| id              | Long          | Yes      |
| orgUnitId       | String        | No       |
| subOrgUnitId    | String        | No       |
| distance        | Int           | No       |

orgUnitId og subOrgUnitId er referanser til organisationUnitId på OrgUnit objectet.

## kafka topics
### konumerer
* .kontroll.entity.administrasjon-organisasjon-organisasjonselement

### publiserer
* .kontroll.entity.orgunit
* .kontroll.entity.orgunitdistance
