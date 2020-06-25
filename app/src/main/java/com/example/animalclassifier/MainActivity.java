package com.example.animalclassifier;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;



import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final Regions clientRegion = Regions.US_EAST_1;
    private static final String INPUT_BUCKET = "animalclassifier-input";
    private static AmazonS3 s3Client;
    TransferUtility transferUtility;
    private boolean taken=false;
    public CameraBridgeViewBase cameraBridgeViewBase;
    public Mat mat1;
    public static TextView classificationText;
    public String image = "";
    BaseLoaderCallback baseLoaderCallback;
    public Random random = new Random();





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        classificationText = (TextView) findViewById(R.id.result);
        s3credentialsProvider();
        setTransferUtility();
        TransferNetworkLossHandler.getInstance(getApplicationContext());
        cameraBridgeViewBase = (JavaCameraView) findViewById(R.id.myCameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                switch (status) {
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };

    }

    public void s3credentialsProvider(){

        // Initialize the AWS Credential
        CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider =
                new CognitoCachingCredentialsProvider(
                        getApplicationContext(),
                        "us-east-1:3a734336-b2fb-4da8-b912-3ec95bffbe80", // Identity Pool ID
                        clientRegion // Region
                );
        createAmazonS3Client(cognitoCachingCredentialsProvider);
    }

    public void setTransferUtility(){

       transferUtility = TransferUtility.builder().s3Client(s3Client).context(getApplicationContext()).build();
    }

    public void createAmazonS3Client(CognitoCachingCredentialsProvider
                                             credentialsProvider){


        s3Client = new AmazonS3Client(credentialsProvider,Region.getRegion(clientRegion));

    }
    public void send(View view) {
        ////////////Amazon Bucket Send & wait//////////////////////////
        // Upload a file as a new object with ContentType and title specified.
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png", mat1, matOfByte);

        byte[] byteArray = matOfByte.toArray();
        InputStream in = new ByteArrayInputStream(byteArray);
        Bitmap bitmap = BitmapFactory.decodeStream(in);

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath=new File(directory,"image.png");
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            File f=new File(directory, "image.png");
            int i = random.nextInt(10000);
            TransferObserver transferObserver = transferUtility.upload(INPUT_BUCKET,"img"+i+".png",f);
            transferObserverListener(transferObserver);

            image = "img"+i+".png";
            //TextView classificationText = (TextView) findViewById(R.id.result);
            //classificationText.setText(classificationResponse);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void transferObserverListener(TransferObserver transferObserver){

        transferObserver.setTransferListener(new TransferListener(){

            @Override
            public void onStateChanged(int id, TransferState state) {
                Toast.makeText(getApplicationContext(), "State Change "
                        + state, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent/bytesTotal * 100);
                Toast.makeText(getApplicationContext(), "Progress in %"
                        + percentage, Toast.LENGTH_SHORT).show();
                if(percentage>=100) {
                    ModelInvoker invoker = new ModelInvoker();
                    invoker.setBody("{\"name\":\"" +image+"\"}");
                    invoker.execute();
                    taken = false;
                }
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("error","error");
                ex.printStackTrace();
                Toast.makeText(getApplicationContext(),"Error", Toast.LENGTH_SHORT).show();
            }

        });
    }

    public void stop(View view) {
        taken=!taken;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mat1 = new Mat(width,height, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
    mat1.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase!=null)
            cameraBridgeViewBase.disableView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()) {
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
            Toast.makeText(this, "Ready to Go", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraBridgeViewBase!=null)
            cameraBridgeViewBase.disableView();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if(!taken)
        mat1 = inputFrame.rgba();

        return mat1;
    }


}
