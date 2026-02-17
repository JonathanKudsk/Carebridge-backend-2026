# CareBridge Backend

CareBridge Backend er en Java Maven-baseret REST API lavet med Javalin, Hibernate og PostgreSQL. Projektet bruger moderne Java-features (Java 21) med fokus på sikkerhed, validering og solid test-dækning.

## Tech Stack

Projektet benytter disse teknologier:

- **Java 21**: Nyeste Java-features
- **Javalin 6.3.0**: Et lightweight framework til REST API'er
- **Hibernate 6.4.4**: Håndterer databasekommunikationen for dig
- **PostgreSQL 42.7.3**: Relationel database
- **Jackson 2.15.0**: Konverterer mellem Java-objekter og JSON
- **JWT 10.5**: Sikkerhed via tokens
- **BCrypt 0.4**: Sikker password-hashing
- **Lombok 1.18.36**: Reducerer boilerplate-kode
- **JUnit 5.10.2**: Test-framework
- **Logback 1.5.13**: Logging

## Det skal du have på din maskine

Du skal have disse værktøjer installeret for at arbejde med projektet:

- Java Development Kit (JDK) 21 eller nyere
- Maven 3.8.0 eller nyere
- PostgreSQL 12 eller nyere (hvis du bruger lokal database)
- Git

## Kom i gang

Start med at klone repositoriet:

```bash
git clone <repository-url>
cd carebridge-backend
```

### Sæt databasen op

Du kan vælge mellem to løsninger:

#### Hvis du arbejder lokalt med PostgreSQL

Installér PostgreSQL og opret en database:

```bash
createdb carebridge
```

#### Hvis du bruger Neon.tech (det er gratis og anbefalet)

Neon.tech giver dig en gratis PostgreSQL-database i cloudmiljøet, som er super praktisk til udvikling og test. Her er nogle fordele:

- Gratis PostgreSQL-database
- Automatiske backups
- Let at lave branches for forskellige teams og miljøer
- Du får skalerbarhed uden at skulle tænke på servere
- Helt gratis til den mængde data i skal gemme

**Sådan starter du med Neon:**

1. Hop ind på [neon.tech](https://neon.tech) og opret en gratis konto
2. Lav et nyt projekt
3. Neon laver automatisk en database til dig
4. Find din forbindelses-string under "Connection Details"

**Opret branches til dine teams:**

I Neon kan du lave forskellige branches til hvert team eller miljø (udvikling, staging, produktion osv.):

1. Åbn Neon-dashboardet og gå til "Branches"
2. Tryk "Create branch"
3. Navngiv den efter dit team eller miljø (f.eks. "team-frontend", "team-backend", "staging")
4. Hver branch får sin egen database og forbindelses-detaljer

**Hvorfor Neon branches er fede:**

- Hvert team arbejder i sin egen isoleret database
- Ingen risiko for at ændringer påvirker andre teams
- Du kan nemt synkronisere data mellem branches når det skal til
- Hele branch-administrations sker direkte i Neon-dashboardet

Opret en `.env` fil i rod-mappen med dine database-oplysninger. Hvis du bruger Neon, finder du værdierne i Neon-dashboardet:

```
DB_HOST=your-project.neon.tech
DB_PORT=5432
DB_NAME=neondb
DB_USER=your_user
DB_PASSWORD=your_password
JWT_SECRET=din_hemmeligt_nøgle
```

Eller hvis du arbejder lokalt:

```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=carebridge
DB_USER=postgres
DB_PASSWORD=din_password
JWT_SECRET=din_hemmeligt_nøgle
```

Download dependencies og byg projektet:

```bash
mvn clean install
```

## Start applikationen

Kør den med Maven:

```bash
mvn clean compile exec:java
```

Eller byg den som en JAR-fil først og kør den bagefter:

```bash
mvn clean package
java -jar target/carebridge-backend-1.0-SNAPSHOT.jar
```

Så er API'en klar på `http://localhost:8080` (Javalin's default port).

## Projektstruktur

