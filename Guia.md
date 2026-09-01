# Guía completa de Android — 1TEL05 (hasta Menús)

Curso: Servicios y Aplicaciones para IoT [1TEL05] — PUCP
Profesor: Oscar Díaz Barriga
Alcance: Clases 1.2, 1.3, 2.1, 2.2, 2.3 (todo lo que cubre el Laboratorio 1)
Basado en: material oficial del curso + proyecto propio "MiApp" (MainActivity + SegundaActivity)

> Nota: esta guía es material de estudio propio, construido a partir de las diapositivas del curso y de mi propio código. No es la solución de ningún laboratorio específico — el objetivo es tener clara la teoría y los patrones de código antes de resolver el enunciado real.

---

## Índice

1. Fundamentos antes de programar
2. Estructura de un proyecto Android
3. UI y ConstraintLayout
4. Eventos
5. Intents (explícitos, implícitos, extras)
6. Navegación: Back vs Up, ActivityResultLauncher
7. Ciclo de vida y Configuration Change
8. Menús: los 4 tipos
9. Proyecto integrador: extendiendo MiApp
10. Preparar el repositorio para entrega
11. Errores frecuentes
12. Checklist final

---

## 1. Fundamentos antes de programar

### 1.1 XML y Java tienen trabajos distintos

```
XML (res/layout)              Java (Activity)
-------------                 ----------------------------
Cómo se ve la pantalla        Qué ocurre al usar la pantalla
TextView                      Cambiar el texto
EditText                      Leer lo escrito
Button                        Detectar el clic
```

Se conectan mediante el `id`:

```xml
android:id="@+id/btnSaludar"
```
```java
Button btnSaludar = findViewById(R.id.btnSaludar);
```

### 1.2 Unidades de medida

- **dp** (density-independent pixels) para tamaños y márgenes de Views.
- **sp** (scalable pixels) para tamaño de texto.
- Nunca usar `px`, `in` o `mm` — dependen del dispositivo.

### 1.3 Qué necesita toda Activity

1. Un archivo XML en `res/layout`.
2. Una clase Java que herede de `AppCompatActivity`.
3. Una llamada a `setContentView()` dentro de `onCreate()`.
4. Una declaración en `AndroidManifest.xml` (Android Studio la agrega solo si creas la Activity desde el asistente).

---

## 2. Estructura de un proyecto Android

```
app
├── manifests
│   └── AndroidManifest.xml       ← describe la app y sus Activities
├── java
│   └── com.example.miapp
│       ├── MainActivity.java
│       └── SegundaActivity.java
└── res
    ├── layout                    ← diseños XML
    ├── menu                      ← menús XML
    ├── drawable                  ← íconos e imágenes
    └── values
        ├── strings.xml
        └── colors.xml
```

**Minimum SDK usado en mi proyecto: API 24** (Android 7.0) — elegido para maximizar compatibilidad (~99% de dispositivos), no hay razón técnica del curso para subirlo.

---

## 3. UI y ConstraintLayout

### 3.1 Jerarquía de Views

Todo en Android es una **View**. Los **ViewGroup** (como `ConstraintLayout`) son Views que contienen otras Views.

```
ConstraintLayout (ViewGroup, padre)
├── EditText (View, hijo)
├── Button (View, hijo)
└── TextView (View, hijo)
```

### 3.2 Regla obligatoria de ConstraintLayout

> **Todo elemento añadido debe tener sus 4 constraints (top, right, bottom, left)** — conectadas al layout padre o a otro elemento. Es la regla explícita del PPT de la Clase 1.3.

Si falta alguna, Android Studio muestra advertencia y el elemento puede saltar a la esquina (0,0) al ejecutar, aunque en el editor visual se vea bien puesto.

### 3.3 Otros layouts vistos

- **LinearLayout** — ordena elementos en una sola dirección (vertical u horizontal).
- **ScrollView** — agrega scroll cuando el contenido es más largo que la pantalla. Solo soporta un hijo directo (por eso normalmente se anida un layout adentro). Para listas largas con imágenes se prefiere RecyclerView (tema posterior del curso).

