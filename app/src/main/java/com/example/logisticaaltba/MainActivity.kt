package com.example.logisticaaltba

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val URL = "http://sistema.logisticaab.com/API/control.php"
        val requestQueue = Volley.newRequestQueue(this)

        val btnlogin = findViewById<Button>(R.id.btnInit)

        btnlogin.setOnClickListener{
            val txtUsuario = findViewById<EditText>(R.id.txtUsuario)
            val txtPasswer = findViewById<EditText>(R.id.txtPassword)
            val txtPedo = findViewById<TextView>(R.id.txtPedo)
            val task = "auth"
            if (txtUsuario.text.toString().isEmpty()){
                Toast.makeText(this, "Por favor, introduce tu nombre", Toast.LENGTH_LONG).show()
            }else if(txtPasswer.text.toString().isEmpty()){
                Toast.makeText(this, "Por favor, introduce tu contraseña", Toast.LENGTH_LONG).show()
            }else{
                val jsonObject = JSONObject()
                jsonObject.put("task", task)
                jsonObject.put("user", txtUsuario.text.toString())
                jsonObject.put("pass", txtPasswer.text.toString())
                val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, URL, jsonObject,
                    { response ->
                        //(response.getString("user")+" "+response.getString("pass")).also { txtPedo.text = it }
                        if (response.getString("login")=="valid"){
                            Toast.makeText(this, "Bienvenido ${txtUsuario.text.toString()}", Toast.LENGTH_SHORT).show()
                            txtUsuario.setText("")
                            txtPasswer.setText("")

                            val intent = Intent(this, Dashboard::class.java)
                            startActivity(intent)
                        }else if(response.getString("login")=="invalid"){
                            Toast.makeText(this, "Su usuario o contrseña son incorrectos", Toast.LENGTH_LONG).show()
                        }else{
                            txtPedo.text = response.toString()
                            Toast.makeText(this, "Algo debio malir sal", Toast.LENGTH_LONG).show()
                        }
                    },
                    { error ->
                        //Toast.makeText(this, "Volley.error: ${error.toString()}", Toast.LENGTH_LONG).show()
                        txtPedo.text = error.toString()
                    })
                requestQueue.add(jsonObjectRequest)
            }
        }
    }
}