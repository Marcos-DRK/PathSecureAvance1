PathSecure  
Proyecto en Android Studio (Lenguaje Java, API 26)
Autor: Marcos Salazar
Colaboradores: Abraham y Cristobal Daza
Versión: Fase 1 - Paso 1 a 7  
Instituto: Virginio Gómez  
Asignatura: Taller de Nuevas Tecnologías  

---

## 📘 Descripción del Proyecto
PathSecure es una aplicación móvil enfocada en la seguridad personal y prevención de secuestros o robos.  
Permite registrar rutas habituales, detectar movimientos anómalos mediante sensores, activar protocolos de emergencia manual o automáticamente, y notificar a contactos de confianza con ubicación y evidencia.  

---

## 🎯 Objetivo General
Desarrollar una aplicación móvil completa que combine interfaz intuitiva, geolocalización en tiempo real, sensores del dispositivo y una Base de datos (Firebase) 
para proveer un sistema de alerta y seguridad personal.

---

## Estado del Proyecto
| Fase 1 |             Descripción                           | Estado |
|--------|---------------------------------------------------|--------|
| Paso 1 | Estructura inicial y repositorio Git              |   ✅  |
| Paso 2 | Login y registro con navegación básica            |   ✅  |
| Paso 3 | Pantalla principal (HomeActivity)                 |   ✅  |
| Paso 4 | Activities de Rutas y Configuración               |   ✅  |
| Paso 5 | Integración inicial del GPS                       |   ✅  |
| Paso 6 | Conexión con Firebase (Auth + Firestore)          |   ✅  |
| Paso 7 | Botón de emergencia manual y registro en Firebase |   ✅  |

---

##  Estructura del Proyecto

com.example.pathsecure
│
├── activities/
│ ├── MainActivity.java
│ ├── RegisterActivity.java
│ ├── HomeActivity.java
│ ├── RoutesActivity.java
│ ├── SettingsActivity.java
│ └── EmergencyActivity.java
│
└── utils/
└── (próximamente: SensorManager, FirebaseHelper)
|
|
├── res/layouts/
|   
│ ├── activity_main.xml
│ ├── activity_register.xml
│ ├── activity_home.xml
│ ├── activity_routes.xml
│ ├── activity_settings.xml
│ └── activity_emergency.xml
│

---

##  Funcionalidades Implementadas 

### 🔸 Paso 1 – Estructura inicial
- Proyecto creado con plantilla **Empty Views Activity**.  
- Organización modular del código (activities, layouts, utils).  
- Primer commit al repositorio GitHub.

### 🔸 Paso 2 – Login y Registro
- Implementación de **MainActivity** (Login) y **RegisterActivity**.  
- Navegación mediante `Intent`.  
- ConstraintLayout y LinearLayout aplicados correctamente.  

### 🔸 Paso 3 – HomeActivity (Pantalla principal)
- Diseño principal con `FrameLayout` (mapa simulado).  
- Uso de `TableLayout` para estados del sistema.  
- Botones de navegación a **Rutas**, **Configuración** y **Emergencia**.

### 🔸 Paso 4 – RoutesActivity y SettingsActivity
- **RoutesActivity:** muestra rutas guardadas simuladas con `TableLayout`.  
- **SettingsActivity:** configuración de usuario y contactos con `LinearLayout`.  
- Flujo de navegación completo.

### 🔸 Paso 5 – Integración de GPS
- Permisos de ubicación (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`).  
- Obtención de coordenadas con `LocationManager`.  
- Visualización en `TextView` dentro de HomeActivity.  
- Manejo de errores con `Log.e()`.

### 🔸 Paso 6 – Conexión con Firebase
- Integración de **Firebase Authentication** y **Cloud Firestore**.  
- Registro de usuarios en Firebase.  
- Inicio de sesión con verificación.  
- Colección `usuarios` creada en Firestore.

### 🔸 Paso 7 – Botón de Emergencia
- Nueva **EmergencyActivity** con interfaz roja y mensaje de alerta.  
- Registro de emergencias en la colección `emergencias` (Firebase).  
- Campos almacenados: usuario, fecha, tipo ("manual"), estado ("activada").

---

##  Dependencias Principales
```gradle
implementation 'com.google.firebase:firebase-auth:22.3.1'
implementation 'com.google.firebase:firebase-firestore:25.1.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
implementation 'com.google.android.gms:play-services-location:21.0.1'


##  Permisos declarados en AndroidManifest.xml:

<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

|------------------------------------------------------|
|Tecnologías Utilizadas                                |
|------------------------------------------------------|
|Lenguaje: Java                                        |
|IDE: Android Studio                                   |
|API Mínima: 26 (Android 8.0 Oreo)                     |
|Base de Datos: Firebase Firestore                     |
|Autenticación: Firebase Auth                          |
|Sensores: GPS (implementado), Acelerómetro (pendiente)|
|------------------------------------------------------|


 Commits Principales
|-----------------------------------------------------------------------|
|Commit	  |Descripción                                                  |    
|Commit 1	|Creación del proyecto base y estructura de carpetas          |
|Commit 2	|Login y RegisterActivity con navegación básica               |
|Commit 3	|HomeActivity con diseño preliminar                           |
|Commit 4	|RoutesActivity y SettingsActivity con layouts Table y Linear |
|Commit 5	|Integración inicial del GPS                                  |
|Commit 6	|Conexión inicial a Firebase Auth y Firestore                 |
|Commit 7	|Implementación del botón de emergencia y registro en Firebase|


 Próximos Pasos (Fase 2)

-Integrar sensores (acelerómetro y botones de volumen).
-Activar grabación de audio en segundo plano.
-Enviar alertas automáticas (SMS o correo).
-Mejorar la interfaz (UI/UX final).


 Autor

DRK
Ingeniero en Informática – Instituto Virginio Gómez
Proyecto académico guiado por el profesor Pablo Calderón Cornejo
Octubre 2025

