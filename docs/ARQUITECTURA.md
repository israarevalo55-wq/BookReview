# Arquitectura de BookReview

> Documento vivo: se actualiza cada semana del cronograma. Sirve como base
> para la sustentación — cada decisión de acá debe poder explicarse y
> defenderse.

## Idea central

Las capas solo se conocen **hacia adentro**:

```
ui  --depende de-->  domain  <--implementa--  data
```

`domain` no depende de nadie. `data` implementa las interfaces de `domain`.
`ui` solo conoce `domain` (nunca Room ni Retrofit directamente). Esto es lo
que permite cambiar la fuente de datos (mock → Retrofit, o Room → otra BD)
sin tocar ViewModels ni pantallas.

## Estructura de carpetas

```
app/src/main/java/com/example/bookreview/
├── BookReviewApplication.kt        Application: crea el AppContainer
├── MainActivity.kt                 Activity única, hospeda BookReviewApp()
├── di/
│   └── AppContainer.kt             Contenedor manual de dependencias (sin Hilt)
├── domain/
│   ├── model/
│   │   ├── Libro.kt                 Modelo puro para búsqueda/detalle (+esFavorito, derivado)
│   │   ├── Resena.kt                Modelo puro para reseñas
│   │   └── LibroConResena.kt        Libro + Resena? guardada (resultado de combinar)
│   ├── repository/
│   │   ├── LibroRepository.kt       Interfaz: buscar/obtener libros (remoto)
│   │   ├── ResenaRepository.kt      Interfaz: CRUD de reseñas (local)
│   │   └── SettingsRepository.kt    Interfaz: leer/guardar modo oscuro (local)
│   └── usecase/
│       ├── ObtenerLibroConResenaUseCase.kt      Detalle: combina Libro (remoto) + Resena? (local)
│       └── BuscarLibrosConFavoritosUseCase.kt   Búsqueda: marca esFavorito cruzando con Room
├── data/
│   ├── local/
│   │   ├── ResenaEntity.kt          @Entity de Room (tabla "resenas")
│   │   ├── ResenaDao.kt             @Dao: queries SQL
│   │   ├── AppDatabase.kt           RoomDatabase (singleton)
│   │   └── PreferenciasDataStore.kt Context.dataStore + claves
│   ├── remote/
│   │   ├── OpenLibraryApi.kt         Interfaz Retrofit: GET search.json?q=
│   │   └── LibroDto.kt               DTOs del JSON (LibroDto, ...ResponseDto)
│   └── repository/
│       ├── ResenaRepositoryImpl.kt   Implementa ResenaRepository con Room
│       ├── LibroRepositoryImpl.kt    Implementa LibroRepository con Open Library (real)
│       └── SettingsRepositoryImpl.kt Implementa SettingsRepository con DataStore
└── ui/
    ├── AppViewModelProvider.kt      Fábrica única de ViewModels
    ├── BookReviewApp.kt             Composable raíz: aplica el tema
    ├── navigation/
    │   ├── Screen.kt                Rutas (sealed class)
    │   └── BookReviewNavGraph.kt    NavHost + barra inferior
    ├── search/    (Búsqueda)        BusquedaScreen.kt, BusquedaViewModel.kt
    ├── detail/    (Detalle)         DetalleScreen.kt, DetalleViewModel.kt
    ├── reviews/   (Mis Reseñas)     MisResenasScreen.kt, MisResenasViewModel.kt
    ├── settings/  (Ajustes)         AjustesScreen.kt, AjustesViewModel.kt
    └── theme/                       Color.kt, Theme.kt, Type.kt (sin cambios)
```

## Qué hace cada capa

### `domain/`
Modelos y contratos, sin anotaciones de Room ni de Retrofit. Es la capa que
casi no cambia durante el proyecto: si hoy defino "una Resena tiene rating,
texto, esFavorito", eso no depende de si el rating se guarda en SQLite o en
un JSON remoto.

