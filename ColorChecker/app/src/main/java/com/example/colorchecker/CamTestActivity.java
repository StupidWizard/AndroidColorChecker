package com.example.colorchecker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CamTestActivity extends Activity {
    private static final String TAG = "CamTestActivity";

    private static final int CAMERA_REQUEST = 1888;
    private ImageViewTouch imageView;

    Preview preview;
    Button buttonClick;
    Camera camera;
    Activity act;
    Context ctx;
    boolean onUseCamera = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        act = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        preview = new Preview(this, (SurfaceView)findViewById(R.id.surfaceView));
        preview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        ((FrameLayout) findViewById(R.id.layout)).addView(preview);
        preview.setKeepScreenOn(true);

        imageView = (ImageViewTouch) findViewById(R.id.image_view);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int[] viewCoords = new int[2];
                imageView.getLocationOnScreen(viewCoords);

                int touchX = (int) motionEvent.getX();
                int touchY = (int) motionEvent.getY();

                int imageX = touchX - viewCoords[0]; // viewCoords[0] is the X coordinate
                int imageY = touchY - viewCoords[1];

                Log.e(TAG, "Touch at X = " + imageX + " Y = " + imageY);

                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                if (bitmap != null) {
                    int pixel = bitmap.getPixel(imageX, imageY);

                    //then do what you want with the pixel data, e.g
                    int redValue = Color.red(pixel);
                    int blueValue = Color.blue(pixel);
                    int greenValue = Color.green(pixel);

                    Log.e(TAG, "Color R = " + redValue + "  G = " + greenValue + "  B = " + blueValue);
                }

                return false;
            }
        });

//        preview.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                camera.takePicture(shutterCallback, rawCallback, jpegCallback);
//            }
//        });

//        Toast.makeText(ctx, getString(R.string.take_photo_help), Toast.LENGTH_LONG).show();

        		buttonClick = (Button) findViewById(R.id.btnCapture);

        		buttonClick.setOnClickListener(new OnClickListener() {
        			public void onClick(View v) {
        //				preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);

                        if (onUseCamera) {
                            camera.takePicture(shutterCallback, rawCallback, jpegCallback);
                        } else {
                            imageView.setVisibility(View.INVISIBLE);
                        }
                    }
        		});
        //
        //		buttonClick.setOnLongClickListener(new OnLongClickListener(){
        //			@Override
        //			public boolean onLongClick(View arg0) {
        //				camera.autoFocus(new AutoFocusCallback(){
        //					@Override
        //					public void onAutoFocus(boolean arg0, Camera arg1) {
        //						//camera.takePicture(shutterCallback, rawCallback, jpegCallback);
        //					}
        //				});
        //				return true;
        //			}
        //		});
    }

    @Override
    protected void onResume() {
        super.onResume();
        int numCams = Camera.getNumberOfCameras();
        if(numCams > 0){
            try{
                if (Camera.getNumberOfCameras() > Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                } else {
                    camera = Camera.open(0);
                }

                camera.startPreview();
                preview.setCamera(camera);
            } catch (RuntimeException ex){
                Toast.makeText(ctx, getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        if(camera != null) {
            camera.stopPreview();
            preview.setCamera(null);
            camera.release();
            camera = null;
        }
        super.onPause();
    }

    private void resetCam() {
        camera.startPreview();
        preview.setCamera(camera);
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            //			 Log.d(TAG, "onShutter'd");
        }
    };

    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //			 Log.d(TAG, "onPictureTaken - raw");
        }
    };

    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            new SaveImageTask().execute(data);
            resetCam();
            showToImageView();
            if (onUseCamera) {

            }
            Log.d(TAG, "onPictureTaken - jpeg");
        }
    };

    private String fileName = "";
    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;

            // Write to SD Card
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File(sdCard.getAbsolutePath() + "/camtest");
                dir.mkdirs();

//                fileName = String.format("%d.jpg", System.currentTimeMillis());
                fileName = String.format("temp.jpg", System.currentTimeMillis());
                File outFile = new File(dir, fileName);

                outStream = new FileOutputStream(outFile);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());

                refreshGallery(outFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return null;
        }

    }

    private void showToImageView() {
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/camtest");
        dir.mkdirs();
        File imgFile = new File(dir, fileName);

        if(imgFile.exists()){

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            Bitmap rotatedBitmap;
            try {
                ExifInterface exif = new ExifInterface(imgFile.getPath());
                int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int rotationInDegrees = exifToDegrees(rotation);
                Matrix matrix = new Matrix();
                if (rotation != 0f) {matrix.preRotate(rotationInDegrees);}
                rotatedBitmap = Bitmap.createBitmap(myBitmap,0,0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);

                imageView.setImageBitmap(rotatedBitmap);
                onUseCamera = false;
            }catch(IOException ex){
                Log.e(TAG, "Failed to get Exif data", ex);
            }

        }
    }

    /**
     * Gets the Amount of Degress of rotation using the exif integer to determine how much
     * we should rotate the image.
     * @param exifOrientation - the Exif data for Image Orientation
     * @return - how much to rotate in degress
     */
    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 0; }
        return 270;
    }
}
