package org.huzaifa.photobackup;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int ID_SELECTED_GALLERY_INTENT_IMAGE = 1;
    ImageView uploadedImage, downloadedImage;
    Button bUpload, bDownload;
    EditText uploadedImageName, downloadedImageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadedImage = (ImageView) findViewById(R.id.uploaded_Image);
        downloadedImage = (ImageView) findViewById(R.id.downloaded_image);
        uploadedImage.setImageResource(R.drawable.default_upload_image);

        bDownload = (Button) findViewById(R.id.button_download);
        bUpload = (Button) findViewById(R.id.button_upload);

        uploadedImageName = (EditText) findViewById(R.id.name_of_uploaded_image);
        downloadedImageName = (EditText) findViewById(R.id.name_of_downoaded_image);
        uploadedImageName.setHint("Enter Photo Name");
        downloadedImageName.setHint("Enter the name of the Photo to Download");
        uploadedImage.setOnClickListener(this);
        bUpload.setOnClickListener(this);
        bDownload.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.uploaded_Image:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent,ID_SELECTED_GALLERY_INTENT_IMAGE);
                break;
            case R.id.button_upload:
                Bitmap image = ((BitmapDrawable) uploadedImage.getDrawable()).getBitmap();
                hideSoftKeyboard(this);
                UploadTheImage uploadTheImage = new UploadTheImage(image,uploadedImageName.getText().toString());
                uploadTheImage.execute();
                break;
            case R.id.button_download:
                DownloadTheImage downloadTheImage = new DownloadTheImage(downloadedImageName.getText().toString());
                downloadTheImage.execute();
                Toast.makeText(getApplicationContext(),getString(R.string.SERVER_ADDRESS) + "Uploaded Images/" + downloadedImageName.getText().toString() + ".jpg",Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ID_SELECTED_GALLERY_INTENT_IMAGE){
            if (resultCode == RESULT_OK && data!=null){
                Uri selectedImage = data.getData();
                uploadedImage.setImageURI(selectedImage);
            }
        }
    }

    public class UploadTheImage extends AsyncTask<Void,Void,Void>{

        Bitmap bitmap;
        String string;

        public UploadTheImage(Bitmap image,String name){
            this.bitmap = image;
            this.string = name;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(),getString(R.string.toast_text),Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("image",encodedImage));
            dataToSend.add(new BasicNameValuePair("name",string));

            /*ContentValues dataToSend = new ContentValues();
            dataToSend.put("image", encodedImage);
            dataToSend.put("name", string);*/

            HttpParams httpRequestParams = getHttpRequestParams();
            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(getString(R.string.SERVER_ADDRESS) + "SavePicture.php");

            try {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                client.execute(post);

            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }
    }

    public class DownloadTheImage extends AsyncTask<Void,Void,Bitmap>{

        String string;
        String url;

        public DownloadTheImage(String name){

            this.string = name;
            this.url = getString(R.string.SERVER_ADDRESS) + "Uploaded Images/" + string + ".jpg";
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                URLConnection urlConnection = new URL(url).openConnection();
                urlConnection.setConnectTimeout(1000*30);
                urlConnection.setReadTimeout(1000*30);

                //return BitmapFactory.decodeStream((InputStream) urlConnection.getContent(),null,null);
                return BitmapFactory.decodeStream((InputStream) urlConnection.getContent());

            }catch (Exception ex){
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap!=null){
                downloadedImage.setImageBitmap(bitmap);
            }else{
                Toast.makeText(getApplicationContext(),"ERROR! Ensure the name of the photo is correct.",Toast.LENGTH_LONG).show();
            }
        }
    }

    private HttpParams getHttpRequestParams(){
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams,1000*30);
        HttpConnectionParams.setSoTimeout(httpParams,100*30);
        return httpParams;
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

}
