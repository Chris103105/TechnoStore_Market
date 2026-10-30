# TechnoStore Market

Aplicación Android para gestionar los pedidos de la tienda de conveniencia TechnoStore Market. Permite consultar el catálogo, armar un carrito, registrar pedidos y revisar el historial de compras.

Proyecto de Cátedra de Desarrollo de Software para Móviles, Universidad Don Bosco.

## Integrantes

| Nombre | Carné |
|---|---|
| Guillermo Alejandro Espinoza Barahona | EB170303 |
| Christopher Steven Jovel Beltrán | JB251834 |
| Elisabet Beatriz Marroquín González | MG251360 |
| Edgardo Giovanni Cortez Valladares | CV211195 |

## Funcionalidades

- Registro e inicio de sesión con correo y contraseña o con Google, y recuperación de contraseña.
- Catálogo de productos con búsqueda por nombre y filtro por categoría.
- Detalle de producto con selector de cantidad.
- Carrito que se actualiza en tiempo real y respeta el stock disponible.
- Registro de pedidos con dirección de entrega y método de pago.
- Historial de compras con detalle, cancelación y eliminación de pedidos.
- Perfil de usuario editable (nombre, teléfono y dirección) y cierre de sesión.

## Tecnologías

- Kotlin y Jetpack Compose con Material Design 3
- Arquitectura MVVM con patrón Repository y `StateFlow`
- Navigation Compose
- Firebase Authentication y Cloud Firestore
- Corrutinas y Flow
- Coil para las imágenes

## Arquitectura

La app sigue el patrón MVVM en tres capas:

```
UI (Compose)  ->  ViewModel  ->  Repository  ->  Firebase Auth / Cloud Firestore
     ^                |
     └── StateFlow ───┘
```

- **UI:** las pantallas solo muestran el estado que reciben y envían los eventos del usuario al ViewModel.
- **ViewModel:** contiene la lógica de cada pantalla y expone su estado con `StateFlow`.
- **Repository:** es la única capa que habla con Firebase. Devuelve `Result` para manejar los errores.

Las dependencias se crean en `AppContainer` y se pasan a los ViewModels con `AppViewModelFactory`.

## Estructura del proyecto

```
app/src/main/java/edu/udb/technostore_market/
├── MainActivity.kt
├── TechnoStoreApp.kt          # Crea el contenedor de dependencias
├── di/                        # AppContainer
├── model/                     # Producto, ItemCarrito, Pedido, PerfilUsuario
├── data/
│   ├── firebase/              # Rutas de Firestore, conversores y datos iniciales del catálogo
│   └── repository/            # Auth, Producto, Carrito, Pedido y Perfil
├── viewmodel/                 # Un ViewModel por pantalla
├── util/                      # Validaciones, formatos y manejo de errores
└── ui/
    ├── navigation/            # Rutas y NavHost
    ├── components/            # Componentes reutilizables
    ├── auth/ catalogo/ detalle/ carrito/ pedidos/ perfil/
    └── theme/                 # Colores, tipografía y tema claro/oscuro
```

## Base de datos

Toda la información se guarda en Cloud Firestore:

| Colección | Contenido |
|---|---|
| `categorias` | Categorías del catálogo |
| `productos` | Nombre, descripción, precio, stock, categoría e imagen |
| `usuarios/{uid}` | Datos del perfil del usuario |
| `usuarios/{uid}/carrito` | Productos en el carrito |
| `usuarios/{uid}/pedidos` | Historial de pedidos |

Las reglas de seguridad están en `firestore.rules`: cada usuario solo puede leer y modificar sus propios datos. Firestore guarda una copia local, así que el catálogo se puede consultar sin conexión y los pedidos se sincronizan al recuperar la red.

## Instalación

**Requisitos**

- Android Studio actualizado (compatible con AGP 9)
- Emulador o dispositivo con Android 10 (API 29) o superior y Google Play Services

**Pasos**

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/Chris103105/TechnoStore_Market.git
   ```
2. Abrir el proyecto en Android Studio y esperar a que Gradle sincronice.
3. El archivo `app/google-services.json` ya viene incluido y apunta al proyecto de Firebase `technostoremarket-3ec5e`.
4. En la consola de Firebase:
   - En *Authentication > Sign-in method*, habilitar Correo/contraseña y Google.
   - En *Firestore Database*, crear la base de datos y publicar el contenido de `firestore.rules` en la pestaña Reglas.
5. Ejecutar la app.

La primera vez que un usuario inicia sesión, la app carga automáticamente el catálogo inicial en Firestore.

Para ejecutar las pruebas unitarias:

```bash
./gradlew test
```

## Capturas de pantalla

| Login | Catálogo | Detalle |
|---|---|---|
| ![Login](docs/capturas/01_login.jpeg) | ![Catálogo](docs/capturas/02_catalogo.jpeg) | ![Detalle](docs/capturas/03_detalle.jpeg) |

| Carrito | Confirmar pedido | Historial |
|---|---|---|
| ![Carrito](docs/capturas/04_carrito.jpeg) | ![Pedido](docs/capturas/05_dialogo_pedido.jpeg) | ![Historial](docs/capturas/06_historial.jpeg) |

| Detalle de pedido | Perfil |
|---|---|
| ![Detalle de pedido](docs/capturas/07_detalle_pedido.jpeg) | ![Perfil](docs/capturas/08_perfil.jpeg) |
