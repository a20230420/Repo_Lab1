package com.example.ejercicio1;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Componentes visuales (Subclases de View)[cite: 3]
    private EditText etNombre;
    private TextView tvResultado;
    private int contadorSelecciones = 0;

    // 1. Registro para recibir datos de regreso desde DetailActivity (Clase 2.2)[cite: 5]
    private final ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    // Lectura del extra devuelto[cite: 4, 5]
                    String datoDevuelto = result.getData().getStringExtra("llave_retorno");
                    tvResultado.setText("Retornado: " + datoDevuelto);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vinculación de vistas por ID (Clase 2.1)[cite: 4]
        etNombre = findViewById(R.id.etNombre);
        tvResultado = findViewById(R.id.tvResultado);
        Button btnEnviar = findViewById(R.id.btnEnviar);
        Button btnPopup = findViewById(R.id.btnPopup);

        // 2. Intent Explícito: Envío de datos y solicitud de respuesta (Clase 2.1 y 2.2)[cite: 4, 5]
        btnEnviar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("usuario", etNombre.getText().toString()); // Transmisión de datos[cite: 4]
            activityResultLauncher.launch(intent); // Inicia la actividad en la pila (Back Stack)[cite: 5]
        });

        // Despliegue de Popup Menu
        btnPopup.setOnClickListener(this::mostrarPopupMenu);

        // 3. Recuperación de estado guardado ante giros de pantalla (Clase 2.2)[cite: 5]
        if (savedInstanceState != null) {
            contadorSelecciones = savedInstanceState.getInt("KEY_CONTADOR", 0);
            Log.d("MainActivity", "Contador recuperado: " + contadorSelecciones); // Logging[cite: 3, 5]
        }
    }

    // Persistencia temporal del estado antes de la destrucción de la Activity (Clase 2.2)[cite: 5]
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("KEY_CONTADOR", contadorSelecciones);
    }

    // 4. Inflar el Options Menu / App Bar (Clase 2.3)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Procesar clic en el Options Menu (Clase 2.3)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_buscar) {
            // 5. Intent Implícito con verificación resolveActivity() (Clase 2.1)[cite: 4]
            Intent intentWeb = new Intent(Intent.ACTION_WEB_SEARCH);
            intentWeb.putExtra(SearchManager.QUERY, "Android Development");
            if (intentWeb.resolveActivity(getPackageManager()) != null) {
                startActivity(intentWeb);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // 6. Creación y manejo del Popup Menu anclado al botón (Clase 2.3)
    private void mostrarPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.menu_popup, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            contadorSelecciones++;
            Toast.makeText(this, "Seleccionado: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            return true;
        });
        popup.show();
    }
}