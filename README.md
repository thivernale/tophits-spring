## Top Hits App in Spring

# Todo

- [x] Add filter
- [x] Add interceptor
- [x] Copy database init scripts to resources
- [x] Set up db - use `spring` schema
- [x] Set up logging to file
- [x] Add rate limiting based on IP
- [x] Redo RateLimiter class alone
- [x] Events logging - from spring-mvc-practice + josh
- [x] Rate-limiting with Redis-data
- ......................
- [x] Track listing
- [x] List template
- [ ] Js or htmx ?
- [x] Modal for import
- [x] File list/import module
- [x] Add AI
- [x] Bass line in html and js
- [x] Method in controller to get bass line from AI service
- [x] AI service to get bass line from OpenAI
- [x] Repo and model for storing bass lines
- [ ] Unit tests

```shell
curl -d'{}' -XPOST -H"content-type: application/json" http://localhost:8080/api/rate-limited
```

```shell
PGPASSWORD=duke PGOPTIONS='--search_path=spring' psql -U duke -h localhost tophits
```

```shell
docker exec -e "PGOPTIONS=--search_path=<your_schema>" -it docker_pg psql -U user db_name
```