### 3.4 Subclases de View vistas en clase

`Button`, `EditText`, `CheckBox`, `RadioButton`, `Switch`, `Slider`, `TextView`, `ScrollView`.

### 3.5 Layouts por orientación

Las configuraciones verticales se mantienen también en horizontal (landscape), salvo que se cree una vista alterna. Para validar la orientación en código:

```java
if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
    // lógica específica para landscape
}
```

---

## 4. Eventos

### 4.1 Captura con listener en Java (la forma que uso en MiApp)

```java
btnSaludar.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        // acción
    }
});
```

### 4.2 Forma alternativa: `android:onClick` en XML

```xml
android:onClick="miMetodo"
```
```java
public void miMetodo(View view) {
    // el método debe ser public, retornar void, y recibir un View
}
```

No usar las dos formas al mismo tiempo sobre el mismo botón.

### 4.3 Depuración con Log

```java
private static final String TAG = "LifecycleDemo";
Log.d(TAG, "MainActivity - onCreate");
```

Se revisa en **Logcat** (`View > Tool Windows > Logcat`), filtrando por el tag.

---

## 5. Intents

### 5.1 Explicit Intent — la que ya uso entre MainActivity y SegundaActivity

```java
Intent intent = new Intent(MainActivity.this, SegundaActivity.class);
intent.putExtra("NOMBRE_USUARIO", nombre);
startActivity(intent);
```

Recepción:
```java
String nombreRecibido = getIntent().getStringExtra("NOMBRE_USUARIO");
```

La clave (`"NOMBRE_USUARIO"`) debe ser **idéntica** en ambos lados.

### 5.2 Implicit Intent

El sistema busca qué Activity puede resolver la solicitud (si hay varias, aparece el App Chooser).

```java
Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://developer.android.com"));
if (intent.resolveActivity(getPackageManager()) != null) {
    startActivity(intent);
}
```

> **Importante: desde API 30**, los Implicit Intents deben declararse en el manifiesto o Android devuelve `null`. Se agrega dentro de `<manifest>`, antes de `<application>`:
```xml
<queries>
    <intent>
        <action android:name="android.intent.action.VIEW" />
        <data android:scheme="https" />
    </intent>
</queries>
```

---

## 6. Navegación: Back vs Up, ActivityResultLauncher

### 6.1 Activity Stack (LIFO)

Cuando se inicia una nueva Activity, la anterior se detiene (stopped) y se envía al **Activity Back Stack**. Es una pila: último en entrar, primero en salir.

### 6.2 Dos formas de navegación

| Tipo | Cómo se usa | Quién la gestiona |
|---|---|---|
| **Temporal (Back)** | Botón de regresar del sistema | Android directamente |
| **Ancestral (Up)** | Botón de retroceso dentro del App Bar | Se define en el Manifest |

Para habilitar Up navigation:
```xml
<activity
    android:name=".SegundaActivity"
    android:parentActivityName=".MainActivity" />
```
```java
if (getSupportActionBar() != null) {
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
}
```
Y en `onOptionsItemSelected()`:
```java
if (id == android.R.id.home) {
    finish();
    return true;
}
```

### 6.3 ActivityResultLauncher — devolver datos desde una pantalla

Sirve para cuando necesito **recibir una respuesta** de vuelta, no solo navegar.

**Registrar el launcher** (en la Activity que lanza):
```java
private final ActivityResultLauncher<Intent> launcher =
    registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                String dato = result.getData().getStringExtra("CLAVE");
                // usar el dato
            }
        }
    );
```

**Lanzar la Activity:**
```java
launcher.launch(intent);
```

**Devolver el resultado** (en la Activity que se abrió):
```java
Intent respuesta = new Intent();
respuesta.putExtra("CLAVE", valor);
setResult(RESULT_OK, respuesta);
finish();
```

No confundir: `startActivity(intent)` es para cuando no necesito una respuesta organizada de vuelta; `launcher.launch(intent)` es cuando sí quiero capturar un resultado.

---

