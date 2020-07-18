package com.e.contentproviderexample

import android.Manifest.permission.READ_CONTACTS
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ActivityInfoCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

private const val TAG = "MainActivity"
private const val REQUEST_CODE_READ_CONTACTS = 1

class MainActivity : AppCompatActivity() {

    private var readGranted = false //許可があるかないか分かりやすくする為に定義

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setContentView(R.layout.activity_snack_bars)

        val hasReadContactPermission = ContextCompat.checkSelfPermission(this, READ_CONTACTS) //二つ目の引数はandroidからimportする
        Log.d(TAG, "onCreate: checkSelfPermission returned $hasReadContactPermission")

        if (hasReadContactPermission == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"onCreate: permission granted")
            readGranted = true
        } else {
            Log.d(TAG, "onCreate: requesting permission")
            ActivityCompat.requestPermissions(this, arrayOf(READ_CONTACTS), REQUEST_CODE_READ_CONTACTS)
        }

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Log.d(TAG, "fab onClick: starts")
            if (readGranted) {
                val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

                val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                    projection,
                    null,
                    null,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

                val contacts = ArrayList<String>() //連絡先を保持するリストを作る
                cursor?.use {  //カーソルをループさせる
                    while (it.moveToNext()) {
                        contacts.add(it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)))
                    }
                }

                val adapter = ArrayAdapter<String>(this, R.layout.contact_detail, R.id.name, contacts)
                contacts_names.adapter = adapter

            } else {
                Snackbar.make(view, "連絡先を許可してください", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Grant Access") {
                        Log.d(TAG, "Snackbar onClick: starts")
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_CONTACTS)) {
                            Log.d(TAG, "Snackbar onClick: calling requestPermissions")
                            ActivityCompat.requestPermissions(this, arrayOf(READ_CONTACTS), REQUEST_CODE_READ_CONTACTS)

                        } else {
                            //今後表示しないを選択されたうえで許可を拒否された場合
                            Log.d(TAG, "Snaclbar onclick: launcing Settings")
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS //アプリの設定画面を開くためのintent
                            val uri = Uri.fromParts("package", this.packageName, null) //uriでアプリを指定
                            Log.d(TAG, "Snackbar onClick: Uri is $uri")
                            intent.data = uri
                            this.startActivity(intent) //uriを指定したアプリの設定画面を開く
                        }
                        Log.d(TAG, "Snaclbar onclick: ends")
                    }.show()
            }
            Log.d(TAG, "fab onClick: ends")
        }
        Log.d(TAG, "onCreate: ends")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult: starts")
        when (requestCode) {
            REQUEST_CODE_READ_CONTACTS -> {
                readGranted = if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //許可が取れて、連絡先タスクを実行する必要がある。
                    Log.d(TAG, "onRequestPermissionsResult: permission granted")
                    true
                } else {
                    //許可が取れていないので、この権限に依存する機能を無効にする
                    Log.d(TAG, "onRequestPermissionsResult: permission refused")
                    false
                }
//                fab.isEnabled = readGranted
            }
        }
        Log.d(TAG, "onRequestPermissionsResult: ends")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}