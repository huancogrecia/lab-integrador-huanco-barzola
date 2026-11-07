# Productos

Catálogo de laptops de DummyJSON para el laboratorio integrador de Programación en Móviles. La app permite buscar por nombre o marca, ordenar por nombre o precio, consultar el detalle y guardar favoritos.

**Integrantes** Grecia Huanco Ticona y José Barzola Veliz.

## Cómo funciona

El tema es Productos y la variante es `laptops`. El usuario puede consultar el catálogo y sus favoritos. El administrador también puede ver los conteos de Room, sincronizar productos y limpiar la caché.

La interfaz usa Jetpack Compose y Material 3. Los ViewModels reciben las acciones de las pantallas y les exponen su estado. El repositorio usa Retrofit para consultar la lista en `GET /products/category/laptops` y el detalle en `GET /products/{id}`. Room conserva el catálogo y los favoritos en el dispositivo, así que la lista puede mostrarse sin internet después de una primera carga. Firebase Authentication valida la cuenta y Firestore proporciona el rol. La app emite una notificación local cuando termina una sincronización exitosa.

El modo oscuro se activa desde Perfil y la preferencia se conserva con DataStore al volver a abrir la app.

## Cómo compilar

1. Abre la carpeta del proyecto en Android Studio. Usa JDK 21 para Gradle e instala el SDK de Android 36. El dispositivo de prueba debe tener Android 8.0 (API 26) o superior.
2. Registra la app Android `com.tecsup.productos` en tu proyecto de Firebase y descarga `google-services.json`. Colócalo en `app/google-services.json`.
3. Sincroniza Gradle y ejecuta el módulo `app` en el emulador o dispositivo. También puedes generar el APK de depuración con `./gradlew :app:assembleDebug`. El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.
4. Para ver las notificaciones de sincronización en Android 13 o superior, concede el permiso cuando la app lo solicite.

## Usuarios de prueba

1. En Firebase Authentication, habilita el proveedor de correo electrónico y contraseña. Crea dos cuentas de prueba y copia el UID de cada una.
2. En Cloud Firestore, crea la colección `usuarios`. Crea un documento por cuenta y usa su UID como ID del documento.
3. Agrega los campos de texto `nombre` y `rol`. Asigna `usuario` a una cuenta y `admin` a la otra.
4. Publica las reglas de `firestore.rules`. Cada cuenta podrá leer su propio perfil y el rol se administrará desde Firebase Console.
5. Ingresa con las dos cuentas para comprobar la navegación. El panel Admin solo aparece con el rol `admin`.

Para restablecer la contraseña de una cuenta de prueba, escribe su correo en el login y pulsa **Olvidé mi contraseña**. Firebase envía el enlace de recuperación al correo registrado.

Mantén `google-services.json` y las contraseñas fuera de Git.

## Capturas

1. Inicio de sesión ([ver captura](capturas/01-login.png)).
2. Catálogo ([ver captura](capturas/02-catalogo.png)).
3. Detalle de un producto ([ver captura](capturas/03-detalle.png)).
4. Favoritos ([ver captura](capturas/04-favoritos.png)).
5. Panel Admin ([ver captura](capturas/05-panel-admin.png)).
6. Notificación de sincronización ([ver captura](capturas/06-notificacion.png)).

## Reparto de tareas

| Integrante | Aportes |
| --- | --- |
| Grecia Huanco Ticona | Proyecto base, estados de interfaz, catálogo de prueba, detalle, base de datos Room, configuración de Firebase, buscador, ordenamiento, perfil, recuperación de contraseña y documentación. |
| José Barzola Veliz | Estructura MVVM, contratos, conexión con la API, catálogo desde Room, favoritos, modo sin conexión, inicio de sesión, navegación por roles, panel Admin, notificaciones y modo oscuro con DataStore. |
