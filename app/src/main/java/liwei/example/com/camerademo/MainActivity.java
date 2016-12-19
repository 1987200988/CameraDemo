package liwei.example.com.camerademo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static android.R.attr.path;

public class MainActivity extends AppCompatActivity {
    private int SYSBITMAP_CODE = 100;
    private int SYSURI_CODE = 101;
    private int MYPICTURE_CODE = 102;
    private int SYSALBUMS_CODE = 103;
    private int MYALBUMS_CODE = 104;
    private ImageView imageView;
    private String imagePath;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.imageView);
        File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        imagePath = externalFilesDir.getAbsolutePath()+"/haha.jpg";



    }
    public void sysBitmap(View view){
        Intent intent =new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,SYSBITMAP_CODE);

    }
    public void sysUri(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(imagePath)));
        startActivityForResult(intent,SYSURI_CODE);


    }
    public void myPicture(View view){


    }

     public void sysAlbums(View view){
         Intent intent = new Intent(Intent.ACTION_PICK,
                 android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
         startActivityForResult(intent,SYSALBUMS_CODE);

    }
    public void myAlbums(View view){

        Intent getAlbum = new Intent("com.android.camera.action.CROP");
        getAlbum.setType("image/*");
        getAlbum.putExtra("aspectX", 1);
        getAlbum.putExtra("aspectY", 1);
        getAlbum.putExtra("outputX", 600);
        getAlbum.putExtra("outputY", 600);
        getAlbum.putExtra("scale", true);
        getAlbum.putExtra("crop", "true");
        getAlbum.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        getAlbum.putExtra("noFaceDetection", false);
        getAlbum.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(imagePath)));
        getAlbum.putExtra("return-data", true);
        startActivityForResult(getAlbum, MYALBUMS_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK) {
            if (requestCode == SYSBITMAP_CODE) {
                Bundle bundle = data.getExtras();
                Bitmap bitmap = (Bitmap) bundle.get("data");
                imageView.setImageBitmap(bitmap);

            } else if (requestCode == SYSURI_CODE) {

                File file = new File(imagePath);
                if (file.exists()) {

                    try {

                        Bitmap bitmap = imageDesc(imagePath);
                        if (bitmap == null) {
                            FileInputStream fis = new FileInputStream(file);
                            bitmap = BitmapFactory.decodeStream(fis);
                            fis.close();
                        }
                        imageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("image", imagePath + "文件丢失！！！");
                }
            }else if(requestCode==SYSALBUMS_CODE){
                Uri selectUri = data.getData();
                String str = selectUri.getPath();


                //多选的情况下使用
                String[] filePathColumns={MediaStore.Images.Media.DATA};
                Cursor c = this.getContentResolver().query(selectUri, filePathColumns, null,null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePathColumns[0]);
                String picturePath= c.getString(columnIndex);

                imageView.setImageURI(selectUri);



            }else if(requestCode==MYALBUMS_CODE){
//                uri会出现不更换图片的bug
//                    imageView.setImageURI(Uri.fromFile(new File(imagePath)));
//                下面这方法可行
//                String path = mFile.getAbsolutePath();
//                imageView.setImageDrawable(Drawable.createFromPath(imagePath));

                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                imageView.setImageBitmap(bitmap);
                // 其次把文件插入到系统图库
                try {
                    MediaStore.Images.Media.insertImage(getContentResolver(), imagePath, "camera", null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                // 最后通知图库更新
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
            }
            }
        }
    private Bitmap decodeUriAsBitmap(Uri uri){
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }

    private Bitmap imageDesc(String imagePath){
        Bitmap bitmap = null;
        int imageW = imageView.getWidth();
        int imageH = imageView.getHeight();

        BitmapFactory.Options options =new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath,options);
        int w = options.outWidth;
        int h = options.outHeight;

        int size = Math.min(w/imageW,h/imageH);
        if(size<1)
            size = 1;
        options.inSampleSize =size;

        options.inJustDecodeBounds=false;
        return BitmapFactory.decodeFile(imagePath,options);
    }


//    private void cropImageUri(Uri uri, int outputX, int outputY, int requestCode){
//
//        Intent intent = new Intent("com.android.camera.action.CROP");
//        intent.setDataAndType(uri, "image/*");
//        intent.putExtra("crop", "true");
//        intent.putExtra("aspectX", 2);
//        intent.putExtra("aspectY", 1);
//        intent.putExtra("outputX", outputX);
//        intent.putExtra("outputY", outputY);
//        intent.putExtra("scale", true);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//        intent.putExtra("return-data", false);
//        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
//        intent.putExtra("noFaceDetection", true); // no face detection
//        startActivityForResult(intent, requestCode);
//
//    }








}
