---
name: docker-compose-creator
description: Design multi-container applications with Docker Compose. Covers service orchestration, networking, volumes, environment configuration, health checks, and production deployment patterns. Use when defining local development stacks or deploying multi-service applications.
allowed-tools:
  - Read
  - Write
  - Edit
  - Bash
---

# Docker Compose Creator

Design and manage multi-container applications with Docker Compose for both development and production deployments.

## When to Use

- Setting up local development environments
- Defining multi-service application stacks
- Managing service dependencies
- Configuring networking between services
- Environment variable management
- Production deployments (single host)
- Testing multi-service interactions

## Docker Compose Versions

| Version | Released | Features | Use Case |
|---------|----------|----------|----------|
| v3.x | 2016 | Service definitions, networks | Legacy (avoid for new projects) |
| v2.x | 2014 | Legacy format | Not recommended |
| v3.14 | 2023 | Full features, modern syntax | Current default |

Use version `3.14` or later for new projects.

## Basic Structure

```yaml
version: '3.14'

services:
  # Service definitions go here

volumes:
  # Named volumes

networks:
  # Custom networks
```

## Full-Featured Example: Web Application Stack

```yaml
version: '3.14'

services:
  # Frontend (Nginx)
  nginx:
    image: nginx:1.25-alpine
    container_name: webapp-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./ssl:/etc/nginx/ssl:ro
    depends_on:
      - api
    networks:
      - frontend
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost/health"]
      interval: 30s
      timeout: 5s
      retries: 3
      start_period: 10s

  # API Server (Node.js)
  api:
    build:
      context: ./api
      dockerfile: Dockerfile
      target: production
    container_name: webapp-api
    environment:
      - NODE_ENV=production
      - DATABASE_HOST=postgres
      - REDIS_HOST=redis
      - LOG_LEVEL=info
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_started
    networks:
      - frontend
      - backend
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:3000/health"]
      interval: 30s
      timeout: 5s
      retries: 3
      start_period: 10s
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  # PostgreSQL Database
  postgres:
    image: postgres:15-alpine
    container_name: webapp-db
    environment:
      - POSTGRES_DB=myapp
      - POSTGRES_USER=appuser
      - POSTGRES_PASSWORD=${DB_PASSWORD:?Database password required}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d:ro
    networks:
      - backend
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U appuser -d myapp"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 10s
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  # Redis Cache
  redis:
    image: redis:7-alpine
    container_name: webapp-cache
    command: redis-server --requirepass ${REDIS_PASSWORD:?Redis password required}
    volumes:
      - redis_data:/data
    networks:
      - backend
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Background Worker (Optional)
  worker:
    build:
      context: ./worker
      dockerfile: Dockerfile
    container_name: webapp-worker
    environment:
      - NODE_ENV=production
      - DATABASE_HOST=postgres
      - REDIS_HOST=redis
    depends_on:
      - postgres
      - redis
    networks:
      - backend
    restart: unless-stopped

volumes:
  postgres_data:
  redis_data:

networks:
  frontend:
    driver: bridge
  backend:
    driver: bridge
```

## Service Configuration

### Image vs Build

```yaml
# Using pre-built image
services:
  app:
    image: myregistry/myapp:1.0.0

# Building from Dockerfile
services:
  app:
    build: ./app

# Advanced build options
services:
  app:
    build:
      context: ./app
      dockerfile: Dockerfile.prod
      args:
        - BUILD_ENV=production
      target: production
```

### Environment Variables

```yaml
# Method 1: Inline
services:
  api:
    environment:
      - NODE_ENV=production
      - LOG_LEVEL=info

# Method 2: From file
services:
  api:
    env_file:
      - .env
      - .env.${ENVIRONMENT}

# Method 3: Mixed (with required variable check)
services:
  api:
    environment:
      - DATABASE_PASSWORD=${DB_PASSWORD:?Database password required}
      - DATABASE_HOST=postgres
```

### Resource Limits

```yaml
services:
  api:
    # Memory: 512MB max, 256MB soft limit
    mem_limit: 512m
    memswap_limit: 1g

    # CPU: 1 core
    cpus: '1.0'

    # File descriptor limit
    ulimits:
      nofile:
        soft: 65536
        hard: 65536
```

### Restart Policy

```yaml
services:
  api:
    restart: unless-stopped  # Restart unless manually stopped
    # Options: no, always, unless-stopped, on-failure

  worker:
    restart: on-failure
    restart_policy:
      condition: on-failure
      delay: 5s
      max_attempts: 3
      window: 60s
```

## Networking

### Service Discovery

```yaml
services:
  api:
    networks:
      - backend
    environment:
      # Access postgres by service name on backend network
      - DATABASE_HOST=postgres

  postgres:
    networks:
      - backend
```