### `data/local/`
Persistencia real en el dispositivo: Room (reseñas) y DataStore (modo
oscuro). Room usa `Flow<List<...>>` para que la UI se actualice sola cuando
cambia la tabla, sin refrescos manuales.

### `data/remote/`
Desde la Semana 2: `OpenLibraryApi.kt` (interfaz Retrofit, `GET search.json?q=`)
+ `LibroDto.kt` (DTOs "tontos" que solo reflejan el JSON de
`https://openlibrary.org/search.json?q={query}`, con `@SerializedName` para
mapear `author_name`, `cover_i`, etc.). Ni domain ni ui saben que estos DTOs
existen.

### `data/repository/`
Cada implementación conoce dos mundos, pero siempre **el mismo par**: su
propia parte de dominio y su propia fuente de datos. `LibroRepositoryImpl`
conoce `Libro` (dominio) y `OpenLibraryApi`/`LibroDto` (remoto) — nunca
Room. `ResenaRepositoryImpl` conoce `Resena` (dominio) y `ResenaDao`
(local) — nunca Retrofit. Ninguno de los dos sabe que el otro existe. Esa
separación es la que obliga a que la combinación entre ambos (Semana 3)
tenga que vivir en otra capa.

### `domain/usecase/` (desde la Semana 3)
Acá es donde de verdad se **combinan** datos remotos y locales — nunca en
un Repository (que representa una sola fuente) ni en el ViewModel
(que no debe conocer dos repositorios para un mismo propósito). Cada caso
de uso recibe `LibroRepository` + `ResenaRepository` por constructor y
expone un solo `operator fun invoke(...)`, así se usan como una función:
`obtenerLibroConResenaUseCase(libroId)`. Ver la sección "Cómo quedó la
combinación" más abajo.

### `ui/`
Composables + ViewModels (`StateFlow<UiState>`). Cada ViewModel recibe su
repositorio (interfaz de `domain`) por constructor, vía
`AppViewModelProvider`. Nunca instancia un DAO ni un servicio Retrofit
directamente.

## Flujo de ejemplo (guardar una reseña)

```
DetalleScreen (ui)
  → DetalleViewModel.guardarResena() (ui)
    → construye Resena (domain/model)
      → ResenaRepository.guardarResena() (interfaz, domain)
        → ResenaRepositoryImpl (data): Resena → ResenaEntity
          → ResenaDao.insert() (data/local)
            → Room persiste
              → MisResenasScreen, que colecciona el Flow de esa tabla,
                se actualiza sola
```

## Cómo quedó la combinación Room + Retrofit (Semana 3)

Antes de esta semana, `DetalleViewModel` llamaba a `libroRepository` y a
`resenaRepository` **por separado** y armaba el `UiState` con los dos
resultados — la combinación pasaba dentro del ViewModel. Eso funcionaba,
pero no era "patrón Repositorio con Clean Architecture": un ViewModel
mezclando dos fuentes de datos por su cuenta es lógica de negocio viviendo
en la capa de presentación.

**Regla que se aplicó:** un Repository representa **una sola fuente**
(Room *o* Retrofit, nunca las dos). Combinar información de más de una
fuente es trabajo de un **caso de uso**, en `domain/usecase/`, que sí puede
depender de dos repositorios a la vez porque su única responsabilidad es
orquestarlos.

### Caso 1 — Detalle: `ObtenerLibroConResenaUseCase`

```
DetalleViewModel.cargar()
  → obtenerLibroConResenaUseCase(libroId)          [domain/usecase]
      → libroRepository.getLibroPorId(libroId)     [remoto: caché de la última búsqueda a Open Library]
      → resenaRepository.getResenaPorLibroId(libroId) [local: Room]
      → arma LibroConResena(libro, resena)          [domain/model]
  ← DetalleViewModel solo desempaqueta LibroConResena en su UiState
    (rating/texto/esFavorito = los de resena si existe, si no, valores vacíos)
```

