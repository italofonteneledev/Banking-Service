# Banking Quarkus (monorepo)

Monorepo com os serviços Quarkus do banking:

| Pasta | Descrição | Porta |
|-------|-----------|-------|
| `banking-service` | API de agências (cadastro, métricas) | `8080` |
| `banking-validation` | API de situação cadastral | `8181` |

Remote: [italofonteneledev/Banking-Service](https://github.com/italofonteneledev/Banking-Service)

## Subir com Docker

```bash
# Validation (precisa estar no ar para o service consultar CNPJ)
cd banking-validation
docker compose up

# Service
cd ../banking-service
docker compose up
```

## Dev local

```bash
# Validation (Docker ou quarkus dev)
cd banking-validation && docker compose up

# Service
cd banking-service
# .env aponta CLIENT HTTP para localhost:8181
quarkus dev
```

O `banking-service` consulta o validation em `http://{QUARKUS_CLIENT_HTTP}:{QUARKUS_CLIENT_HTTP_PORT}/situacao-cadastral/{cnpj}`.
