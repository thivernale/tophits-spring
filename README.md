## Top Hits App in Spring

The Top Hits App is a Spring Boot application designed to manage and analyze music tracks. It provides RESTful APIs for
various functionalities, including track similarity searches, data loading, and rate-limited operations. The app
integrates with PostgreSQL for data storage and Redis for vector store, caching and rate limiting.

### Features

- **Track Similarity Search**: Find similar tracks based on vectorized data.
- **Data Loading**: Load and process track data efficiently.
- **Rate Limiting**: Protect APIs with Redis-based rate limiting.
- **AI Integration**: Utilize AI tools for data augmentation and analysis.

### Usage

```shell
curl -d'{}' -XPOST -H"content-type: application/json" http://localhost:8080/api/rate-limited
```

```shell
PGPASSWORD=duke PGOPTIONS='--search_path=spring' psql -U duke -h localhost tophits
```

```shell
docker exec -e "PGOPTIONS=--search_path=<your_schema>" -it docker_pg psql -U user db_name
```