```
carebridge-backend/
├── src/
│   ├── main/java/com/carebridge/
│   │   ├── controllers/        # REST endpoint-definitioner
│   │   ├── models/            # Entity-klasser og DTOs
│   │   ├── services/          # Forretningslogik
│   │   ├── repositories/      # Database access layer
│   │   ├── security/          # Authentication og JWT-håndtering
│   │   └── utils/             # Hjælpeklasser
│   └── test/java/com/carebridge/  # Testklasser
├── pom.xml                    # Maven-konfiguration
└── README.md                  # Denne fil
```

## Hvad projektet kan

- RESTful API med Javalin
- JWT-baseret authentication
- BCrypt til sikker password-hashing
- Hibernate ORM til database-kommunikation
- Input-validering med Hibernate Validator
- Ordentlig test-suite med JUnit 5 og Testcontainers
- Logging via Logback
- Docker-support til integration tests med Testcontainers

## Test alt

Kør alle tests:

```bash
mvn test
```

Eller hvis du kun vil teste en bestemt klasse:

```bash
mvn test -Dtest=Klassenavn
```

Projektet bruger Testcontainers til at spin up PostgreSQL i Docker under testene, så du får virkelig test-miljøer uden at påvirke din udviklings-database.

## Database-management

### Branch-strategi med Neon for dit team

Hvis I bruger Neon kan I organisere branches sådan her:

**Production branch:**
- Navn: `production`
- Det der kører live
- Skal være stabil
- Daglige backups automatisk

**Staging branch:**
- Navn: `staging`
- Hvor du tester før den går live
- Spejler produktions-data
- Yearly refresh fra production

**Team branches:**
- Navn: `team-frontend`, `team-backend`, `team-devops`
- Hver team får sin egen database
- De kan udvikle uden at påvirke hinanden
- Data kan synkroniseres når det skal til

**Feature branches:**
- Navn: `feature/my-feature`
- Kortvarige branches mens I udvikler
- Slettes efter feature er færdig
- Perfekt til at teste nye features i isolation

**Skift mellem branches i applikationen:**

Du skifter mellem branches ved at opdatere `.env` fil eller environment variables til at pege på den branch du vil bruge. Hver Neon branch får sin egen connection-string.

### Database under udvikling

Under udvikling bruges Docker-containere automatisk via Testcontainers når du kører tests:

```bash
mvn test
```

Det betyder dine tests ikke røre ved udviklings-databasen, og du får de samme resultater hver gang.

### Hvis du skal ændre databasen

Når der skal ske større ændringer i database-skemaet:

1. Opret en ny branch i Neon
2. Test dine ændringer på den branch
3. Verificer at applikationen virker som den skal
4. Merge til main når alt virker
5. Deploy til production med de nye ændringer

## Build-kommandoer

Byg uden at køre tests (når du er stresset for tid):

```bash
mvn clean package -DskipTests
```

Byg med fuld test-dækning:

```bash
mvn clean package
```

## Dependency-versioner

Alle library-versionerne styres på et sted i `pom.xml` properties-sektionen. Det gør det super nemt når der kommer nye versioner - du skifter bare ét tal et sted.

For eksempel, hvis du vil opdatere Javalin:

```xml
<javalin.version>6.3.0</javalin.version>
<hibernate.version>6.4.4.Final</hibernate.version>
<postgresql.version>42.7.3</postgresql.version>
```

## Compiler-setup

Projektet er indstillet til at lave Java 21 bytecode, så det virker på alle maskiner. Maven compiler plugin bruger `<release>21</release>`, hvilket betyder du får det samme output uanset hvilken JDK-version du har.

Lombok bruger annotation processors under både main- og test-compilation, så boilerplate-kode bliver genereret automatisk.

## Bidrag til projektet

Hvis du vil hjælpe til:

1. Fork repositoriet
2. Lav din egen branch (`git checkout -b feature/MinFeature`)
3. Commit dine ændringer (`git commit -m 'Tilføj MinFeature'`)
4. Push til din branch (`git push origin feature/MinFeature`)
5. Åbn en Pull Request

Husk at alle tests skal passere og at ny kode skal følge projektets kodestil.


## Hjælp og spørgsmål

Hvis noget er uklart eller der er problemer, kan du åbne et issue på GitHub eller kontakte teamet direkte.
