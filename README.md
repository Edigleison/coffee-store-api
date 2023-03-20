# coffee-store-api

This is an REST API for a coffee store written in
Kotlin and held by Docker. Some technologies and libs used:

- Spring Boot
- Kotlin
- Gradle
- Docker
- JPA
- H2 Database
- Junit
- MockK
- Swagger

To run the application is necessary to execute the following steps:

1. Build the docker image executing the command below:

`docker build -t coffee-store-api .`

2. Run the docker image executing the following command:

`docker run -p 8080:8080 coffee-store-api`

3. Now the application is running on port 8080. Is possible to access the API documentation using the URL `http://localhost:8080/swagger-ui/index.html`

