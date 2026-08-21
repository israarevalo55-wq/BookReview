# BookReview 📚

Catálogo de libros con reseñas propias, desarrollado en Kotlin con Jetpack
Compose. Proyecto final individual de la asignatura de Aplicaciones
Móviles — última semana de la carrera.

Busca libros en una API pública, guarda tu propia calificación, reseña y
una foto tomada con la cámara, todo persistido localmente en el
dispositivo.

---

## Índice

- [Descripción general](#descripción-general)
- [Capturas de pantalla](#capturas-de-pantalla)
- [Arquitectura](#arquitectura)
- [Stack técnico](#stack-técnico)
- [API utilizada](#api-utilizada)
- [Persistencia local](#persistencia-local)
- [Hardware y permisos](#hardware-y-permisos)
- [Decisiones técnicas y por qué](#decisiones-técnicas-y-por-qué)
- [Bugs reales encontrados y resueltos](#bugs-reales-encontrados-y-resueltos)
- [Despliegue](#despliegue)
- [Mejoras futuras conocidas](#mejoras-futuras-conocidas)
- [Cómo correr el proyecto](#cómo-correr-el-proyecto)

---

## Descripción general

**BookReview** permite:

1. **Buscar libros** por título o autor contra la API pública de Open Library.
2. Ver el **detalle** de un libro y escribir tu propia reseña: calificación
   (0-5), texto libre, y una foto tomada con la cámara del dispositivo.
3. Marcar libros como **favoritos**, cruzando resultados remotos con datos
   guardados localmente.
4. Consultar **Mis Reseñas**: todo lo que has calificado, guardado en Room,
   con acceso rápido para editar o eliminar.
5. Cambiar el **modo oscuro** desde Ajustes, persistido con DataStore.

La app maneja los tres estados típicos de una operación de red (cargando,
éxito, error) y no depende de tener conexión para ver reseñas ya guardadas.

## Capturas de pantalla

| Búsqueda | Detalle + reseña |
|---|---|
| ![Búsqueda](assets/01_busqueda.png) | ![Detalle](assets/02_detalle_resena.png) |

| Mis Reseñas | Ajustes |
|---|---|
| ![Mis Reseñas](assets/03_mis_resenas.png) | ![Ajustes](assets/04_ajustes.png) |

**Estado de error de red** (probado apagando WiFi y datos móviles reales, no simulado):

![Estado de error](assets/05_estado_error.png)

## Arquitectura

![Diagrama de arquitectura](assets/diagrama_arquitectura.svg)

Separación en capas, donde las dependencias solo apuntan **hacia adentro**:

```
ui  --depende de-->  domain  <--implementa--  data
```

- **`domain`** no depende de nadie: modelos puros y contratos (interfaces
  de repositorio + casos de uso).
- **`data`** implementa las interfaces de `domain` contra Room, DataStore
  y Retrofit.
- **`ui`** solo conoce `domain`: Composables + ViewModels con `StateFlow`.
  Nunca instancia un DAO ni un servicio Retrofit directamente.

```
app/src/main/java/com/example/bookreview/
├── BookReviewApplication.kt        Application: crea el AppContainer
├── MainActivity.kt                 Activity única, hospeda BookReviewApp()
├── di/
│   └── AppContainer.kt             Contenedor manual de dependencias (sin Hilt)
├── domain/
│   ├── model/                      Libro, Resena, LibroConResena
│   ├── repository/                 Interfaces: LibroRepository, ResenaRepository, SettingsRepository
│   └── usecase/                    ObtenerLibroConResenaUseCase, BuscarLibrosConFavoritosUseCase, ErroresRed
├── data/
│   ├── local/                      Room (ResenaEntity, ResenaDao, AppDatabase) + DataStore (PreferenciasDataStore)
│   ├── remote/                     Retrofit (OpenLibraryApi, DTOs)
│   └── repository/                 Implementaciones que traducen entre dominio y cada fuente
└── ui/
    ├── search/    (Búsqueda)
    ├── detail/    (Detalle + cámara)
    ├── reviews/   (Mis Reseñas)
    ├── settings/  (Ajustes)
    ├── common/    (Estados de UI compartidos: carga/error)
    ├── navigation/
    └── theme/
```

### Regla clave: un Repository = una sola fuente

`LibroRepositoryImpl` conoce `Libro` (dominio) y Retrofit — nunca Room.
`ResenaRepositoryImpl` conoce `Resena` (dominio) y Room — nunca Retrofit.
Ninguno sabe que el otro existe.

Combinar datos remotos y locales (por ejemplo, "¿este libro de la API ya
tiene una reseña guardada?") es responsabilidad de un **caso de uso** en
`domain/usecase/`, no de un Repository ni del ViewModel:

```
DetalleViewModel.cargar()
  → ObtenerLibroConResenaUseCase(libroId)
      → libroRepository.getLibroPorId(libroId)        [remoto]
      → resenaRepository.getResenaPorLibroId(libroId) [local]
      → arma LibroConResena(libro, resena)
```

```
BusquedaViewModel.buscar()
  → BuscarLibrosConFavoritosUseCase(query)
      → libroRepository.buscarLibros(query)  [remoto, N resultados]
      → resenaRepository.getFavoritos()      [local, solo favoritos]
      → cruza por libroId → Libro.esFavorito calculado
```

Esta separación permitió agregar la combinación de fuentes (Semana 3) y el
manejo de errores/carga (Semana 5) **sin modificar** `LibroRepository`,
`ResenaRepository` ni sus implementaciones.

## Stack técnico

| Componente | Versión / Herramienta |
|---|---|
| Lenguaje | Kotlin 2.2.10 |
| Android Gradle Plugin | 9.1.1 |
| UI | Jetpack Compose (BOM 2026.02.01) |
| Navegación | Navigation Compose |
| Persistencia estructurada | Room |
| Preferencias | DataStore |
| Red | Retrofit + OkHttp + Gson |
| Imágenes | Coil |
| Concurrencia | Corrutinas + StateFlow |
| Inyección de dependencias | Manual (`AppContainer` + `AppViewModelProvider`), sin Hilt |
| Gestión de dependencias | Version Catalog (`libs.versions.toml`) |

Se priorizaron versiones estables y maduras del ecosistema (Room 2.8.4,
Navigation Compose 2.9.8, Retrofit 2.11.0) en vez de las más recientes
(`androidx.room3`, Navigation3, Retrofit 3.0.0), para reducir el riesgo de
incompatibilidades durante el desarrollo.

## API utilizada

**Open Library Search API** — pública, gratuita, sin necesidad de API key:

```
GET https://openlibrary.org/search.json?q={query}
```

Las portadas se cargan desde:

```
https://covers.openlibrary.org/b/id/{cover_i}-M.jpg
```

**Limitación conocida:** Open Library no ofrece un endpoint "obtener libro
por id" en el mismo formato que la búsqueda. `getLibroPorId()` resuelve
esto con una caché en memoria de la última búsqueda realizada en la
sesión. Si el libro remoto no está disponible (por ejemplo, tras recrearse
el proceso), `ObtenerLibroConResenaUseCase` reconstruye un `Libro` mínimo
a partir de los datos ya guardados en la `Resena` local (título y autor se
duplican intencionalmente en Room para este caso).

## Persistencia local

- **Room** — tabla `resenas`, una fila por libro reseñado
  (`libroId, titulo, autor, rating, texto, fotoUri, esFavorito`). Expuesta
  como `Flow`, así la UI se actualiza sola cuando cambia la tabla.
- **DataStore** — una sola preferencia por ahora: `modo_oscuro` (Boolean),
  aplicada directamente al tema de Compose.

## Hardware y permisos

**Cámara**, para adjuntar una foto a cada reseña:

- Permiso `CAMERA` solicitado en tiempo de ejecución
  (`ActivityResultContracts.RequestPermission()`).
- Si el usuario lo **rechaza**: se muestra un Snackbar explicativo y el
  resto del formulario de reseña sigue siendo utilizable — sin crash, sin
  bloquear la app.
- Si lo **acepta**: se abre la cámara (`ActivityResultContracts.TakePicture()`)
  con una `Uri` generada por un `FileProvider` (nunca se expone una ruta
  `file://` directa entre procesos).
- La foto se guarda en almacenamiento privado de la app
  (`context.filesDir/fotos/`, no `cacheDir`, para que sobreviva tanto como
  la reseña que la referencia en Room).
- Toda la lógica de permisos/cámara vive en `DetalleScreen` (capa `ui`); el
  `DetalleViewModel` solo recibe la `Uri` final ya resuelta.

## Decisiones técnicas y por qué

- **DI manual, sin Hilt**: un `AppContainer` colgado de
  `BookReviewApplication` + una fábrica única `AppViewModelProvider`
  (patrón del codelab *Inventory* de Android Basics with Compose). Se
  evitó un framework adicional para mantener el proyecto simple y
  explicable.
- **Casos de uso en vez de que un Repository dependa de otro**: se pudo
  hacer que `LibroRepositoryImpl` recibiera también el DAO de reseñas y
  cruzara ahí mismo, pero eso mezclaría responsabilidades (conocería Room
  *y* Retrofit) y sería más difícil de probar por separado.
- **`Libro.esFavorito` es un campo derivado**: nace siempre en `false`
  (ni la API ni `LibroRepositoryImpl` lo llenan); solo
  `BuscarLibrosConFavoritosUseCase` lo calcula cruzando con Room. Distingue
  entre campos que vienen de una sola fuente (`titulo`, de la API) y
  campos calculados combinando dos fuentes.
- **Cliente Retrofit armado en `AppContainer`**, no en el ViewModel ni la
  pantalla. El logging de red (`HttpLoggingInterceptor`, `Level.BODY`)
  solo se activa si la app es debuggable, para no loguear tráfico completo
  en un build de release.
- **Estados de UI como `sealed class`** (`Cargando / Éxito / Error`) en vez
  de banderas booleanas sueltas (`isLoading`, `errorMessage`), para que el
  compilador obligue a manejar todos los casos en la pantalla.

## Bugs reales encontrados y resueltos

Estos se descubrieron **ejecutando la app**, no durante la compilación —
son la evidencia de que se probó de verdad, no solo que "compiló en verde".

### 1. Rutas de Navigation con "/" (Semana 2)

Las keys de Open Library incluyen una barra (`/works/OL27448W`). La ruta
`"detalle/$libroId"` quedaba mal formada (`detalle//works/...`) y
Navigation Compose no la reconocía → crash al tocar un resultado de
búsqueda real (los ids mock de la Semana 1 nunca expusieron este caso, por
no tener barras).

**Solución:** `Uri.encode()` al construir la ruta, `Uri.decode()` al
leerla.

### 2. Pérdida de estado al volver de la cámara (Semana 4)

Elegir **"Only this time"** en el diálogo de permiso de cámara hace que
Android **mate el proceso** de la app al pasar a segundo plano. Esto
expuso dos problemas:

- La `Uri` de la foto pendiente vivía en `remember` (no sobrevive la
  muerte del proceso) → se perdía el destino de la foto tomada.
  **Fix:** `rememberSaveable`.
- La caché en memoria de `LibroRepositoryImpl` quedaba vacía al recrear el
  proceso, y el caso de uso devolvía `null` completo, perdiendo el acceso
  a una reseña que sí era persistente en Room.
  **Fix:** el caso de uso ahora reconstruye un `Libro` mínimo desde los
  datos ya guardados en `Resena` si el remoto no está disponible.

> **Lección:** lanzar cualquier Activity externa (cámara, selector de
> archivos, etc.) es un punto donde Android puede matar el proceso sin
> avisar. El estado que depende de sobrevivir eso debe vivir en
> `rememberSaveable`/`SavedStateHandle`, nunca en `remember` a secas ni en
> una caché puramente en memoria.

### 3. Conflicto de build: AGP 9.1.1 + KSP (Semana 1)

AGP 9.1.1 trae "Kotlin integrado" (built-in Kotlin) activado por defecto,
incompatible con KSP 2.2.10-2.0.2 (usado por Room para generar el DAO).

**Solución:** `android.disallowKotlinSourceSets=false` en
`gradle.properties` (recomendación del propio issue de KSP,
[google/ksp#2729](https://github.com/google/ksp/issues/2729)). También se
subió `compileSdk` a 37, exigido por `androidx.core:core-ktx:1.19.0`.

## Despliegue

- Keystore generado (`keytool`, RSA 2048, alias `bookreview`, válido hasta
  2054), credenciales fuera del repositorio (`keystore.properties` y la
  carpeta `keystore/` están en `.gitignore`).
- `app/build.gradle.kts` lee esas credenciales y firma automáticamente el
  build de `release`.
- Generados: `app-release.aab` (para Play Store) y `app-release.apk`
  (instalación directa para pruebas).

## Mejoras futuras conocidas

- **Precisión de `Float` en el rating**: en algunas reseñas el promedio se
  muestra como `3.0000005 / 5` en vez de `3.0 / 5`. No afecta la
  funcionalidad, pero se debería redondear explícitamente al formatear en
  la UI (`"%.1f".format(rating)`).
- **Caché de `getLibroPorId()` en memoria**: al reiniciar completamente la
  app (no solo recrear el proceso), se pierde hasta hacer una nueva
  búsqueda. Una mejora futura sería persistir también los metadatos del
  libro (no solo la reseña) en Room.

## Cómo correr el proyecto

1. Clonar el repositorio y abrirlo en Android Studio.
2. Sincronizar Gradle (usa Version Catalog, no requiere configuración
   adicional).
3. Ejecutar en un emulador o dispositivo con API 24+ y conexión a
   internet (para la búsqueda de libros).
4. El modo oscuro y las reseñas guardadas persisten entre sesiones
   automáticamente.

---

*Proyecto individual — Aplicaciones Móviles. Basado en la estructura y
convenciones de arquitectura vistas en clase con CineMatch, aplicadas a un
tema y funcionalidad propios.*
