
Prueba tecnica para desarrollador Java Spring Boot 

## Descripcion

arquitectura de microservicios que gestiona clientes, cuentas y movimientos bancarios. Los microservicios se comunican de forma asincrónica usando RabbitMQ.

## Tecnologias

- Java 17
- Spring Boot 3.2
- PostgreSQL 15
- RabbitMQ
- Docker & Docker Compose
- MapStruct
- Lombok

## Arquitectura

El sistema está dividido en 2 microservicios:

- **ms-cliente-persona** (Puerto 8081): Maneja clientes y hereda personas
- **ms-cuenta-movimiento** (Puerto 8082): Maneja cuentas, movimientos y reportes

Cada microservicio tiene su propia base de datos PostgreSQL y se comunican mediante eventos de RabbitMQ.

## Requisitos

- Java 17
- Maven 3.8+
- Docker Desktop

## Instalación

1. Clonar el repositorio:

git clone <URL>
cd Prueba-Tecnica
```

2. Levantar todo con Docker:

docker-compose up --build
```

3. Verificar que todo esté corriendo:

docker-compose ps
```

Deberías ver 5 contenedores corriendo:
- banco-db-clientes
- banco-db-cuentas
- banco-rabbitmq
- ms-cliente-persona
- ms-cuenta-movimiento

## Endpoints

### Clientes (Puerto 8081)

```
POST   /api/clientes              - Crear cliente
GET    /api/clientes              - Listar con paginación
GET    /api/clientes/{id}         - Obtener por ID
PUT    /api/clientes/{id}         - Actualizar
DELETE /api/clientes/{id}         - Eliminar
```

### Cuentas (Puerto 8082)

```
POST   /api/cuentas               - Crear cuenta
GET    /api/cuentas               - Listar con paginación
GET    /api/cuentas/{id}          - Obtener por número
PUT    /api/cuentas/{id}          - Actualizar
DELETE /api/cuentas/{id}          - Eliminar
```

### Movimientos (Puerto 8082)

```
POST   /api/movimientos           - Registrar movimiento
GET    /api/movimientos           - Listar con paginación
GET    /api/movimientos/{id}      - Obtener por ID
```

### Reportes (Puerto 8082)

```
GET /api/reportes?clienteId={id}&fechaInicio={fecha}&fechaFin={fecha}
```

## Ejemplos de Uso

### Crear un cliente

```bash
curl -X POST http://localhost:8081/api/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Jose Lema",
    "genero": "M",
    "edad": 35,
    "identificacion": "1234567890",
    "direccion": "Otavalo sn y principal",
    "telefono": "098254785",
    "contrasena": "1234",
    "estado": true
  }'
```

### Crear una cuenta

```bash
curl -X POST http://localhost:8082/api/cuentas \
  -H "Content-Type: application/json" \
  -d '{
    "tipoCuenta": "AHORRO",
    "saldoInicial": 2000.00,
    "estado": true,
    "clienteId": "CLI-XXXXXXXX"
  }'
```

### Registrar un movimiento

```bash
curl -X POST http://localhost:8082/api/movimientos \
  -H "Content-Type: application/json" \
  -d '{
    "numeroCuenta": "CTA-XXXXXXXX",
    "tipoMovimiento": "RETIRO",
    "valor": 575.00
  }'
```

## Postman

Hay una coleccion de Postman en `postman/Banco-API.postman_collection.json` con todos los endpoints ya configurados

## Base de Datos

Los scripts SQL están en la carpeta `scripts/`:
- `BaseDatos.sql` - Script completo

Las bases de datos se crean automáticamente cuando levantas Docker.

## Características Técnicas

- IDs generados con UUID 
- Paginación en todos los listados
- Validación de saldo en movimientos
- Comunicación asincrónica con RabbitMQ
- Contraseñas encriptadas con BCrypt
- Indices en BD para optimizar reportes
- Health checks en todos los servicios
- Circuit Breaker con Resilience4j para tolerancia a fallos

## Patrones de Diseño Implementados


### Patrones Creacionales
- **Builder Pattern**: DTOs y entidades con Lombok ` para construcción flexible de objetos
- **Factory Pattern**: para crear estrategias de deposito/retiro
- **Singleton Pattern**: Todos los beans de Spring (`@Service`, `@Component`, `@Repository`)

### Patrones Estructurales
- **DTO Pattern**: transferencia de datos sin exponer entidades de BD
- **Mapper Pattern**: mapStruct para conversión automática entre entidades y DTOs

### Patrones de Comportamiento

- **Repository Pattern**: Abstracción de acceso a datos con Spring Data JPA
- **Publisher-Subscriber Pattern**: Comunicación entre microservicios via RabbitMQ

### Patrones Arquitectonicos
- **Service Layer Pattern**: Logica de negocio separada en servicios
- **MVC Pattern**: Controllers para manejo de peticiones HTTP
- **Circuit Breaker Pattern**: Resilience4j para llamadas entre microservicios


# Reiniciar un servicio
docker-compose restart ms-cliente-persona

# Detener todo
docker-compose down

# Rebuild
docker-compose up -d --build

# Conectar a BD
docker exec -it banco-db-clientes psql -U banco_user -d db_clientes
```

## Pruebas

# Pruebas unitarias
cd ms-cliente-persona
./mvnw test

cd ms-cuenta-movimiento
./mvnw test
```



## Recomendaciones Futuras

### 1. Implementar Redis para Caché

**¿Por qué Redis?**
- Reducir latencia en consultas frecuentes (clientes, cuentas)
- Disminuir carga en PostgreSQL
- Cache distribuido compartido entre instancias del microservicio


### 2. API Gateway

Implementar Spring Cloud Gateway para:
- Punto de entrada único
- Balanceo de carga
- Autenticación centralizada con JWT

### 3. Service Discovery

Usar Eureka o Consul para:
- Registro automático de microservicios
- Descubrimiento dinámico de servicios
- Eliminar URLs hardcodeadas

### 4. Logging Centralizado

Implementar ELK Stack (Elasticsearch, Logstash, Kibana):
- Logs centralizados de todos los microservicios
- Búsqueda y análisis de logs
- Dashboards de monitoreo

### 5. Monitoreo y Métricas

- **Prometheus + Grafana**: Métricas de rendimiento
- **Distributed Tracing**: Sleuth + Zipkin para rastrear requests entre microservicios

### 6. Seguridad Mejorada

- **OAuth2 + JWT**: Reemplazar Spring Security básico
- **API Keys**: Para comunicación entre microservicios
- **Rate Limiting**: Prevenir abuso de APIs
- **Secrets Management**: Usar Vault para credenciales

### 7. Testing

- Aumentar cobertura de tests (actualmente basica)
- Tests de integración con Testcontainers
- Tests de contrato con Pact
- Tests de carga con JMeter/Gatling

### 8. CI/CD

Implementar pipeline con:
- GitHub Actions / Jenkins
- Build automático
- Tests automáticos
- Deploy a Kubernetes



## Notas

- Los microservicios se reinician automaticamente si fallan
- Para desarrollo local sin Docker, ajusta las URLs en application.yml
- Este proyecto implementa las mejores prácticas de microservicios con Spring Boot


## Este proyecto se baso en este perfil Semi Senior
- SemiSenior: Separar en 2 microservicios, agrupando (Cliente, Persona) y (Cuenta, 
Movimientos) donde se contemple una comunicación asincrónica entre los 2 microservicios. 
Cumplir las funcionalidades: F1, F2, F3, F4, F5 deseable la funcionalidad F6. 