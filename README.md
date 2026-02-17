# CareBridge Backend

CareBridge Backend er en Java Maven-baseret REST API bygget med Javalin, Hibernate og PostgreSQL. Projektet implementerer moderne Java-funktioner (Java 21) med fokus på sikkerhed, validering og testdækningsdækning.

## Teknologi Stack

Projektet benytter følgende teknologier:

- **Java 21**: Moderne Java med den nyeste features
- **Javalin 6.3.0**: Lightweight REST API framework
- **Hibernate 6.4.4**: ORM (Object-Relational Mapping) framework
- **PostgreSQL 42.7.3**: Relationel database
- **Jackson 2.15.0**: JSON serialisering og deserialisering
- **JWT 10.5**: JSON Web Tokens til authentication
- **BCrypt 0.4**: Sikker passwordhashing
- **Lombok 1.18.36**: Reducer boilerplate-kode
- **JUnit 5.10.2**: Testing framework
- **Logback 1.5.13**: Logging framework

## Forudsætninger

For at køre projektet skal du have installeret:

- Java Development Kit (JDK) 21 eller nyere
- Maven 3.8.0 eller nyere
- PostgreSQL 12 eller nyere
- Git

## Opsætning

Klone repositoriet og naviger til projektets root-mappe:

```bash
git clone <repository-url>
cd carebridge-backend
```

### Database-opsætning

Du har to muligheder for at sætte databasen op:

#### Lokal PostgreSQL (udvikling)

Installér PostgreSQL lokalt og opret en database:

```bash
createdb carebridge
```

#### Cloud Database på Neon.tech (anbefalet)

Neon.tech tilbyder gratis PostgreSQL-hosting i cloudmiljøet, hvilket er perfekt til udvikling og test. Fordele ved at bruge Neon:

- Gratis PostgreSQL-database
- Automatiske backups
- Nem branch-administration for forskellige teams og miljøer
- Skalerbar infrastruktur
- Ingen kreditkort påkrævet til gratis tier

**Sådan sætter du Neon op:**

