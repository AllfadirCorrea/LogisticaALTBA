package com.altba.logisticaaltba

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.BarcodeUtils.encodeBitmap
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.logisticaaltba.R
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.Permission
import java.util.*
import kotlin.jvm.Throws

private const val CAMERA_REQUEST_CODE = 101

class EntregarPedidosAct : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    private var loading: ProgressDialog? = null
    var no_guia: String = ""
    var bitmap: Bitmap? = null

    var PICK_IMAGE_REQUEST = 1
    var KEY_IMAGE = "upload"
    var KEY_GUIDE = "guia"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entregar_pedidos)

        val btnChoosePic = findViewById<Button>(R.id.btnChoosePic)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)
        loading = ProgressDialog(this)

        setupPermission()
        codeScanner()

        btnChoosePic.setOnClickListener {
            showFileChooser()
        }
        btnConfirmar.setOnClickListener {
            confirmarEntrega()
        }
    }

    //Code scanner actions
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
    @SuppressLint("SetTextI18n")
    private fun consultar(){
        val URL ="http://sistema.logisticaab.com/API/control.php"//"http://192.168.1.229/sistema.logisticaab.com/API/control.php"
        val requestQueue = Volley.newRequestQueue(this)
        val txtNombre = findViewById<TextView>(R.id.txtClientName)
        val txtDireccion= findViewById<TextView>(R.id.txtDireccion)
        val txtProducto = findViewById<TextView>(R.id.txtProducto)
        val txtTelefono = findViewById<TextView>(R.id.txtTelefono)
        val txtPago = findViewById<TextView>(R.id.txtPago)
        val scannerTextview = findViewById<TextView>(R.id.scannerTextView)
        val btnChoosepic = findViewById<Button>(R.id.btnChoosePic)
        if (no_guia!=""){
            loading!!.setMessage("Espere un segundo")
            loading!!.show()
            val jsonObject = JSONObject()
            jsonObject.put("task", "consultar")
            jsonObject.put("no_guia", no_guia)
            val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, URL, jsonObject,
                { response ->
                    if (response.getString("status")=="found"){
                        txtNombre.text="Cliente: "+response.getString("Nombre_destinatario")+" - "+response.getString("Nombre_comprador")
                        txtProducto.text="Producto: "+response.getString("Cantidad")+" "+response.getString("Producto")+" de color "+response.getString("Color")
                        txtDireccion.text="Direccion: "+response.getString("Direccion")
                        txtTelefono.text="Telefono: "+response.getString("Contacto")
                        txtPago.text="Pago: "+response.getString("Pago")
                        loading!!.dismiss()
                        btnChoosepic.visibility = View.VISIBLE
                    }else if(response.getString("status")=="notFound"){
                        Toast.makeText(this, "El codigo proporcionado no se ha encontrado", Toast.LENGTH_LONG).show()
                        txtNombre.text="Cliente: "
                        txtDireccion.text="Producto: "
                        txtProducto.text="Direccion: "
                        txtTelefono.text="Telefono: "
                        txtPago.text="Pago: "
                        scannerTextview.text = getString(R.string.notFound)
                        btnChoosepic.visibility = View.INVISIBLE
                        loading!!.dismiss()
                    }
                },
                { error ->
                    Toast.makeText(this, "Volley.error: ${error.toString()}", Toast.LENGTH_LONG).show()
                    btnChoosepic.visibility = View.INVISIBLE
                    loading!!.hide()
                })
            requestQueue.add(jsonObjectRequest)
        }
    }

    //Camera permission actions
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

    //btnChoosePic
    private fun showFileChooser(){
        val alertDialogBuilder = AlertDialog.Builder(this)
            .setTitle("Confirmar entrega")
            .setMessage("Para confirmar la entrega del mueble es necesario tomar una foto de la credencial del comprador y despues seleccionarla. ¿Desea continuar?")
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton(android.R.string.yes) { dialog, which ->
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Selecciona la imagen"), PICK_IMAGE_REQUEST)
            }
            .setNegativeButton(android.R.string.no) { dialog, which ->
                Toast.makeText(this, "No se ha confirmado el pedido", Toast.LENGTH_SHORT).show()
            }
            alertDialogBuilder.show()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null){
            val filePath = data.data
            try{
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                val btnChoosepic = findViewById<Button>(R.id.btnChoosePic)
                btnChoosepic.visibility = View.INVISIBLE
                val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)
                btnConfirmar.visibility = View.VISIBLE
            }catch (e: IOException){
                e.printStackTrace()
            }
        }

    }

    //btnConfirmar
    private fun confirmarEntrega(){
        val uploadURL ="http://sistema.logisticaab.com/API/upload.php"//"http://192.168.1.229/sistema.logisticaab.com/API/upload.php"
        val txtNombre = findViewById<TextView>(R.id.txtClientName)
        val txtDireccion= findViewById<TextView>(R.id.txtDireccion)
        val txtProducto = findViewById<TextView>(R.id.txtProducto)
        val txtTelefono = findViewById<TextView>(R.id.txtTelefono)
        val txtPago = findViewById<TextView>(R.id.txtPago)
        val scannerTextview = findViewById<TextView>(R.id.scannerTextView)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)
        loading!!.setMessage("Espere un segundo")
        loading!!.show()

        val uploadImage: StringRequest = object : StringRequest(
            Method.POST, uploadURL,
            Response.Listener { response ->
                if (response == "success"){
                    val URL ="http://sistema.logisticaab.com/API/control.php"//"http://192.168.1.229/sistema.logisticaab.com/API/control.php"
                    val requestQueue = Volley.newRequestQueue(this)
                    val jsonObject = JSONObject()
                    jsonObject.put("task", "entregar")
                    jsonObject.put("no_guia", no_guia)
                    /* Cambio de la BD */
                    val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, URL, jsonObject,
                       {  respuesta ->
                            if (respuesta.getString("status")=="success"){
                                Toast.makeText(this, "Entrega registrada exitosamente", Toast.LENGTH_SHORT).show()
                                txtNombre.text="Cliente: "
                                txtProducto.text="Producto: "
                                txtDireccion.text="Direccion: "
                                txtTelefono.text="Telefono: "
                                txtPago.text="Pago: "
                                scannerTextview.text = getString(R.string.scan)
                                btnConfirmar.visibility = View.INVISIBLE
                                loading!!.dismiss()
                            }else if(respuesta.getString("status")=="failed"){
                                Toast.makeText(this, "Fallo el registro, intente de nuevo", Toast.LENGTH_SHORT).show()
                                loading!!.dismiss()
                            }
                        },
                        { error ->
                            Toast.makeText(this, "Fallo el registro, intente de nuevo", Toast.LENGTH_SHORT).show()
                            loading!!.dismiss()
                        })
                    requestQueue.add(jsonObjectRequest)
                }
                else if(response == "failed"){
                    Toast.makeText(this, "Fallo la carga de la imagen, favor de ponerse en contacto con Logistica AB para informar", Toast.LENGTH_SHORT).show()
                    loading!!.dismiss()
                }else{
                    Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                    loading!!.dismiss()

                }
            },Response.ErrorListener { error ->
                loading!!.dismiss()
                Toast.makeText(this@EntregarPedidosAct, /*.message.toString()*/"Fallo el regsitro, ponerse en contacto con logistica AB", Toast.LENGTH_LONG).show()
            }
        ){
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val upload = encodeBitmap(bitmap)
                val guia = no_guia
                val params: MutableMap<String, String> = Hashtable()
                params[KEY_IMAGE] = upload
                params[KEY_GUIDE] = guia
                return params
            }
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(uploadImage)

    }
    private fun encodeBitmap(bitmap: Bitmap?): String{
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteofimages = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteofimages, Base64.DEFAULT)
    }

}