package cmu.team5.MaraudersMap.ViewController;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;

import java.io.InputStream;

import cmu.team5.MaraudersMap.R;

/**
 * Share a POI photo to the Facebook when user reaches the destination
 */
public class ShareFacebookActivity extends Activity {

    private ImageView imgPreview;
    private Bitmap bitmap;
    private ShareButton shareButton;

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;

    // current map link:
    private String curLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_share_facebook);
        AppEventsLogger.activateApp(this);


        // Retrieve current link from Intent:
        curLink = "";
        curLink = getIntent().getStringExtra("curLink");

        shareButton = (ShareButton)findViewById(R.id.fb_share_button);

        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(curLink))
                .build();

        shareButton.setShareContent(content);

        imgPreview = (ImageView) findViewById(R.id.imageView);

        // load the current POI's image
        new DownloadImageTask(imgPreview)
                .execute(curLink);


        //   ShareDialog shareDialog = new ShareDialog(this);
     //   shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);

     /*   btn_takePicture = (ImageButton) findViewById(R.id.imageButton);
        btn_takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // capture picture
                takePictures();
            }
        });*/

    }


    //Take a picture action
    private void takePictures() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


        // start the image capture Intent
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);}


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            bitmap = imageBitmap;
            imgPreview.setImageBitmap(imageBitmap);
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            SharePhotoContent content = new SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .build();

            ShareDialog shareDialog = new ShareDialog(this);
            shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
        }
    }


    /**
     * Responsible for downloading POI images from AWS
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