1. Gå til [neon.tech](https://neon.tech) og opret en gratis konto
2. Opret et nyt projekt
3. Neon vil automatisk oprette en standard database
4. Under "Connection Details" finder du din forbindelses-string

**Opret branches til teams:**

I Neon kan du oprette forskellige branches for hver team eller miljø (udvikling, staging, produktion):

1. I Neon-dashboard, gå til "Branches"
2. Klik "Create branch"
3. Navngiv den efter dit team eller miljø (fx "team-frontend", "team-backend", "staging")
4. Hver branch får sin egen database og forbindelses-detaljer

**Fordele ved at bruge Neon branches:**

- Hvert team kan have isoleret arbejdsplads
- Nem kopiering af data mellem branches
- Uafhængige databaser uden påvirkning på andre teams
- Enkel branch-administration direkte fra Neon-dashboard

Opret en `.env` fil i root-mappen med dine databaseoplysninger (fra Neon eller lokal setup):

```
DB_HOST=your-project.neon.tech
DB_PORT=5432
DB_NAME=neondb
DB_USER=your_user
DB_PASSWORD=your_password
JWT_SECRET=din_hemmeligt_nøgle
```

For lokal PostgreSQL:

```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=carebridge
DB_USER=postgres
DB_PASSWORD=din_password
JWT_SECRET=din_hemmeligt_nøgle
```

Installér afhængigheder og byg projektet:

```bash
mvn clean install
```

## Kørsel

Start applikationen med Maven:

```bash
mvn clean compile exec:java
```

Eller byg en JAR-fil og kør den:

```bash
mvn clean package
java -jar target/carebridge-backend-1.0-SNAPSHOT.jar
```

API'en vil være tilgængelig på `http://localhost:8080` (standard Javalin-port).

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

## Features

- RESTful API med Javalin-framework
- JWT-baseret authentication
- Sikker password-hashing med BCrypt
- Hibernate ORM for databaseadgang
- Validering af input-data med Hibernate Validator
- Comprehensive testing med JUnit 5 og Testcontainers
- Logging med Logback
- Docker-support med Testcontainers til integration tests

## Testing

Kør alle tests:

```bash
mvn test
```

Kør specifikke testklasser:

```bash
mvn test -Dtest=Klassenavn
```

Projektet bruger Testcontainers til at køre PostgreSQL i isolerede Docker-containere under tests, hvilket sikrer reproducerbare testresultater.

## Database-management

### Neon Branch-strategi for Teams

Hvis dit team bruger Neon.tech kan du organisere branching sådan:

**Production branch:**
- Branch-navn: `production`
- Bruges til live-miljø
- Kræver approval før ændringer
- Daglig backup

**Staging branch:**
- Branch-navn: `staging`
- Test-miljø før produktion
- Spejler produktions-data
- Årlig opbygning fra produktion

**Team branches:**
- Branch-navn: `team-frontend`, `team-backend`, `team-devops`
- Hver team får isoleret database
- Uafhængig udvikling uden påvirkning på andre teams
- Mulighed for at synkronisere data fra main-branch når det er nødvendigt

**Feature branches:**
- Branch-navn: `feature/my-feature`
- Kortvarige branches for udvikling
- Automatisk slettet efter feature er færdig
- Nøjtig test af nye features før merge

**Sådan skifter du mellem branches i din applikation:**

Opdater din `.env` fil eller miljø-variabler til at pege på den relevante Neon branch forbindelses-detaljer. Hver branch i Neon får sin egen host og database-navn.

### Databasen under development

Under udvikling bruger projektet automatisk Docker-containere via Testcontainers:

```bash
mvn test
```

Dette sikrer at dine tests ikke påvirker udviklings-databasen og giver reproducerbare resultater hver gang.

### Database-migrationer

For større database-skema ændringer anbefales det at:

1. Opret en ny branch i Neon
2. Test dine skema-ændringer på branch-databasen
3. Verificer at applikationen fungerer som forventet
4. Merge til main-branch i Neon når det er bekræftet
5. Deploy til produktion med de nye skema-ændringer

## Maven Build

Byg projektet uden at køre tests:

```bash
mvn clean package -DskipTests
```

Byg med fuld test-dækningsdækning:

```bash
mvn clean package
```

## Dependency Management

Alle dependency-versioner styres centralt gennem properties i `pom.xml`. Dette gør det nemt at opdatere biblioteker globalt.

Vigtige afhængigheder kan opdateres ved at ændre versionsnummeret i properties-sektionen:

```xml
<javalin.version>6.3.0</javalin.version>
<hibernate.version>6.4.4.Final</hibernate.version>
<postgresql.version>42.7.3</postgresql.version>
```

## Compiler-konfiguration

Projektet er konfigureret til at generere Java 21 bytecode for maksimal kompatibilitet. Maven compiler plugin er indstillet til at bruge `<release>21</release>`, hvilket sikrer korrekt bytecode-generation uanset hvilken JDK-version der bruges til compilation.

Lombok-annotation-processoren er konfigureret til både main- og test-kompilering for at reducere boilerplate-kode.

## Kontribution

For at bidrage til projektet:

1. Fork repositoriet
2. Opret en feature-branch (`git checkout -b feature/MinFeature`)
3. Commit dine ændringer (`git commit -m 'Tilføj MinFeature'`)
4. Push til branchen (`git push origin feature/MinFeature`)
5. Åbn en Pull Request

Sørg for at alle tests passerer og at ny kode følger projektets kodestandarder.

## Licens

Dette projekt er licenseret under MIT-licensen. Se LICENSE-filen for detaljer.

## Support

Hvis du har spørgsmål eller støder på problemer, kan du åbne et issue på GitHub eller kontakte projektholdet.
