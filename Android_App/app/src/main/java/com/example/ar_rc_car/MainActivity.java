package com.example.ar_rc_car;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {
    private ArFragment arFragment;
    private Button btnStopSign;
    private int stopFlag = 0;
    private Button btnStartSign;
    private Button btnMode;
    private AlertDialog.Builder builderSingle;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initial ID
        init();

        // Config buttons
        configBtnMode();
        configBtnStop();
        setPin();

    }
    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable, float MaxScale, float MinScale) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
        transformableNode.getScaleController().setMaxScale(MaxScale);
        transformableNode.getScaleController().setMinScale(MinScale);
        transformableNode.setParent(anchorNode);
        transformableNode.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        transformableNode.select();
    }

    private void init()
    {
        arFragment   = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_ux_fragment);
        btnStartSign = (Button) findViewById(R.id.idBtnStart);
        btnStopSign  = (Button) findViewById(R.id.idBtnStop);
        btnMode      = (Button) findViewById(R.id.idBtnMode);
    }

    private void setPin() {

        arFragment.setOnTapArPlaneListener(((hitResult, plane, motionEvent) -> {
            Anchor anchor = hitResult.createAnchor();
            ModelRenderable.builder()
                    .setSource(this, Uri.parse("Golf_tee.sfb"))
                    .build()
                    .thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable, 3.5f, 3))
                    .exceptionally(throwable -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage(throwable.getMessage())
                                .show();
                        return null;
                    });
        }));
    }

    private void configBtnStop()
    {
        // Set button stop
        btnStopSign.setOnClickListener(view -> {
                arFragment.setOnTapArPlaneListener(((hitResult, plane, motionEvent) -> {
                    if (stopFlag == 0) {
                        Anchor anchor = hitResult.createAnchor();
                        ModelRenderable.builder()
                                .setSource(this, Uri.parse("stopSign.sfb"))
                                .build()
                                .thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable, 0.1f, 0.05f))
                                .exceptionally(throwable -> {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                    builder.setMessage(throwable.getMessage())
                                            .show();
                                    return null;
                                });
                        stopFlag = 1;
                    }
                }));
                Toast.makeText(this, "Set stop pin", Toast.LENGTH_SHORT).show();
        });
    }

    private void configBtnMode()
    {
        // Set button mode
        btnMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builderSingle = new AlertDialog.Builder(MainActivity.this);
                builderSingle.setIcon(R.drawable.icon_connect_car);
                builderSingle.setTitle("Select connection mode");
                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
                arrayAdapter.add("Bluetooth");
                arrayAdapter.add("Local LAN");
                arrayAdapter.add("Internet");

                builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    String strName;
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        strName = arrayAdapter.getItem(which);
                        Toast.makeText(MainActivity.this, strName + " mode is selected", Toast.LENGTH_SHORT).show();
                        if (which == 0) {
                            turnOnBluetooth();
                            // Open Device list to check bluetooth status and connect device
                            if (bluetoothAdapter.isEnabled()) {
                                Intent newIntent = new Intent(MainActivity.this, BluetoothActivity.class);
                                startActivity(newIntent);
                            }

                        }
                    }
                });
                builderSingle.show();
            }
        });
    }

    private void turnOnBluetooth()
    {
        if(bluetoothAdapter == null)
        {
            //Show a mensag. that the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        }
        else if(!bluetoothAdapter.isEnabled())
        {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnBTon,1);
        }
    }
}
