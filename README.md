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
### consumer
* .kontroll.entity.administrasjon-organisasjon-organisasjonselement

| Property                                |
|-----------------------------------------|
| .continueFromPreviousOffsetOnAssignment |

```json
Eksempel på organisasjonselement:
{
    "gyldighetsperiode": {
        "slutt": "2030-12-31T00:00:00Z",
        "start": "2023-01-01T00:00:00Z"
    },
    "kortnavn": "FAK",
    "navn": "FAK Finans og administrasjon",
    "organisasjonsId": {
        "identifikatorverdi": "5"
    },
    "organisasjonsKode": {
        "identifikatorverdi": "V30"
    },
    "organisasjonsnavn": "FAK Finans og administrasjon",
    "organisasjonsnummer": {
        "identifikatorverdi": "5"
    },
    "_links": {
        "overordnet": [
            {
                "href": "https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/1"
            }
        ],
        "underordnet": [
            {
                "href": "https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/26"
            },
            {
                "href": "https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/36"
            }
        ],
        "leder": [
            {
                "href": "https://beta.felleskomponent.no/administrasjon/personal/personalressurs/ansattnummer/1002"
            }
        ],
        "arbeidsforhold": [
            {
                "href": "https://beta.felleskomponent.no/administrasjon/personal/arbeidsforhold/systemid/5dad7056-d9bd-4c7d-ae3c-09e16344ab5d"
            },
            {
                "href": "https://beta.felleskomponent.no/administrasjon/personal/arbeidsforhold/systemid/2d1c057e-6e85-4e40-8937-3abec7233db1"
            },
            {
                "href": "https://beta.felleskomponent.no/administrasjon/personal/arbeidsforhold/systemid/ed3f08da-f4e8-40dd-8547-cfaeb6897fc5"
            }
        ],
        "self": [
            {
                "href": "https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/5"
            },
            {
                "href": "https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonskode/V30"
            }
        ]
    }
}
```

* .kontroll.entity.orgunit

| Property           |
|--------------------|
| .seekToBeginningOnAssignment              |

```json
Eksempel: se producuser
```

### producer
* .kontroll.entity.orgunit

| Property                                |
|-----------------------------------------|
| .lastValueRetainedForever |

```json
Eksempel på orgUnit
{
    "id": 3,
    "resourceId": "https://beta.felleskomponent.no/administrasjon/organisasjon/organisasjonselement/organisasjonsid/26",
    "organisationUnitId": "26",
    "name": "OKO Økonomiavdeling",
    "shortName": "OKO",
    "parentRef": "5",
    "managerRef": "[https://beta.felleskomponent.no/administrasjon/personal/personalressurs/ansattnummer/1005]",
    "childrenRef": [
        "27",
        "30"
    ],
    "allSubOrgUnitsRef": [
        "27",
        "30"
    ]
}
```

* .kontroll.entity.orgunit-distance

| Property                                |
|-----------------------------------------|
| .lastValueRetainedForever |

```json
Eksempler på orgUnitDistance
{
   "id": "89_89",
   "orgUnitId": "89",
   "subOrgUnitId": "89",
   "distance": 0
}
og
{
   "id": "84_89",
   "orgUnitId": "84",
   "subOrgUnitId": "89",
   "distance": 1
}
```




## api
Dokumentert med Swagger, se http://localhost:\<port\>/swagger-ui/index.html

