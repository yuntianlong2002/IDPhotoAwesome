package org.tyun.idphotoawesome.idphotoawesome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.hybridsquad.android.library.BasePhotoCropActivity;
import org.hybridsquad.android.library.CropHelper;
import org.hybridsquad.android.library.CropParams;

import uk.co.senab.photoview.PhotoViewAttacher;


public class MainActivity extends BasePhotoCropActivity {

    public static final String TAG = "TestActivity";

    ImageView mImageView;
    ImageView mImageViewOverlay;
    PhotoViewAttacher mAttacher;

    PhotoSize current_country = new PhotoSize();

    CropParams mCropParams = new CropParams();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageViewOverlay = (ImageView) findViewById(R.id.imageViewOverlay);

    }


    public void onClickCapture(View v) {

        Spinner spinner_type = (Spinner)findViewById(R.id.spinner);
        long visa_ID = spinner_type.getSelectedItemId();

        //Visa Size

        String[] testArray1=getResources().getStringArray(R.array.height_pixel);
        current_country.height_pixel=Integer.parseInt(testArray1[(int) visa_ID]);

        String[] testArray2=getResources().getStringArray(R.array.width_pixel);
        current_country.width_pixel=Integer.parseInt(testArray2[(int) visa_ID]);

        String[] testArray3=getResources().getStringArray(R.array.percentage_height_max_head);
        current_country.percentage_height_max_head=Double.parseDouble(testArray3[(int) visa_ID]);

        String[] testArray4=getResources().getStringArray(R.array.percentage_height_min_head);
        current_country.percentage_height_min_head=Double.parseDouble(testArray4[(int)visa_ID]);

        String[] testArray5=getResources().getStringArray(R.array.h_div_w);
        current_country.h_div_w=Double.parseDouble(testArray5[(int)visa_ID]);

        String[] testArray6=getResources().getStringArray(R.array.size_max_kb);
        current_country.size_max_kb=Integer.parseInt(testArray6[(int)visa_ID]);

        //Set Crop Param
        mCropParams.aspectX=current_country.width_pixel;
        mCropParams.aspectY=current_country.height_pixel;

        mCropParams.outputX=current_country.width_pixel*2;
        mCropParams.outputY=current_country.height_pixel*2;


        Intent intent = CropHelper.buildCaptureIntent(mCropParams.uri);
        startActivityForResult(intent, CropHelper.REQUEST_CAMERA);

        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.dialog_message);

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onSendClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog, null));

        builder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                // go to a new activity of the app

            }
        });
        // set negative button: No message
        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                // cancel the alert box and put a Toast to the user
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "You chose a negative answer",
                        Toast.LENGTH_LONG).show();
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void onGalleryCapture(View v) {
         startActivityForResult(CropHelper.buildCropFromGalleryIntent(mCropParams), CropHelper.REQUEST_CROP);
    }

    @Override
    public CropParams getCropParams() {
        return mCropParams;
    }

    @Override
    public void onPhotoCropped(Uri uri) {
        Log.d(TAG, "Crop Uri in path: " + uri.getPath());
        Toast.makeText(this, "Photo cropped!", Toast.LENGTH_LONG).show();
        Bitmap bt = CropHelper.decodeUriAsBitmap(this, mCropParams.uri);

        //RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mImageView.getLayoutParams();

        //int current_imageview_width=params.width;
        //int ideal_imageview_height=(int)(current_imageview_width*current_country.h_div_w);
        //params.height=ideal_imageview_height;
        //mImageView.setLayoutParams(params);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x-32;//The left right margin has been set to 16 px
        int height = (int)(width*current_country.h_div_w);

        RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        mImageView.setLayoutParams(params);
        mImageView.getLayoutParams().width = width;
        mImageView.getLayoutParams().height = height;

        mImageViewOverlay.setLayoutParams(params);
        mImageViewOverlay.getLayoutParams().width = width;
        mImageViewOverlay.getLayoutParams().height = height;

        double valid_portion = height*current_country.percentage_height_max_head;
        int min_limit = (int)((height-valid_portion)/2);
        int max_limit = min_limit + (int)valid_portion;

        mImageView.setImageBitmap(CropHelper.decodeUriAsBitmap(this, mCropParams.uri));
        // Attach a PhotoViewAttacher, which takes care of all of the zooming functionality.
        mAttacher = new PhotoViewAttacher(mImageView);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLUE);
        //canvas.drawCircle(50, 50, 10, paint);
        canvas.drawLine(0,min_limit,size.x,min_limit,paint);
        canvas.drawLine(0,max_limit,size.x,max_limit,paint);
        //canvas.drawLine(200,200,200,50,paint);
        //canvas.drawLine(200,50,50,50,paint);
        //imageView.setImageBitmap(bitmap);

        //Attach the canvas to the ImageView
        mImageViewOverlay.setImageDrawable(new BitmapDrawable(getResources(), bitmap));



    }

    @Override
    public void onCropCancel() {
        Toast.makeText(this, "Crop canceled!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCropFailed(String message) {
        Toast.makeText(this, "Crop failed:" + message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
