package com.dvlab.criminalintent;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class CrimeCameraFragment extends Fragment {

    public static final String TAG = CrimeCameraFragment.class.getSimpleName();
    public static final String EXTRA_PHOTO_FILENAME = "com.bignerdranch.android.criminalintent.photo_filename";

    private Camera camera;
    private SurfaceView surfaceView;
    private View progressContainer;

    public CrimeCameraFragment() {
        // Required empty public constructor
    }

    private Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            // Display the progress indicator
            progressContainer.setVisibility(View.VISIBLE);
        }
    };

    private Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            // Create a filename
            String filename = UUID.randomUUID().toString() + ".jpg";
            // Save the jpeg data to disk
            FileOutputStream os = null;
            boolean success = true;

            try {
                os = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
                os.write(data);
            } catch (Exception e) {
                Log.e(TAG, "Error writing to file " + filename, e);
                success = false;
            } finally {
                try {
                    if (os != null)
                        os.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error closing file " + filename, e);
                    success = false;
                }
            }

            // Set the photo filename on the result intent
            if (success) {
                Log.i(TAG, "JPEG saved at " + filename);

                Intent i = new Intent();
                i.putExtra(EXTRA_PHOTO_FILENAME, filename);
                getActivity().setResult(Activity.RESULT_OK, i);
            } else {
                getActivity().setResult(Activity.RESULT_CANCELED);
            }

            getActivity().finish();
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_camera, container, false);

        progressContainer = view.findViewById(R.id.crime_camera_progress_container);
        progressContainer.setVisibility(View.INVISIBLE);

        Button takePictureButton = (Button) view.findViewById(R.id.crime_camera_take_picture_button);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera != null) {
                    camera.takePicture(shutterCallback, null, jpegCallback);
                }
            }
        });

        surfaceView = (SurfaceView) view.findViewById(R.id.crime_camera_surface_view);
        final SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (camera != null) {
                    try {
                        camera.setPreviewDisplay(surfaceHolder);
                    } catch (IOException e) {
                        Log.e(TAG, "Error setting up preview display", e);
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (camera == null) return;

                Camera.Parameters parameters = camera.getParameters();

                Camera.Size size = getBestSupportedSize(parameters.getSupportedPreviewSizes(), width, height);
                parameters.setPreviewSize(size.width, size.height);

                size = getBestSupportedSize(parameters.getSupportedPictureSizes(), width, height);
                parameters.setPictureSize(size.width, size.height);

                camera.setParameters(parameters);

                try {
                    camera.startPreview();
                } catch (Exception e) {
                    Log.e(TAG, "Could not start preview", e);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (camera != null) {
                    camera.stopPreview();
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        camera = Camera.open(0);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    /**
     * A simple algorithm to get the largest size available. For a more
     * robust version, see CameraPreview.java in the ApiDemos
     * sample app from Android.
     */
    private Camera.Size getBestSupportedSize(List<Camera.Size> sizes, int width, int height) {
        Camera.Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for (Camera.Size s : sizes) {
            int area = s.width * s.height;
            if (area > largestArea) {
                bestSize = s;
                largestArea = area;
            }
        }

        return bestSize;
    }

}
