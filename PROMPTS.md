# Prompts utilizados

Usamos Gemini en Android Studio.

## Prompt 1 · Inicio de sesión

> Actúa como desarrollador Android experto en Kotlin y Jetpack Compose. Revisa la pantalla de inicio de sesión y su ViewModel actuales antes de proponer cambios.
>
> Objetivo:
> Mejorar la experiencia durante el inicio de sesión sin cambiar la autenticación con Firebase ni el diseño general de la app.
>
> Implementa estos cambios:
> - Conserva el correo escrito si el inicio de sesión falla o está cargando.
> - Mantén la pantalla visible durante la carga y muestra el progreso en el botón “Ingresar”.
> - Agrega un control accesible para mostrar u ocultar la contraseña; por defecto, debe permanecer oculta.
> - Deshabilita el botón mientras se procesa la solicitud para evitar envíos repetidos.
> - Mantén la validación de campos vacíos y la opción “Olvidé mi contraseña”.
>
> Restricciones:
> - Conserva la arquitectura MVVM existente.
> - No agregues librerías si Compose ya ofrece los componentes necesarios.
> - No cambies Firebase, las reglas de Firestore ni otras pantallas.
> - No incluyas credenciales reales en el código.
>
> Al terminar, resume los archivos modificados y cómo probar manualmente los estados de carga, error y visibilidad de la contraseña.

- **Respuesta y aplicación.** Gemini propuso el botón para ver u ocultar la contraseña y el indicador de carga en «Ingresar». Aplicamos ambos cambios. Durante el ingreso, la navegación muestra una pantalla de carga aparte y el indicador del botón queda fuera de vista.
- **Qué aprendimos.** Para conservar el correo durante la carga también debemos revisar cuándo la navegación retira el formulario de la pantalla.

## Prompt 2 · Mensajes de error al ingresar

> Actúa como desarrollador Android experto en Kotlin y Firebase Authentication/Firestore. Inspecciona el repositorio y el flujo de inicio de sesión actual antes de modificarlo.
>
> Objetivo:
> Mostrar mensajes claros y seguros cuando falla el inicio de sesión o la lectura del perfil en Firestore.
>
> Implementa:
> - Un mensaje general y seguro para credenciales incorrectas, sin revelar si una cuenta existe.
> - Un mensaje distinto cuando no hay conexión o ocurre un error de red.
> - Un mensaje útil si Firebase Authentication funciona, pero no se puede leer el documento de perfil o su rol en Firestore.
> - Conserva los mensajes actuales para campos vacíos y el flujo de recuperación de contraseña.
> - Evita mostrar excepciones técnicas, datos sensibles o credenciales en la interfaz.
>
> Restricciones:
> - Mantén MVVM y concentra la conversión de errores en la capa apropiada.
> - No cambies la configuración Firebase, las reglas de Firestore ni el formato de los documentos.
> - No alteres el comportamiento de otras pantallas.
> - Añade solo cambios relacionados con este objetivo.
>
> Al terminar, indica los archivos modificados y una lista breve de escenarios para verificar cada mensaje.

- **Respuesta y aplicación.** Gemini propuso separar los errores de credenciales, conexión y lectura del perfil. Aplicamos esa clasificación en el ViewModel con mensajes sencillos.
- **Qué aprendimos.** El ingreso puede fallar al validar las credenciales en Authentication o al consultar el perfil en Firestore.

## Prompt 3 · Pruebas unitarias del catálogo

> Actúa como desarrollador Android experto en Kotlin, MVVM y pruebas unitarias. Revisa ListaViewModel, ItemRepository, Notificador y las dependencias actuales del proyecto Productos antes de hacer cambios.
>
> Agrega tres pruebas unitarias para comprobar el comportamiento del catálogo:
>
> 1. Si la sincronización es exitosa, el ViewModel entrega los productos en UiState.Exito.
> 2. Si falla la conexión y no hay productos guardados, el ViewModel devuelve UiState.Error.
> 3. Si falla la conexión y ya hay productos guardados, el ViewModel conserva la lista y muestra el aviso de que está usando los datos guardados.
>
> Usa un repositorio falso para simular las respuestas y un notificador falso para que las pruebas se ejecuten sin emulador ni servicios reales. Ten en cuenta que el ViewModel inicia la carga al crearse y expone el estado mediante StateFlow.
>
> Mantén los cambios en los archivos de prueba y agrega solo las dependencias necesarias. Si hace falta modificar el código de la app, explica primero qué cambio propones y por qué. Escribe el código sin comentarios y pon a cada prueba un nombre que indique qué comprueba. Limita el contexto a los archivos de código y Gradle necesarios. No leas ni incluyas google-services.json, local.properties o credenciales.
>
> Al terminar, indica qué archivos agregaste o modificaste y cómo ejecutar las tres pruebas desde Android Studio.

- **Respuesta y aplicación.** Gemini generó tres pruebas con un repositorio y un notificador falsos. Ajustó las pruebas tras la primera ejecución y verificamos con Gradle que las tres pasaron.
- **Qué aprendimos.** Podemos simular una falla de red y comprobar si el ViewModel muestra un error o conserva los productos guardados, sin usar el emulador.
