## Excercise Analytics Server

### Usage

This is a normal sbt project. You can compile code with `sbt compile`, run it
with `sbt run`, and `sbt console` will start a Scala 3 REPL.

The server requires a PostgresQL database with the correct structure. You can
start an empty database with `docker compose up -d` which will use the provided
`docker-compose.yml` file and scripts to make the system ready. This has been
configured to use nonstandard port 5433 so it doesn't conflict with any
preexisting deployments.

Some application options can be configured via `application.conf`, including
database access and server port. By default the application will start on
`0.0.0.0:8080` so pointing any requests there (or localhost) should work.

#### Client
The `client` module can be run in order to insert random sample data. This can
be done with `sbt "client/run"`.

You can configure the client via its `application.conf` file. The main configurations are:
  - **root-host**: Root for calls. This is where the server is expected to be. By default `http://localhost:8080`
  - **data-points**: Number of data points to be created. Application will make a call per datapoint
  - **max-user-id**: User ids will be all positive and below this number. Mostly for sanity.
  - **max-par-calls**: Maximum number of calls to make to the server in parallel

The client also allows overriding the configs via the following environment variables:
  - `ROOT_HOST`: Overrides **root-host**
  - `DATA_POINTS`: Overrides **data-points**
  - `MAX_USER_ID`: Overrides **max-user-id**
  - `MAX_PAR_CALLS`: Overrides **max-par-calls**

The client will generate a random timestamp after the epoch and before the
current time. It will generate a user from 0 to `abs(max-user-id)` to keep it
non-negative. It will select randomly and roughly equally between `click` and
`impression`.

### Exposed endpoints
  - POST /analytics?timestamp={millis_since_epoch}&user={user_id}&event={click|impression}
  - GET /analytics?timestamp={millis_since_epoch}
  - GET /analyticsJson?timestamp={millis_since_epoch}

The difference between the GET endpoints is the output. `/analytics` will return
the queried data in plain text in a requested format. `analyticsJson` will
return the data as `application/json` with the full data, including the bounds
of the time range.

### Structure

The project has a `webdef` module that defines all endpoints in
`Endpoints.scala`. These are consumed from the root project to provide the logic
and start the server. Theoretically one could publish this `webdef` module as a
separate dependency and have clients use the definitions instead of wiring
things manually.

The rest of the main application is in the root project. In particular, the following:
- `EndpointsImpl`: Server logic for defined endpoints. Mostly just inputs and outputs.
- `AnalyticsService`: Contracy for the service which will handle actual backend logic.
    - `AnalyticsServiceImpl`: Implementation of the service
- `TrackedEventRepo`: Repo contract
    - `TrackedEventRepoDB`: Implements the repo comunicating with the database
    - `TrackedEventRepoCached`: Cache in front of db access to prevent excessive querying
- `TimeRangeFinder`: Takes a timestamp and provides the corresponding `TimeRange`
    - `TimeRangeFinderHour`: Implementation that finds the surrounding hour block and uses that as the range
- `AggregationFormatter`: Contract for formatting output for the GET endpoint
    - `AggregationFormatterDefault`: Implementation of formatting as required


Additionally, there is a `client` module to assist with inserting data points.
