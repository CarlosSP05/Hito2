# EcoCity - Proyecto Integrador 2º Trimestre

## Descripción del Proyecto

**EcoCity** es una aplicación móvil Android desarrollada como parte de la iniciativa "Ciudad Inteligente" del Ayuntamiento. Esta aplicación permite a los ciudadanos reportar incidencias urbanas en tiempo real, como baches, farolas rotas o basura acumulada. A través de la aplicación, los usuarios pueden crear alertas con evidencia multimedia (fotos y notas de voz), incluyendo la ubicación exacta mediante coordenadas GPS, lo que mejora la respuesta del ayuntamiento y contribuye a una ciudad más eficiente y limpia.

El proyecto combina una solución híbrida que integra tanto la modernidad de la nube como la robustez de los sistemas de bajo nivel para asegurar la correcta comunicación y funcionamiento de la aplicación.

## Funcionalidades Principales

La aplicación incluye las siguientes funcionalidades clave:

- **Gestión de Identidad**: Registro e inicio de sesión seguro mediante correo electrónico o Google.
  
- **Reportar Incidencias**: Los usuarios pueden reportar incidencias proporcionando:
  - Título, descripción y nivel de urgencia.
  - Evidencia multimedia (foto de cámara y nota de voz).
  - Ubicación precisa mediante coordenadas GPS.

- **Sincronización Inteligente**: La aplicación funciona de manera offline, almacenando los datos localmente y sincronizándolos con la nube cuando se restablezca la conexión a internet.

- **Soporte Técnico (Chat)**: Canal de comunicación directo vía TCP/IP con el centro de soporte del ayuntamiento para atender incidencias y consultas de los ciudadanos.

## Módulos del Proyecto

Este proyecto está basado en los siguientes módulos:

- **Programación Multimedia (PMDM)**: Integración de funcionalidades multimedia como la captura de fotos y notas de voz, así como la geolocalización mediante Google Maps SDK.
  
- **Servicios y Procesos (PSP)**: Gestión de hilos en segundo plano para la sincronización y procesamiento de datos, como la carga de archivos multimedia y la actualización de la base de datos en la nube.
  
- **Desarrollo de Interfaces (DI)**: Creación de interfaces de usuario amigables y fáciles de usar, incluyendo la pantalla de login y formularios para el reporte de incidencias.

## Hoja de Ruta de Desarrollo

### 📅 Hito 1: Experiencia de Usuario y Persistencia Local
Objetivo: Crear la estructura visual y asegurar que la app funcione sin conexión a internet.

#### Tareas:
- **Interfaz de Usuario**:
  - Pantalla de Login (diseño y validación visual).
  - Listado de incidencias utilizando RecyclerView y tarjetas personalizadas.
  - Formulario de alta con validaciones de entrada.

- **Persistencia Local**:
  - Implementación de una base de datos local con SQLite.
  - CRUD completo: Crear, Leer, Editar y Borrar incidencias en el dispositivo.

### 📅 Hito 2: Multimedia y Sensores
Objetivo: Integrar el hardware del dispositivo.

#### Tareas:
- **Cámara**:
  - Integrar la cámara del dispositivo para capturar fotos y asociarlas a las incidencias.
  - Gestión de permisos en tiempo de ejecución.

- **Geolocalización**:
  - Integración de Google Maps SDK para la selección de ubicación y el guardado de coordenadas GPS.

- **Procesos en Segundo Plano**:
  - Uso de hilos en segundo plano para almacenar los archivos multimedia sin bloquear la interfaz de usuario.


