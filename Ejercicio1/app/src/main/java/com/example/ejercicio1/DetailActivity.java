package com.example.ejercicio1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    private TextView tvDetalle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Vinculación de vistas por ID (Clase 2.1)
        tvDetalle = findViewById(R.id.tvDetalle);
        Button btnFinalizar = findViewById(R.id.btnFinalizar);

        // 1. Recepción de datos enviados desde MainActivity (Clase 2.1)
        Intent intentRecibido = getIntent();
        if (intentRecibido != null && intentRecibido.hasExtra("usuario")) {
            String usuario = intentRecibido.getStringExtra("usuario");
            tvDetalle.setText("Bienvenido: " + usuario);
        }

        // 2. Registrar componente para Context Menu por pulsación prolongada (Clase 2.3)
        registerForContextMenu(tvDetalle);

        // 3. Preparación de datos de retorno y finalización (Clase 2.2)
        btnFinalizar.setOnClickListener(v -> {
            Intent intentDevolucion = new Intent();
            intentDevolucion.putExtra("llave_retorno", "Proceso completado con éxito");
            setResult(Activity.RESULT_OK, intentDevolucion); // Establece el resultado OK
            finish(); // Destruye esta Actividad para volver a la anterior en la pila (Back Stack)
        });
    }

    // 4. Inflar el Context Menu cuando el usuario mantiene presionado tvDetalle (Clase 2.3)
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_contextual, menu);
    }

    // 5. Procesar clic en el ítem del Context Menu (Clase 2.3)
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_opcion_contextual) {
            tvDetalle.setText("¡Texto modificado desde el Context Menu!");
            return true;
        }
        return super.onContextItemSelected(item);
    }
}