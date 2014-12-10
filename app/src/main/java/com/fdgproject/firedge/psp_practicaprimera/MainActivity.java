package com.fdgproject.firedge.psp_practicaprimera;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


public class MainActivity extends Activity {

    //ProgressBar
    private ProgressBar pb;

    //Ruta de archivos
    private String ruta;

    //Componentes
    private RadioButton rb_public, rb_private;
    private TextView tv;
    private EditText etUrl, etNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pb = (ProgressBar) findViewById(R.id.pb_cargar);
        etUrl = (EditText)findViewById(R.id.et_url);
        etNombre = (EditText)findViewById(R.id.et_nombre);
        rb_public = (RadioButton)findViewById(R.id.rb_public);
        rb_private = (RadioButton) findViewById(R.id.rb_private);
        tv = (TextView)findViewById(R.id.tv_resultado);
    }

    private class MyThread extends AsyncTask<String, Integer, Long> {

        protected Long doInBackground(String... s) {
            long totalSize = 0;
            //totalSize = Downloader.downloadFile(urls[0]);
            try {
                URL url = new URL(s[0]);
                URLConnection urlCon = url.openConnection();
                pb.setProgress(0);
                pb.setMax(urlCon.getContentLength());
                InputStream is = urlCon.getInputStream();
                FileOutputStream fos = new FileOutputStream(ruta);
                byte[] array = new byte[1024];
                int leido = is.read(array);
                totalSize = leido;
                while (leido > 0) {
                    publishProgress((int) totalSize);
                    fos.write(array, 0, leido);
                    totalSize += leido;
                    leido = is.read(array);
                }
                is.close();
                fos.close();

                return totalSize;
            }catch(Exception ex){
                return totalSize;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            pb.setProgress(values[values.length-1]);
        }

        protected void onPostExecute(Long result) {
            pb.setVisibility(View.INVISIBLE);
            if(result > 0) {
                File imgFile = new  File(ruta);
                if(imgFile.exists())
                {
                    ImageView myImage = (ImageView)findViewById(R.id.iv_foto);
                    myImage.setImageURI(Uri.fromFile(imgFile));
                }
                tv.setText("Downloaded " + result + " bytes");
            } else {
                tv.setText(getString(R.string.msg_error));
            }
        }
    }

    private String construirNombre(String url){
        String s = etNombre.getText().toString();
        if(s.isEmpty()){
            String [] trozos = url.split("/");
            s = trozos[trozos.length-1];
        }else{
            s += "."+url.substring(url.length()-3);
        }
        return s;
    }

    private void construirRuta(String nombre){
        if (rb_public.isChecked()) {
            ruta = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/" + nombre;
        } else if (rb_private.isChecked()) {
            ruta = getExternalFilesDir(Environment.DIRECTORY_DCIM).toString() + "/" + nombre;
        }
    }

    public void bt_down(View v){
        tv.setText("");
        pb.setVisibility(View.VISIBLE);
        String url = etUrl.getText().toString();
        String extension = url.substring(url.length()-3);
        if(!url.isEmpty() && (extension.equals("jpg") || extension.equals("png") || extension.equals("gif") )) {
            String nombre = construirNombre(url);
            construirRuta(nombre);
            new MyThread().execute(url);
        } else {
            tv.setText(getString(R.string.msg_error));
            pb.setVisibility(View.INVISIBLE);
        }
    }
}
