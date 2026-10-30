# Guía de defensa — TechnoStore Market

## Demo sugerida (5-7 min)
1. **Registro** con datos inválidos → mostrar mensajes de error por campo. Registrar cuenta válida.
2. **Catálogo** (Firestore): buscar «samsung», filtrar por categoría, abrir un producto (argumento `productoId`).
3. **Agregar al carrito** desde catálogo y detalle → Snackbar + insignia en la barra inferior.
4. **Carrito** (Firestore, tiempo real): cambiar cantidades, quitar un producto (Dialog), intentar superar el stock.
5. **Realizar pedido**: Dialog con dirección vacía (error) → completar → se guarda en `usuarios/{uid}/pedidos` y se vacía el carrito → detalle del pedido. Mostrarlo en la consola de Firebase.
6. **Historial**: cancelar un pedido (UPDATE) y eliminar otro (DELETE).
7. **Perfil**: editar teléfono/dirección (validaciones), cerrar sesión, iniciar con otra cuenta y mostrar que el carrito/historial es independiente.
8. Apagar el Wi-Fi: el catálogo sigue cargando desde la caché de Firestore; un pedido sin red queda «Pendiente de sincronizar» y se sube al volver la conexión.

## Decisiones técnicas (preguntas probables)
- **¿Por qué solo Firebase?** Es el requerimiento del caso de estudio: Firestore centraliza catálogo, carritos, pedidos y perfiles en la nube, sincroniza en tiempo real y tiene caché offline propia, así que no hace falta Room ni una API propia.
- **¿Cómo se separan los datos por usuario?** Carrito, pedidos y perfil viven dentro de `usuarios/{uid}`; las reglas de Firestore impiden leer datos de otro uid. `SessionManager.userIdFlow` + `flatMapLatest` re-suscribe los Flows al cambiar de sesión.
- **¿Por qué `StateFlow` y un `UiState` inmutable?** Estado único y observable por pantalla; la UI es una función del estado. Los mensajes de un solo uso (Snackbar) se «consumen» desde el ViewModel.
- **¿Dónde se usa `Dispatchers.IO`?** En los repositorios (`withContext(Dispatchers.IO)`), para que el ViewModel (`viewModelScope.launch`) nunca bloquee el hilo principal.
- **¿Qué pasa si no hay red al pedir?** Firestore guarda la escritura en su caché local y la sube al reconectar; el detalle muestra «Pendiente de sincronizar» (`metadata.hasPendingWrites()`).
- **¿Cómo llegan los argumentos al ViewModel?** `NavBackStackEntry` → `SavedStateHandle` (`createSavedStateHandle()` en la `AppViewModelFactory`).
- **¿Por qué inyección manual y no Hilt?** El alcance es pequeño; `AppContainer` + factory mantienen el desacople sin agregar procesamiento de anotaciones adicional.
- **Pedido atómico:** `PedidoRepository.registrarPedido` usa un *batch* de Firestore: crea el pedido y borra los ítems del carrito en una sola operación.

## Mapeo contra la lista de cotejo
| Criterio | Evidencia |
|---|---|
| Arquitectura MVVM (15%) | `viewmodel/`, `data/repository/`, `model/`, `di/` |
| Gestión del estado (10%) | `MutableStateFlow`/`StateFlow` en cada ViewModel, `collectAsStateWithLifecycle` |
| Persistencia (15%) | Firestore — CRUD de carrito, pedidos (con cancelar/eliminar) y perfil; caché offline |
| API REST (15%) | Servicios en la nube de Firebase vía SDK: `data/firebase/` + repositorios — Auth y Firestore con corrutinas (`await`), `llamadaSegura` + `toUserMessage` |
| Navegación (10%) | `ui/navigation/` — NavHost, NavController, 2 rutas con argumentos |
| Interfaz (10%) | Compose + M3, `ui/components/`, tema claro/oscuro |
| Calidad de código (10%) | Capas, nombres descriptivos, sin duplicidad, `util/Validadores` con pruebas |
| Documentación (5%) | `README.md` (agregar capturas en `docs/capturas/`) |
| Presentación (10%) | Esta guía |
