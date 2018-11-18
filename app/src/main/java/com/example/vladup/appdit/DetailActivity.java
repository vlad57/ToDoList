package com.example.vladup.appdit;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private String id_element;
    private int position;
    private boolean isCheckedD;
    static String getColorDetail;
    Button calendrierDetail;
    private DatePicker datePicker;
    private Calendar calendar;
    private int defaultColorR;
    private int defaultColorG;
    private int defaultColorB;
    static final int DATE_DIALOG_ID = 999;

    private int myear;
    private int mmonth;
    private int mday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        final EditText titleDetail = (EditText)findViewById(R.id.titleDetail);
        final EditText contentDetail = (EditText)findViewById(R.id.contentDetail);
        calendrierDetail = (Button) findViewById(R.id.calendrierDetail);
        final Button colorDetail = (Button) findViewById(R.id.colorDetail);
        Button editButton = (Button) findViewById(R.id.editButton);

        position = getIntent().getExtras().getInt("MAPOSITION");
        id_element = getIntent().getStringExtra(Constantes.ID_KEY);
        titleDetail.setText(getIntent().getStringExtra(Constantes.TITLE_KEY));
        contentDetail.setText(getIntent().getStringExtra(Constantes.CONTENT_KEY));
        calendrierDetail.setText(getIntent().getStringExtra(Constantes.DATE_KEY));
        getColorDetail = getIntent().getStringExtra(Constantes.COLOR_KEY);
        isCheckedD = getIntent().getExtras().getBoolean(Constantes.FAVORIS_KEY);

        defaultColorR = (Color.parseColor(getColorDetail) >> 16) & 0xFF;
        defaultColorG = (Color.parseColor(getColorDetail) >> 8) & 0xFF;
        defaultColorB = (Color.parseColor(getColorDetail) >> 0) & 0xFF;
        colorDetail.setBackgroundColor(Color.parseColor(getColorDetail));
        final ColorPicker cp = new ColorPicker(DetailActivity.this, defaultColorR, defaultColorG, defaultColorB);

        colorDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cp.show();
                cp.enableAutoClose();
            }
        });

        cp.setCallback(new ColorPickerCallback() {
            @Override
            public void onColorChosen(@ColorInt int color) {
                // Do whatever you want
                // Examples
                Log.d("Alpha", Integer.toString(Color.alpha(color)));
                Log.d("Red", Integer.toString(Color.red(color)));
                Log.d("Green", Integer.toString(Color.green(color)));
                Log.d("Blue", Integer.toString(Color.blue(color)));

                Log.d("Pure Hex", Integer.toHexString(color));
                Log.d("#Hex no alpha", String.format("#%06X", (0xFFFFFF & color)));
                Log.d("#Hex with alpha", String.format("#%08X", (0xFFFFFFFF & color)));

                getColorDetail = String.format("#%06X", (0xFFFFFF & color));
                colorDetail.setBackgroundColor(Color.parseColor(getColorDetail));
                // If the auto-dismiss option is not enable (disabled as default) you have to manually dimiss the dialog
                // cp.dismiss();
            }
        });

        calendrierDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(calendrierDetail.getText().toString());
            }
        });



        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApolloClient.getMyApolloClient().mutate(
                        UpdatePostTMutation.builder()
                        .id(id_element)
                        .title(titleDetail.getText().toString())
                        .content(contentDetail.getText().toString())
                        .date(calendrierDetail.getText().toString())
                        .color(getColorDetail).build())
                        .enqueue(new ApolloCall.Callback<UpdatePostTMutation.Data>() {
                            @Override
                            public void onResponse(@NotNull Response<UpdatePostTMutation.Data> response) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.putExtra("MAPOSITIONRETOUR", position);
                                intent.putExtra(Constantes.ID_KEY, id_element);
                                intent.putExtra(Constantes.TITLE_KEY, titleDetail.getText().toString());
                                intent.putExtra(Constantes.CONTENT_KEY, contentDetail.getText().toString());
                                intent.putExtra(Constantes.DATE_KEY, calendrierDetail.getText().toString());
                                intent.putExtra(Constantes.COLOR_KEY, getColorDetail);
                                intent.putExtra(Constantes.FAVORIS_KEY, isCheckedD);
                                //Log.e("SAMER", "POSITION : "+position);
                                setResult(RESULT_OK, intent);
                                finish();
                            }

                            @Override
                            public void onFailure(@NotNull ApolloException e) {

                            }
                        });
            }
        });
    }

    private void showDatePickerDialog(String date) {
        String[] split = date.split("/");
        int day = Integer.valueOf(split[0]);
        final int month = Integer.valueOf(split[1]);
        int year = Integer.valueOf(split[2]);
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                showDate(dayOfMonth, monthOfYear, year);
            }
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                dateSetListener, year, month, day);
        datePickerDialog.show();
    }

    private void showDate(int year, int month, int day) {
        calendrierDetail.setText(new StringBuilder().append(year).append("/")
                .append(month).append("/").append(day));
    }
}
