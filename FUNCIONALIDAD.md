# DeathTimer Plugin - Documentación Técnica de Desarrollo

##  Requisitos Principales
### **Funcionales**
- [ ] Sistema de contador decreciente por jugador
    - Inicia en X segundos (configurable)
    - Decrementa en 1 cada segundo
- [ ] Item especial que añade tiempo (Y segundos)
    - Metadata personalizada (NBT) para que su obtención sea exclusiva
    - Dropeo por matar jugadores o bosses (configurable)
- [ ] Sistema de ban automático al llegar a 0
    - Tiempo de ban configurable
    - Bypass para admins (p.e. permiso `deathtimer.bypass`)
- [ ] Comandos administrativos:
    - `/deathtime set <segundos>` - Establece tiempo base
    - `/deathtime reload` - Recarga configuración
    - `/deathtime check <jugador>` - Ver tiempo restante
    - `/deathtime bypass <jugador>` - Permite que un jugador no tenga contador
- [ ] Placeholders para que los usuarios sean capaces de ver sus estadísticas (p.e. %player_remaining_time%)

### **No Funcionales**
- [ ] Alta performance (≥50 jugadores concurrentes)
  - Evitar saturar la bbdd con muchas peticiones de cambios pequeños
- [ ] Persistencia de datos ante reinicios
- [ ] Compatibilidad 1.20.4+ (Paper/Spigot)

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
- [ ] Timer decreciente base
- [ ] Sistema de items con NBT
- [ ] Eventos de muerte de entidades
- [ ] Sistema de ban automático
### 2. Datos
- [ ] Conexión SQLite con HikariCP
- [ ] Caché en memoria (Caffeine)
- [ ] Sync memoria-DB cada 5s
### 3. Optimización
- [ ] Batch updates asíncronos
- [ ] Uso de AtomicLong
- [ ] Pool de conexiones DB
### 4. Comandos 
- [ ] /deathtimer set
- [ ] /deathtimer reload
- [ ] /deathtimer check
### 5. Eventos
- [ ] PlayerJoinEvent (iniciar timer)
- [ ] PlayerQuitEvent (guardar datos)
- [ ] EntityDeathEvent (dropeo item)
- [ ] PlayerInteractEvent (usar item)