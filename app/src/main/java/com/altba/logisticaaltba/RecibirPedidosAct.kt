package com.altba.logisticaaltba

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.logisticaaltba.R
import org.json.JSONObject

private const val CAMERA_REQUEST_CODE = 101

class RecibirPedidosAct : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    private var no_guia: String = ""
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recibir_pedidos)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)
        progressDialog = ProgressDialog(this)

        setupPermission()
        codeScanner()

        btnConfirmar.setOnClickListener{
            confirmarRecibido()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun codeScanner(){
        val scanner_view = findViewById<CodeScannerView>(R.id.scanner_view)
        val scannerTextview = findViewById<TextView>(R.id.scannerTextView)
        codeScanner = CodeScanner(this, scanner_view)
        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                runOnUiThread {
                    scannerTextview.text = "Numero de guia: ${it.text}"
                    no_guia = it.text
                    consultar()
                }
            }
            errorCallback = ErrorCallback {
                runOnUiThread {
                    Log.e("Main","Camera initialization error ${it.message}")
                }
            }
        }
        scanner_view.setOnClickListener {
            codeScanner.startPreview()
        }
    }
    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        super.onPause()
        codeScanner.releaseResources()
    }
    private fun setupPermission(){
        val permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED){
            makeRequest()
        }
    }
    private fun makeRequest(){
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Necesitas dar permiso de la camara para usar esta app", Toast.LENGTH_LONG).show()
                }else{
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults)//succesfull request
                }
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun consultar(){
        val URL ="http://sistema.logisticaab.com/API/control.php"//"http://192.168.1.229/sistema.logisticaab.com/API/control.php"
        val requestQueue = Volley.newRequestQueue(this)
        val txtNombre = findViewById<TextView>(R.id.txtClientName)
        val txtDireccion = findViewById<TextView>(R.id.txtDireccion)
        val txtProducto = findViewById<TextView>(R.id.txtProducto)
        val txtTelefono = findViewById<TextView>(R.id.txtTelefono)
        val scannerTextview = findViewById<TextView>(R.id.scannerTextView)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)
        if (no_guia!=""){
            progressDialog!!.setMessage("Espere un segundo")
            progressDialog!!.show()
            val jsonObject = JSONObject()
            jsonObject.put("task", "consultar")
            jsonObject.put("no_guia", no_guia)
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST, URL, jsonObject,
                { response ->
                    if (response.getString("status")=="found"){
                        txtNombre.text="Cliente: "+response.getString("Nombre_destinatario")+" - "+response.getString("Nombre_comprador")
                        txtProducto.text="Producto: "+response.getString("Cantidad")+" "+response.getString("Producto")+" de color "+response.getString("Color")
                        txtDireccion.text="Direccion: "+response.getString("Direccion")
                        txtTelefono.text="Telefono: "+response.getString("Contacto")
                        progressDialog!!.dismiss()
                        btnConfirmar.visibility = View.VISIBLE
                    }else if(response.getString("status")=="notFound"){
                        Toast.makeText(this, "El codigo proporcionado no se ha encontrado", Toast.LENGTH_LONG).show()
                        progressDialog!!.dismiss()
                        txtNombre.text=""
                        txtDireccion.text=""
                        txtProducto.text=""
                        txtTelefono.text=""
                        scannerTextview.text = getString(R.string.notFound)
                        btnConfirmar.visibility = View.INVISIBLE
                    }
                },
                { error ->
                    Toast.makeText(this, "Volley.error: ${error.toString()}", Toast.LENGTH_LONG).show()
                    progressDialog!!.hide()
                })
            requestQueue.add(jsonObjectRequest)
        }
    }
    fun confirmarRecibido(){
        val URL ="http://sistema.logisticaab.com/API/control.php"//"http://192.168.1.229/sistema.logisticaab.com/API/control.php"
        val requestQueue = Volley.newRequestQueue(this)
        progressDialog!!.setMessage("Espere un segundo")
        progressDialog!!.show()
        val jsonObject = JSONObject()
        jsonObject.put("task", "recibir")
        jsonObject.put("no_guia", no_guia)
        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, URL, jsonObject,
            { response ->
                if (response.getString("status")=="success"){
                    progressDialog!!.dismiss()
                    Toast.makeText(this, "Confirmacion de recibido registrada exitosamente", Toast.LENGTH_SHORT).show()
                }else if(response.getString("status")=="fail"){
                    progressDialog!!.dismiss()
                    Toast.makeText(this, "Fallo el registro, intente de nuevo", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Fallo el registro, intente de nuevo", Toast.LENGTH_SHORT).show()
                progressDialog!!.hide()
            })
        requestQueue.add(jsonObjectRequest)
    }
}