# DeathTimer Plugin - Documentación Técnica de Desarrollo

##  Requisitos Principales
### **Funcionales**
- [X] Sistema de contador decreciente por jugador
    - Inicia en X segundos (configurable)
    - Decrementa en 1 cada segundo
- [X] Item especial que añade tiempo (Y segundos)
    - Metadata personalizada (NBT) para que su obtención sea exclusiva
    - Dropeo por matar jugadores o bosses (configurable)
- [X] Sistema de ban automático al llegar a 0
    - Tiempo de ban configurable
    - Bypass para admins (p.e. permiso `deathtimer.bypass`)
- [ ] Comandos administrativos:
    - `/deathtime set <segundos>` - Establece tiempo base
    - `/deathtime reload` - Recarga configuración
    - `/deathtime check <jugador>` - Ver tiempo restante
    - `/deathtime bypass <jugador>` - Permite que un jugador no tenga contador
- [ ] Placeholders para que los usuarios sean capaces de ver sus estadísticas (p.e. %player_remaining_time%)

### **No Funcionales**
- [X] Alta performance (≥50 jugadores concurrentes)
  -  Evitar saturar la bbdd con muchas peticiones de cambios pequeños
- [X] Persistencia de datos ante reinicios
- [X] Compatibilidad 1.20.4+ (Paper/Spigot)

## Implementación

### 1. Dependencias
```gradle
// build.gradle
plugins {
    id 'java'
    id 'com.github.johnrengelman.shadow' version '8.1.1'
}

repositories {
    mavenCentral()
    maven { url 'https://repo.papermc.io/repository/maven-public/' }
}

dependencies {
    compileOnly 'io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT'
    implementation 'com.zaxxer:HikariCP:5.0.1'
    implementation 'com.github.ben-manes.caffeine:caffeine:3.1.8'
    implementation 'cloud.commandframework:cloud-paper:1.8.3'
}
```
### 2. Estructura del config.yml (sujeto a cambios)
```
# Valores predeterminados
defaults:
  initial-time: 7200 # 2 horas en segundos
  ban-duration: 86400 # 24 horas
  item:
    material: DRAGON_BREATH
    name: "&5Aliento del Tiempo"
    lore:
      - "&7Añade {seconds} segundos de vida"
    glow-effect: true
  
rewards:
  entities:
    PLAYER: 300
    ENDER_DRAGON: 1800
    WARDEN: 1200
  
messages:
  time-left: "&eTiempo restante: &6{time}"
  banned: "&c¡Baneado! Desbaneo en: &4{time}"
```
### 3. Checklist de Desarrollo

#### 1. Core Features
- [X] Timer decreciente base
- [X] Sistema de items con NBT
- [X] Eventos de muerte de entidades
- [X] Sistema de ban automático
### 2. Datos
- [X] Conexión SQLite con HikariCP
- [X] Caché en memoria (Caffeine)
- [X] Sync memoria-DB cada 5s
### 3. Optimización
- [X] Batch updates asíncronos
- [X] Uso de AtomicLong
- [X] Pool de conexiones DB
### 4. Comandos 
- [X] /deathtime set
- [X] /deathtime reload
- [X] /deathtime check
- [X] /deathtime give
### 5. Eventos
- [X] PlayerJoinEvent (iniciar timer)
- [X] PlayerQuitEvent (guardar datos)
- [X] EntityDeathEvent (dropeo item)
- [X] PlayerInteractEvent (usar item)