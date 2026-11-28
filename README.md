# ColTech - Sistema de reparaciones

## Requisitos

- Git
- Node.js 24 y npm
- JDK 24
- MySQL 8 en `localhost:3306`

## Preparar el backend

Inicia MySQL. La configuración actual usa la base de datos `reparaciones` y puede crearla automáticamente. Si las credenciales locales de MySQL son distintas, ajústalas en `Backend_Reparaciones/paginaweb/src/main/resources/application.properties`.

Inicia el backend:

```powershell
cd Backend_Reparaciones\paginaweb
.\mvnw.cmd spring-boot:run
```

Para ejecutar las pruebas:

```powershell
.\mvnw.cmd test
```

Las pruebas de integración también requieren que MySQL esté disponible con las mismas credenciales del archivo de configuración.

## Preparar el frontend

En otra terminal:

```powershell
cd Frontend_Reparaciones
npm ci
npm start
```

Abre `http://localhost:4200`. El frontend se conecta al backend en `http://localhost:8080`.

## Validaciones

Desde `Frontend_Reparaciones`:

```powershell
npm test -- --watch=false
npm run build
npm run lint
```

Desde `Backend_Reparaciones\paginaweb`:

```powershell
.\mvnw.cmd test
```
