package com.example.logisticaaltba

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

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
}