`DetalleViewModel` ya **no recibe `LibroRepository`** en su constructor —
solo recibe el caso de uso (para leer) y `ResenaRepository` (para guardar,
que es una escritura simple a una sola fuente, no una combinación).

### Caso 2 — Búsqueda: `BuscarLibrosConFavoritosUseCase`

```
BusquedaViewModel.buscar()
  → buscarLibrosConFavoritosUseCase(query)          [domain/usecase]
      → libroRepository.buscarLibros(query)         [remoto: Open Library, N resultados]
      → resenaRepository.getFavoritos().first()     [local: Room, solo esFavorito=true]
      → cruza por libroId: List<Libro>.map { it.copy(esFavorito = it.id in idsFavoritos) }
  ← BusquedaViewModel recibe la lista de Libro ya con esFavorito correcto
```

`BusquedaScreen` solo lee `libro.esFavorito` y pinta un ícono de corazón —
no sabe (ni le importa) que ese booleano salió de cruzar dos fuentes.

### Por qué `Libro.esFavorito` es un campo "derivado"

Se agregó `esFavorito: Boolean = false` al modelo de dominio `Libro`, pero
**ni la API ni `LibroRepositoryImpl` lo llenan nunca** — el JSON de Open
Library no tiene ese concepto. Siempre nace en `false` y solo
`BuscarLibrosConFavoritosUseCase` lo pone en `true`, después de consultar
Room. Es importante poder explicar esta diferencia: hay campos que vienen
de una sola fuente (`titulo`, `autor`, de la API) y campos que existen
*porque* se combinaron dos fuentes (`esFavorito`, calculado).

### Lo que NO cambió (y por qué es la prueba de que el patrón funciona)

`LibroRepository`, `ResenaRepository`, `LibroRepositoryImpl` y
`ResenaRepositoryImpl` no se tocaron para nada esta semana. Solo se agregó
una capa nueva encima. Eso es exactamente lo que se espera de una buena
separación en capas: agregar un comportamiento nuevo (combinar fuentes) sin
modificar el código que ya funcionaba.

## Decisiones y por qué (para defender en la sustentación)

- **Sin Hilt, DI manual**: `AppContainer` (en `di/`) colgado de
  `BookReviewApplication`, y una fábrica única `AppViewModelProvider` con un
  `initializer { }` por ViewModel. Es el patrón oficial del codelab
  *Inventory* de Android Basics with Compose — no agregamos un framework
  para mantener el proyecto simple.
- **Versiones elegidas, no las más nuevas**: Room 2.8.4 (no `androidx.room3`,
  recién salido), Navigation Compose 2.9.8 (no Navigation3), Retrofit 2.11.0
  (no 3.0.0). Se prioriza estabilidad y ecosistema maduro sobre lo último.
- **`LibroRepositoryImpl` con mock (Semana 1) → Open Library real (Semana 2)**:
  la interfaz `LibroRepository` no cambió, así que `BusquedaViewModel` y
  `DetalleViewModel` no se tocaron al conectar la API. Solo cambió la
  implementación, exactamente como estaba planeado.
- **`AppContainer` arma el cliente Retrofit** (`OkHttpClient` +
  `HttpLoggingInterceptor` + `Retrofit` + `GsonConverterFactory`), no el
  ViewModel ni la pantalla. El logging de red (`Level.BODY`) solo se activa
  si la app es debuggable (`context.applicationInfo`), para no loguear
  tráfico completo en un build de release.
- **`getLibroPorId()` usa una caché en memoria** de las últimas búsquedas,
  no una lista fija ni un segundo endpoint: Open Library no tiene un
  "obtener por key" en el formato de `/search.json`. Limitación conocida:
  si se entra a Detalle sin haber buscado antes en esa sesión, la pantalla
  muestra el formulario de reseña sin portada/autor. Se revisará si hace
  falta cuando se trabajen estados de carga/error (Semana 5).
- **`fotoUri` nullable, siempre `null` por ahora**: el campo ya existe en
  `Resena`/`ResenaEntity` para cuando se agregue la cámara con permisos en
  runtime.