### Multi-Network Setup

```yaml
services:
  api:
    networks:
      - frontend  # Can talk to nginx
      - backend   # Can talk to postgres

  nginx:
    networks:
      - frontend  # Can talk to api

  postgres:
    networks:
      - backend   # Can talk to api, worker

networks:
  frontend:
  backend:
```

### Port Mapping

```yaml
services:
  api:
    ports:
      - "3000:3000"           # Map container 3000 to host 3000
      - "3001:3000"           # Map container 3000 to host 3001
      - "127.0.0.1:3000:3000" # Bind to localhost only
```

## Volumes

### Named Volumes

```yaml
services:
  postgres:
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

### Bind Mounts

```yaml
services:
  api:
    volumes:
      # Host path : Container path
      - ./src:/app/src
      # Read-only
      - ./config:/app/config:ro
      # With options
      - ./logs:/app/logs:delegated
```

### Tmpfs (Memory-backed)

```yaml
services:
  api:
    tmpfs:
      - /tmp
      - /run
```

## Depends On

### Basic Dependency

```yaml
services:
  api:
    depends_on:
      - postgres
      - redis

  postgres:
    image: postgres:15

  redis:
    image: redis:7
```

### Conditional Dependency (Health Checks)

```yaml
services:
  api:
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_started

  postgres:
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U user"]
      interval: 10s
      timeout: 5s
      retries: 5
```

## Health Checks

```yaml
services:
  api:
    healthcheck:
      # Check command
      test: ["CMD", "curl", "-f", "http://localhost:3000/health"]

      # Check interval
      interval: 30s

      # Timeout per check
      timeout: 5s

      # Retries before failure
      retries: 3

      # Wait before first check
      start_period: 10s
```

## Logging

```yaml
services:
  api:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
        labels: "service=api"

  # Syslog
  worker:
    logging:
      driver: "syslog"
      options:
        syslog-address: "udp://127.0.0.1:514"
        tag: "{{.Name}}"
```

## Development vs Production

### Development (docker-compose.yml)

```yaml
version: '3.14'

services:
  api:
    build: ./api
    ports:
      - "3000:3000"
    volumes:
      - ./api/src:/app/src  # Hot reload
    environment:
      - NODE_ENV=development
    command: npm run dev
```

### Production (docker-compose.prod.yml)

```yaml
version: '3.14'

services:
  api:
    image: myregistry/myapp:1.0.0
    restart: unless-stopped
    environment:
      - NODE_ENV=production
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:3000/health"]
      interval: 30s
      timeout: 5s
      retries: 3
```

### Running with Override

```bash
# Development
docker compose up

# Production
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## Common Commands

```bash
# Start services
docker compose up

# Start in background
docker compose up -d

# Stop services
docker compose down

# Stop without removing volumes
docker compose stop

# Remove containers, networks, volumes
docker compose down -v

# View logs
docker compose logs -f

# Specific service logs
docker compose logs -f api

# Execute command in service
docker compose exec api npm test

# Run one-off command
docker compose run api npm test

# View service status
docker compose ps

# Rebuild images
docker compose up --build

# Remove and rebuild
docker compose down --rmi all && docker compose up --build
```

## Advanced Patterns

### Service Override

```yaml
# docker-compose.override.yml (git-ignored)
services:
  api:
    volumes:
      - ./src:/app/src  # Local development
    environment:
      - DEBUG=true
```

### Multiple Configurations

```bash
# Production with monitoring
docker compose \
  -f docker-compose.yml \
  -f docker-compose.prod.yml \
  -f docker-compose.monitoring.yml \
  up -d
```

### Environment-Specific Files

```bash
# Load .env.${ENVIRONMENT}
docker compose --env-file .env.production up -d
```

## Production Deployment Checklist

- [ ] All services have healthchecks
- [ ] Services use `restart: unless-stopped`
- [ ] Sensitive data in `.env` (git-ignored)
- [ ] Volumes for persistent data
- [ ] Resource limits set
- [ ] Logging configured
- [ ] Networks isolated
- [ ] Use specific image tags (never `latest`)
- [ ] Test with `docker compose up -d && docker compose ps`

## Troubleshooting

```bash
# Check service status
docker compose ps

# View detailed service info
docker compose exec postgres psql -U user -c "\l"

# Network diagnosis
docker compose exec api ping postgres

# Resource usage
docker stats

# Rebuild and restart
docker compose down --rmi local
docker compose up --build
```

## References

- Docker Compose documentation: https://docs.docker.com/compose/
- Compose file format: https://docs.docker.com/compose/compose-file/
- Best practices: https://docs.docker.com/compose/production/