## 7. Ciclo de vida y Configuration Change

### 7.1 Orden de los callbacks

```
Abrir app:            onCreate → onStart → onResume
Otra activity encima:  onPause → onStop
Regresa:               onRestart → onStart → onResume
Se destruye:           onPause → onStop → onDestroy
```

- `onPause()` — la actividad sigue parcialmente visible. Código corto y rápido (la siguiente actividad no entra hasta que termine).
- `onStop()` — ya no es visible, pero sigue existiendo con su información.
- `onDestroy()` — última llamada antes de ser destruida. **No confiar en que siempre se llame** — Android puede matar el proceso sin invocarlo. Para persistencia real, mejor guardar en `onPause()`/`onStop()` o usar base de datos.

### 7.2 Configuration Change (rotar pantalla)

Android **destruye y recrea la Activity completa**: `onPause→onStop→onDestroy` seguido inmediatamente de `onCreate→onStart→onResume`.

Para no perder datos temporales (como un contador o puntaje) en ese proceso:

```java
@Override
protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt("cantidad", contador);
}
```
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (savedInstanceState != null) {
        contador = savedInstanceState.getInt("cantidad", 0);
    }
}
```

> Ojo: si la app se cierra completamente y se vuelve a abrir, `savedInstanceState` siempre será `null` — esto solo sobrevive rotaciones/cambios de configuración, no un cierre real.

---

## 8. Menús: los 4 tipos

> **Regla obligatoria de todo el curso: prohibido `switch`/`case` para gestionar eventos de menú.** Usar siempre `if/else` comparando contra `item.getItemId()`. (Motivo técnico real: en ciertos contextos de proyecto los `R.id` dejan de ser constantes en tiempo de compilación, y Java exige constantes para `switch`.)

### 8.1 App Bar / Options Menu

Barra superior con: ícono de navegación, título, íconos de acciones, y overflow menu (3 puntos).

```java
@Override
public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_app_bar, menu);
    return true;
}

@Override
public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.accion_uno) {
        // ...
        return true;
    } else if (id == R.id.accion_dos) {
        // ...
        return true;
    }
    return super.onOptionsItemSelected(item);
}
```

### 8.2 Context Menu

Aparece con long press. Uno por Activity.

```java
registerForContextMenu(tvSaludo); // en onCreate()

@Override
public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    getMenuInflater().inflate(R.menu.menu_contexto, menu);
}

@Override
public boolean onContextItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.opcion_rojo) {
        tvSaludo.setTextColor(Color.RED);
        return true;
    }
    return super.onContextItemSelected(item);
}
```

### 8.3 Contextual Action Bar (CAB)

Reemplaza temporalmente la App Bar cuando se selecciona un elemento. Se activa con `startActionMode()`.

```java
private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_cab, menu);
        return true; // infla el menú
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false; // se llama cada vez que se muestra el ActionMode
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.accion_cab) {
            // ...
            mode.finish(); // cierra la barra tras la acción
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        // limpieza al salir del ActionMode
    }
};

