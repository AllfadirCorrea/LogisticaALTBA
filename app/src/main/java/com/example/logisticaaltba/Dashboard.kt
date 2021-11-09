package com.example.logisticaaltba

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class Dashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        /*val URL = "http://192.168.1.229/sistema.logisticaab.com/API/connection.php"
        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, URL, null,
            { response ->
                if (response.getString("status")=="success"){
                    Toast.makeText(this, "Conecction succesfully", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this, "Conecction failed", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(this, "Volley.error: ${error.toString()}", Toast.LENGTH_LONG).show()
            })
        requestQueue.add(jsonObjectRequest)*/
    }

    fun recibirPedido(view: View){
        val intent = Intent(this, RecibirPedidosAct::class.java)
        startActivity(intent)
    }
    fun entregarPedido(view: View){
        val intent = Intent(this, EntregarPedidosAct::class.java)
        startActivity(intent)
    }
}