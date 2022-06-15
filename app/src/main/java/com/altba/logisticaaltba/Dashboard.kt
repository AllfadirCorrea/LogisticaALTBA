package com.altba.logisticaaltba

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.altba.logisticaaltba.LogisiticaAbApplication.Companion.pref
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.logisticaaltba.R

class Dashboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
    }

    fun recibirPedido(view: View){
        val intent = Intent(this, RecibirPedidosAct::class.java)
        startActivity(intent)
    }
    fun entregarPedido(view: View){
        val intent = Intent(this, EntregarPedidosAct::class.java)
        startActivity(intent)
    }

    fun cerrarSesion(view: View){
        pref.wipe()
        onBackPressed()
    }
}