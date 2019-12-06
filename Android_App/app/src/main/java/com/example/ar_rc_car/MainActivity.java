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
import android.widget.TextView;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;

public class MainActivity extends AppCompatActivity {
    private ArFragment arFragment;
    private Button btnDeletePin;
    private Button btnStartSign;
    private Button btnMode;
    private TextView tvLocation;
    private TextView tvAngle;
    private TextView tvDirection;
    private AlertDialog.Builder builderSingle;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    final int amountPoint = 10;
    Vector3 pointPosition[] = new Vector3[amountPoint];
    AnchorNode anchorNode[] = new AnchorNode[amountPoint];
    float distanceArray[]   = new float[amountPoint];
    int   angleArray[]      = new int[amountPoint];
    int   direction[]       = new int[amountPoint];
    int   readyNode = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initial
        init();

        // Config buttons
        configBtnMode();

        // Create AR object
        setARObject();

        clickBtnDelete();
        clickBtnStart();
    }


    /* ----------------------------- PRIVATE FUNCTIONS ----------------------------- */
    private void init()
    {
        angleArray[0] = 0;
        direction[0]  = 0;
        arFragment      = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_ux_fragment);
        btnStartSign    = (Button) findViewById(R.id.idBtnStart);
        btnDeletePin    = (Button) findViewById(R.id.idBtnDelete);
        btnMode         = (Button) findViewById(R.id.idBtnMode);
        tvLocation      = (TextView) findViewById(R.id.tv_Location);
        tvAngle         = (TextView) findViewById(R.id.tv_Angle);
        tvDirection     = (TextView) findViewById(R.id.tv_Direction);
    }

    private void setARObject() {
        arFragment.setOnTapArPlaneListener(((hitResult, plane, motionEvent) -> {
            Anchor anchor = hitResult.createAnchor();
            // Set 3D model
            ModelRenderable
                    .builder()
                    .setSource(this, Uri.parse("Golf_tee.sfb"))
                    .build()
                    .thenAccept(modelRenderable -> addModelToScene(anchor, modelRenderable))
                    .exceptionally(throwable -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage(throwable.getMessage()).show();
                        return null;
                    });
            // Set text model
            ViewRenderable
                    .builder()
                    .setView(this, R.layout.point_position)
                    .build()
                    .thenAccept(viewRenderable  -> addTextLocation(anchor, viewRenderable))
                    .exceptionally(throwable -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage(throwable.getMessage()).show();
                        return null;
                    });
        }));
    }

    private void addTextLocation(Anchor anchor, ViewRenderable viewRenderable){
        // AnchorNode to get position
        AnchorNode nodeAnchored = new AnchorNode(anchor);
        // AnchorNode to set position
        AnchorNode tempNode = new AnchorNode();
        tempNode.setRenderable(viewRenderable);
        tempNode.setLocalPosition(new Vector3(nodeAnchored.getLocalPosition().x,(nodeAnchored.getLocalPosition().y + 0.3f),nodeAnchored.getLocalPosition().z));
        arFragment.getArSceneView().getScene().addChild(tempNode);

        // Set text
        TextView tvPointLocation = (TextView) viewRenderable.getView();
        tvPointLocation.setText("x: " + String.format("%.1f", tempNode.getWorldPosition().x) +
                                "\ny: " + String.format("%.1f", tempNode.getWorldPosition().y) +
                                "\nz: " + String.format("%.1f", tempNode.getWorldPosition().z));
    }

    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        anchorNode[readyNode] = new AnchorNode(anchor);
        anchorNode[readyNode].setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode[readyNode]);
        pointPosition[readyNode] = anchorNode[readyNode].getWorldPosition();

        if (readyNode>0) {
            // get distance of point before & current point
            float tempDistance = getDistance(pointPosition[readyNode-1], pointPosition[readyNode]);
            distanceArray[readyNode-1] = tempDistance;
            tvLocation.setText(String.valueOf(tempDistance));
        }
        else {
            tvLocation.setText("0");
        }
        if (readyNode > 1) {
            Vector3 tempVector3_1, tempVector3_2;
            int tempAngle, tempDirection;
            // Create vector of 3 points
            tempVector3_1 = createVector(pointPosition[readyNode-1], pointPosition[readyNode-2]);
            tempVector3_2 = createVector(pointPosition[readyNode], pointPosition[readyNode-1]);
            // calculate angle
            tempAngle = getAngle(tempVector3_1, tempVector3_2);
            angleArray[readyNode-1] = tempAngle;
            tvAngle.setText(String.valueOf(tempAngle));
            // check the current point if it on left or right
            tempDirection = getDirection(tempVector3_1, pointPosition[readyNode]);
            if (tempDirection==1){
                tvDirection.setText("Left");
            }
            else
                tvDirection.setText("Right");
            direction[readyNode-1] = tempDirection;
        }
        else {
            tvAngle.setText("0");
        }
        readyNode++;
    }

    private int getDirection(Vector3 v1, Vector3 v2){
        if (v2.x < v1.x)
        {
            return 1;
        }
        else
            return 0;
    }
    private Vector3 createVector(Vector3 v1, Vector3 v2)
    {
        Vector3 tempVector3;
        tempVector3 = Vector3.subtract(v1, v2);
        return tempVector3;
    }

    private int getAngle(Vector3 v1, Vector3 v2){
        float angle = Vector3.angleBetweenVectors(v1, v2);
        return (int) angle;
    }

    private float getDistance(Vector3 v1, Vector3 v2){
        float distance = Vector3.subtract(v1, v2).length();
        return distance;
    }

    private void clickBtnDelete()
    {
        btnDeletePin.setOnClickListener(view -> {
            if (readyNode > 0) {
                readyNode--;
                distanceArray[readyNode] = 0;
                arFragment.getArSceneView().getScene().onRemoveChild(anchorNode[readyNode]);
            }
            if (readyNode<=1)
            {
                tvLocation.setText("0");
            }
        });
    }

    private void clickBtnStart() {
        btnStartSign.setOnClickListener(view -> {
            for (int i = 0; i < readyNode; i++) {
                pointPosition[i].zero();
                arFragment.getArSceneView().getScene().onRemoveChild(anchorNode[i]);
                distanceArray[i] = 0;
                angleArray[i] = 0;
                direction[i] = 0;
            }
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
