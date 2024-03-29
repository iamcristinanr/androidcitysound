package com.example.citysound

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONException

class StopsList : AppCompatActivity(), StopListAdapter.OnItemClickListener {

    private lateinit var requestQueue: RequestQueue
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerviewstops)

        requestQueue = Volley.newRequestQueue(this)

        // Configurar RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewStops)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val tourId = intent.getIntExtra("tour_id", -1)

        if (tourId != -1) {
            Log.d("TourActivity", "Tour ID: $tourId")
            val url = "http://192.168.0.10:8000/api/tours/$tourId/stops/"
            ApiRequest(url)
        } else {
            Toast.makeText(this, "Tour ID no válido", Toast.LENGTH_SHORT).show()
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.setOnItemSelectedListener { menuItem ->

            // Manejar las selecciones del menú
            when (menuItem.itemId) {
                R.id.nav_search -> {
                    // Abrir la actividad SearchTour si no está abierta ya
                    if (!this::class.java.simpleName.equals("SearchTour", ignoreCase = true)) {
                        startActivity(Intent(this, SearchTour::class.java))
                        finish() // Cerrar la actividad actual
                    }
                    true
                }

                R.id.nav_profile -> {
                    // Abrir la actividad Profile si no está abierta ya
                    if (!this::class.java.simpleName.equals("Profile", ignoreCase = true)) {
                        startActivity(Intent(this, Profile::class.java))
                        finish() // Cerrar la actividad actual
                    }
                    true
                }

                R.id.nav_logout -> {
                    // Abrir la actividad HomeActivity
                    logout()
                    true
                }

                else -> false
            }
        }
    }

    private fun logout() {
        // Limpiar el token de acceso al cerrar sesión
        SessionManager.clearAccessToken(this)
        // Redirigir al usuario a la pantalla de inicio de sesión
        startActivity(Intent(this, Login::class.java))
        finish() // Cerrar la actividad actual
    }

    private fun ApiRequest(url: String) {
        val jsonArrayRequest = object : JsonArrayRequest(Method.GET, url, null,
            { response ->
                try {
                    val stops = mutableListOf<Stop>()

                    for (i in 0 until response.length()) {
                        val stopObject = response.getJSONObject(i)
                        Log.d("Response", "JSON Object at position $i: $stopObject")
                        val stop = Stop(
                            stopObject.getInt("id"),
                            stopObject.getString("name"),
                            stopObject.getString("description"),
                            stopObject.getString("image"),
                        )
                        stops.add(stop)
                    }

                    Log.d("StopsList", "Stops list size: ${stops.size}")
                    // Crear y establecer el adaptador para el RecyclerView
                    val stopListAdapter = StopListAdapter(stops, this@StopsList)
                    val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewStops)
                    recyclerView.adapter = stopListAdapter

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this@StopsList, "Error al obtener la lista de paradas", Toast.LENGTH_SHORT)
                    .show()
            }) {

            // Agregar el token de autorización a las cabeceras de la solicitud
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                // Obtener el token de acceso desde SessionManager
                val sharedPreferences = getSharedPreferences(SessionManager.PREFS_NAME, Context.MODE_PRIVATE)
                val accessToken = SessionManager.getAccessToken(sharedPreferences)
                // Agregar el token de autenticación a las cabeceras si está disponible
                accessToken?.let {
                    headers["Authorization"] = "Token $it"
                }
                return headers
            }
        }

        // Agregar la solicitud a la cola de solicitudes Volley
        requestQueue.add(jsonArrayRequest)
    }

    // Crear y establecer el adaptador para el RecyclerView
    //val stopListAdapter = StopListAdapter(getSampleStops(), this)
    //recyclerView.adapter = stopListAdapter
    //}

    // Implementar el método onItemClick del listener del adaptador
    override fun onItemClick(stop: Stop) {
        // Abrir la actividad de detalles de la parada
        val intent = Intent(this, StopActivity::class.java)
        intent.putExtra("stopName", stop.name)
        intent.putExtra("stopDescription", stop.description)
        intent.putExtra("stopImage", stop.image)
        startActivity(intent)
    }
}

    // Esta es una función de ejemplo que devuelve una lista de paradas de ejemplo
    /*private fun getSampleStops(): List<Stop> {
        val stops = mutableListOf<Stop>()
        // Agrega aquí las paradas que desees mostrar en la lista
        stops.add(Stop("Stop 1", "Description 1"))
        stops.add(Stop("Stop 2", "Description 2"))
        stops.add(Stop("Stop 3", "Description 3"))
        return stops*/