- **Casos de uso (`domain/usecase/`) en vez de que un Repository dependa de
  otro**: se pudo haber hecho que `LibroRepositoryImpl` recibiera también
  `ResenaDao` y cruzara ahí mismo, pero eso mezclaría sus responsabilidades
  (uno pasaría a conocer Room *y* Retrofit) y sería más difícil de probar
  por separado. Un caso de uso que depende de dos interfaces de repositorio
  es el lugar correcto para orquestar, según Clean Architecture.

## Problema de build resuelto (Semana 1)

AGP 9.1.1 trae "Kotlin integrado" (built-in Kotlin) activado por defecto.
KSP 2.2.10-2.0.2 (que usa Room para generar el DAO) todavía no es compatible
con eso ([google/ksp#2729](https://github.com/google/ksp/issues/2729)).

**Solución aplicada** en `gradle.properties`:
```properties
android.disallowKotlinSourceSets=false
```
(Se probó `android.builtInKotlin=false` primero, pero esa combinación rompe
con AGP 9.1.1 con un `ClassCastException` interno. El flag de arriba es el
que recomienda el propio issue de KSP.)

También se subió `compileSdk` a 37 porque `androidx.core:core-ktx:1.19.0`
(ya venía en el proyecto) lo exige.

## Bug real resuelto (Semana 2): rutas de Navigation con "/"

Al conectar la API real, tocar un resultado de búsqueda **crasheaba** la
app (esto no lo detectó el build, solo probarla en el emulador). Causa: el
`key` de Open Library viene con una barra adentro, p. ej. `/works/OL27448W`.
`Screen.Detalle.createRoute(libroId)` armaba `"detalle/$libroId"`, y con
ese id quedaba `detalle//works/OL27448W` (doble barra) — Navigation Compose
no lo matchea contra el patrón `detalle/{libroId}` y lanza
`IllegalArgumentException: Navigation destination ... cannot be found`. Los
ids mock de la Semana 1 (`"ol1"`, `"ol2"`, ...) nunca habían expuesto este
caso porque no tenían barras.

**Solución**: codificar el id con `Uri.encode()` al construir la ruta
(`Screen.kt`) y decodificarlo con `Uri.decode()` al leerlo en
`DetalleViewModel` (decodificar un texto sin `%` no hace nada, así que es
seguro aunque Navigation ya lo entregue decodificado por su cuenta).

> Lección para la sustentación: un build verde no prueba que la app
> funcione con datos reales — hay que ejecutarla. Los ids/keys que vienen
> de una API externa pueden tener caracteres que un mock hecho a mano
> nunca iba a tener.

## Verificado en ejecución (no solo compilado)

Semana 2 se probó de punta a punta en el emulador `Pixel_6`: se instaló el
APK debug, se buscó "harry potter" en la pantalla de Búsqueda, se
confirmaron en logcat las llamadas reales
(`GET https://openlibrary.org/search.json?q=harry%20potter` → `200`), se
vieron portadas reales cargando con Coil, y se entró a Detalle desde un
resultado sin errores.

## Estado por semana

- [x] **Semana 1** — estructura de capas, Room + DataStore reales, 4
      pantallas navegando sobre datos mock, build verde (`assembleDebug`).
- [x] **Semana 2** — `data/remote/OpenLibraryApi.kt` + DTOs; `LibroRepositoryImpl`
      llama a Open Library en vez de devolver la lista mock. Probado en
      emulador, no solo compilado.
- [x] **Semana 3** — `domain/usecase/` combina Room + Retrofit (Detalle
      precarga la reseña guardada, Búsqueda marca favoritos). ViewModels ya
      no reciben `LibroRepository` directo. Probado en emulador: reseña
      favorita guardada en Detalle → corazón visible en Búsqueda.
- [ ] **Próxima** (sin fecha aún) — estados visuales de carga/error
      (Semana 5 según el cronograma original), cámara con permisos en
      runtime y guardar `fotoUri` real en `Resena`.