// Para activarlo, por ejemplo con long click:
vista.setOnLongClickListener(v -> {
    startActionMode(actionModeCallback);
    return true;
});
```

### 8.4 Popup Menu

Se ancla a una vista específica (por ejemplo un botón).

```java
private void mostrarPopup(View vistaAncla) {
    PopupMenu popupMenu = new PopupMenu(this, vistaAncla);
    popupMenu.getMenuInflater().inflate(R.menu.menu_popup, popupMenu.getMenu());

    popupMenu.setOnMenuItemClickListener(item -> {
        int id = item.getItemId();
        if (id == R.id.opcion_a) {
            // ...
            return true;
        } else if (id == R.id.opcion_b) {
            // ...
            return true;
        }
        return false;
    });

    popupMenu.show();
}
```

---

## 9. Proyecto integrador: extendiendo MiApp

Mi proyecto ya tiene funcionando: UI con constraints (1.3), evento de click + Explicit Intent (2.1), Context Menu funcional, y logs de ciclo de vida verificados en Logcat (2.2), más un Options Menu con lógica condicional en `SegundaActivity` (2.3).

**Lo que me falta practicar para cubrir el temario completo hasta Menús:**

1. Agregar una tercera Activity (`PerfilActivity`) para practicar:
   - Navegación **Up** desde `SegundaActivity` (con `parentActivityName`).
   - **ActivityResultLauncher** — que `PerfilActivity` devuelva un dato a `SegundaActivity`.
2. Agregar un **Popup Menu** anclado al botón `accion_perfil` de `SegundaActivity`, en vez de (o junto con) el Toast simple que ya tengo.
3. Agregar un **Contextual Action Bar** activado con long press sobre algún contenedor de `SegundaActivity`.
4. Agregar `onSaveInstanceState()` para conservar el puntaje si lo agrego, ante rotación de pantalla.

Este orden replica exactamente el temario de las 5 clases, en secuencia.

---

## 10. Preparar el repositorio para entrega

Según el formato visto en el PB1 real: subir la URL del repositorio de GitHub a Paideia, con formato `LAB1_[Código PUCP]`.

**Qué subir:**
- Código Java, XML de layouts y menús, Manifest, archivos Gradle.

**Qué NO subir:**
- `local.properties`, contraseñas/API keys, la carpeta `build/`. Android Studio suele generar un `.gitignore` que ya excluye esto.

**Comandos básicos desde la Terminal de Android Studio:**
```bash
git init
git add .
git commit -m "Primera versión"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/TU_REPO.git
git push -u origin main
```

Recuerda: el sílabo exige declarar y citar el uso de IA generativa, y el profesor puede pedir el reporte de prompts usados como anexo — ya tengo esos archivos guardados (`prompt_transferencia_gemini_lab1.md` y `prompt_backup_gemini_lab1.md`).

---

## 11. Errores frecuentes

| Error | Revisar |
|---|---|
| `Cannot resolve symbol R` | Hay un error en algún XML o recurso |
| `Cannot resolve symbol id` | El id en Java no coincide con el XML |
| La Activity no abre | Revisar declaración en el Manifest y nombre de clase |
| El extra llega `null` | La clave del `putExtra`/`getExtra` no coincide exactamente |
| La app se cierra sola | Abrir Logcat y buscar `FATAL EXCEPTION` |
| El menú no aparece | Revisar `onCreateOptionsMenu()` y que el tema tenga Action Bar |
| El Context Menu no aparece | Falta `registerForContextMenu()`, o no se hizo long press |
| El Popup no aparece | Falta `popupMenu.show()` |
| El resultado no regresa | Se usó `startActivity()` en vez de `launcher.launch()` |
| Se pierden datos al rotar | Falta `onSaveInstanceState()` / restaurar en `onCreate()` |
| No aparece el botón Up | Falta `setDisplayHomeAsUpEnabled(true)` o `parentActivityName` en el Manifest |
| Switch/case no compila | Prohibido en este curso — usar `if/else` con `getItemId()` |

---

## 12. Checklist final antes del laboratorio

- [ ] Todos los elementos de ConstraintLayout tienen sus 4 constraints
- [ ] Uso `dp` para tamaños/márgenes y `sp` para texto
- [ ] Los `id` de mis Views son descriptivos y coinciden entre XML y Java
- [ ] Los Explicit Intents usan claves idénticas al enviar y recibir
- [ ] Los Implicit Intents (si los uso) tienen su `<queries>` en el Manifest (API 30+)
- [ ] Sé diferenciar Back navigation de Up navigation
- [ ] Entiendo cuándo usar `startActivity()` vs `launcher.launch()`
- [ ] Tengo logs del ciclo de vida y verifiqué el orden en Logcat
- [ ] Sé qué pasa en un Configuration Change y cómo preservar datos con `onSaveInstanceState`
- [ ] **Ningún menú usa switch/case** — todos con if/else
- [ ] Repaso los 4 tipos de menú: App Bar, Context Menu, CAB, Popup Menu
- [ ] Tengo listo el flujo de `git init` → `commit` → `push` para la entrega
