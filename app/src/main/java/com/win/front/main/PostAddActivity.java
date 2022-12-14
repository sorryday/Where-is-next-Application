package com.win.front.main;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.win.front.R;
import com.win.front.dto.PostDTO;
import com.win.front.retorfit.RetrofitAPI;
import com.win.front.retorfit.RetrofitClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostAddActivity extends AppCompatActivity {
    Button post_add_picture_btn;
    Button post_add_complete_btn;
    Button post_add_cancle_btn;

    EditText post_title;
    EditText post_contents;

    Uri uri;
    ArrayList<Bitmap> PostAddImage;

    private String userId;
    private SharedPreferences sp;

    private RetrofitClient retrofitClient;      // retrofit2 ?????? ?????? ??????
    private RetrofitAPI retrofitAPI;            // retrofit2 api ?????? ?????? ??????

    ProgressDialog dialog;  // ??????????????? ???????????????

    ImageView post_add_back_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.post_fragment_add_post_activity);

        sp = getSharedPreferences("UserInfo", MODE_PRIVATE);
        userId = sp.getString("userId", "");

        PostAddImage = new ArrayList<>();

        dialog = new ProgressDialog(this);

        // ?????? ????????????
        post_add_back_view = findViewById(R.id.post_add_back_view);
        post_add_back_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // ??????
        post_title = findViewById(R.id.post_add_title);
        post_title.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == event.KEYCODE_ENTER)
                    return true;
                return false;
            }
        });

        // ??????
        post_contents = findViewById(R.id.post_add_contents);
        post_contents.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == event.KEYCODE_ENTER)
                    return true;
                return false;
            }
        });

        // ?????? ??????
        post_add_picture_btn = findViewById(R.id.post_add_picture_btn);
        post_add_picture_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityResult.launch(intent);
            }
        });

        // ????????????
        post_add_cancle_btn = findViewById(R.id.post_add_cancle_btn);
        post_add_cancle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // ?????? ??????
        post_add_complete_btn = findViewById(R.id.post_add_complete_btn);
        post_add_complete_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                String input_title = post_title.getText().toString();
                String input_contents = post_contents.getText().toString();

                if (input_title.equals("")) {
                    custom_dialog_one_text_check("????????? ??????????????????.");
                } else if (input_contents.equals("")) {
                    custom_dialog_one_text_check("????????? ??????????????????.");
                } else {
                    // ?????? ?????? ?????????
                    LocalDate now = LocalDate.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
                    String formatedNow = now.format(formatter);


                    AddPost(input_title, input_contents, formatedNow);
                }
            }
        });
    }

    // ??????????????? ????????? ???????????? ?????? ??? View Control
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if( result.getResultCode() == RESULT_OK && result.getData() != null){

                        uri = result.getData().getData();

                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                            LinearLayout picture_layout = (LinearLayout) findViewById(R.id.picture_layout);
                            ImageView imageView = new ImageView(picture_layout.getContext());
                            imageView.setImageBitmap(bitmap);

                            CardView cardView = new CardView(picture_layout.getContext());
                            cardView.addView(imageView);
                            cardView.setRadius(10);

                            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            param.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
                            param.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
                            param.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
                            param.gravity = Gravity.CENTER;

                            PostAddImage.add(bitmap);

                            picture_layout.addView(cardView,param);
                            cardView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    BitmapDrawable dr = (BitmapDrawable) imageView.getDrawable();
                                    Bitmap tempBitmap = dr.getBitmap();

                                    PostAddImage.remove(tempBitmap);
                                    picture_layout.removeView(cardView);
                                }
                            });

                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            });

    // ????????? ??????
    private void AddPost(String input_title, String input_contents, String formatedNow) {
        retrofitClient = RetrofitClient.getInstance();
        retrofitAPI = RetrofitClient.getRetrofitInterface();

        ArrayList<byte[]> input_image = new ArrayList<>();

        if (!PostAddImage.isEmpty()) {
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  // ??????????????? ????????? ??????
            dialog.setCancelable(false);    // ?????? ??? ???????????? ???????????? ??????
            dialog.setMessage("Loading ...");   // ?????????
            dialog.show();  // ??????????????? ??????

            for (int i = 0; i < PostAddImage.size(); i++) {
                Bitmap resizeBitmap = resize(PostAddImage.get(i)); //????????? ??????
                byte[] image = bitmapToByteArray(resizeBitmap);

                input_image.add(image);
            }
        }

        retrofitAPI.getNickNameResponse(userId).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                String nickname = response.body().replaceAll("\"", "");

                PostDTO postAddDTO = new PostDTO(userId, nickname, formatedNow, input_title, input_contents, input_image);

                // ???????????? ??????
                retrofitAPI.addPostMethod(postAddDTO).enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if (response.body()) {
                            dialog.dismiss();
                            custom_dialog_one_text_finish("?????? ?????????????????????.");
                        }
                    }
                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {
                    }
                });
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
            }
        });
    }

    //???????????? ???????????? ?????????????????? ??????????????? ?????????
    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    // ????????? ????????? ??????
    private Bitmap resize(Bitmap bm){
        bm = Bitmap.createScaledBitmap(bm, 1050, 800, true);

        return bm;
    }

    // ????????? ??????????????? : one_text, ?????? ?????????
    public void custom_dialog_one_text_finish(String message) {
        LayoutInflater inflater= getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_alert_dialog_one_text, null);
        ((TextView)view.findViewById(R.id.first_text)).setText(message);

        AlertDialog alert = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

        view.findViewById(R.id.alert_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
                finish();
            }
        });

        alert.show();

        WindowManager.LayoutParams params = alert.getWindow().getAttributes();
        params.width = 900;
        alert.getWindow().setAttributes(params);
    }

    // ????????? ??????????????? : one_text, ?????? ?????????
    public void custom_dialog_one_text_check(String message) {
        LayoutInflater inflater= getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_alert_dialog_one_text, null);
        ((TextView)view.findViewById(R.id.first_text)).setText(message);

        AlertDialog alert = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

        view.findViewById(R.id.alert_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();
            }
        });

        alert.show();

        WindowManager.LayoutParams params = alert.getWindow().getAttributes();
        params.width = 900;
        alert.getWindow().setAttributes(params);
    }
}

