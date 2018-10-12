package jx.examples.decodelistener;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.datalogic.decode.BarcodeManager;
import com.datalogic.decode.DecodeException;
import com.datalogic.decode.DecodeResult;
import com.datalogic.decode.ReadListener;
import com.datalogic.device.ErrorManager;

public class MainActivity extends AppCompatActivity
        implements ReadListener, View.OnTouchListener {
  private static final String TAG = MainActivity.class.getName();

  BarcodeManager mDecoder;
  ArrayAdapter<String> mAdapter;
  FloatingActionButton mScan;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
    ListView listView = findViewById(R.id.list_view);
    listView.setAdapter(mAdapter);

    mScan = findViewById(R.id.btn_scan);
    mScan.setOnTouchListener(this);
  }

  /** Evento del boton de la app para scanear por medio del sensor del dispositivo datalogic. */
  @Override public boolean onTouch(View v, MotionEvent event) {
    if(event.getAction() == MotionEvent.ACTION_DOWN) {
      try {
        mScan.setPressed(true);
        if (mDecoder != null) mDecoder.startDecode();
      } catch (Exception e) {
        Log.e(TAG, "Action DOWN", e);
        showMessage("ERROR! Check logcat");
      }
    } else if (event.getAction() == MotionEvent.ACTION_UP) {
      try {
        if (mDecoder != null) mDecoder.stopDecode();
        mScan.setPressed(false);
      } catch (Exception e) {
        Log.e(TAG, "Action UP", e);
        showMessage("ERROR! Check logcat");
      }
      v.performClick();
    }
    return true;
  }

  /** Evento del sensor para notificarnos que se escaneo algo desde el dispositivo. */
  @Override public void onRead(DecodeResult decodeResult) {
    // Agregamos el resultado recibido actual a la lista.
    mAdapter.add("text:" + decodeResult.getText() + "id:" + decodeResult.getBarcodeID().name());
  }

  @Override protected void onResume() {
    super.onResume();
    Log.i(TAG, "onResume");

    // Si la instancia del decodificador es nula, se créa.
    if (mDecoder == null) { // Recuerde que una llamada onPause lo pondrá en nulo.
      mDecoder = new BarcodeManager();
    }

    // De aquí en adelante, queremos ser notificados con excepciones en caso de errores.
    ErrorManager.enableExceptions(true);

    try {
      // Recuerda agregarlo, como oyente.
      mDecoder.addReadListener(this);
    } catch (DecodeException e) {
      Log.e(TAG, "Error al intentar vincular un oyente a BarcodeManager", e);
    }
  }

  @Override protected void onPause() {
    super.onPause();
    Log.i(TAG, "onPause");

    // Si tenemos una instancia de BarcodeManager.
    if (mDecoder != null) {
      try {
        // Anular el registro de nuestro oyente y recursos gratuitos.
        mDecoder.removeReadListener(this);
        // Deje que el recolector de basura se encargue de nuestra referencia.
        mDecoder = null;
      } catch (Exception e) {
        Log.e(TAG, "Error al intentar eliminar un agente de escucha de BarcodeManager", e);
      }
    }
  }

  /** Muestra un mensaje de texto. */
  void showMessage(String s) {
    if (mToast == null || mToast.getView().getWindowVisibility() != View.VISIBLE) {
      mToast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
      mToast.show();
    } else {
      mToast.setText(s);
    }
  }
  Toast mToast;
}