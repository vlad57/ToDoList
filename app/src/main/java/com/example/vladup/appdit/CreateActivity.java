package com.example.vladup.appdit;

import android.app.DatePickerDialog;
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
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateActivity extends AppCompatActivity {

    private int defaultColorR;
    private int defaultColorG;
    private int defaultColorB;
    static String retourColor;
    private int newPos;
    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener date;
    Button calendrierCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        final EditText titleCreate = (EditText)findViewById(R.id.titleCreate);
        final EditText contentCreate = (EditText)findViewById(R.id.contentCreate);
        calendrierCreate = (Button) findViewById(R.id.calendrierCreate);
        myCalendar = Calendar.getInstance();
        final Button colorCreate = (Button) findViewById(R.id.colorCreate);
        Button createButton = (Button) findViewById(R.id.createButton);
        newPos = getIntent().getExtras().getInt("NEWPOS");

        defaultColorR = 255;
        defaultColorG = 255;
        defaultColorB = 255;
        retourColor = "#ffffff";
        final ColorPicker cp = new ColorPicker(CreateActivity.this, defaultColorR, defaultColorG, defaultColorB);

        colorCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cp.show();
                cp.enableAutoClose();
            }
        });

        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        calendrierCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(CreateActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        cp.setCallback(new ColorPickerCallback() {
            @Override
            public void onColorChosen(@ColorInt int color) {
                retourColor = String.format("#%06X", (0xFFFFFF & color));
                colorCreate.setBackgroundColor(Color.parseColor(retourColor));
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkFields(titleCreate.getText().toString(), contentCreate.getText().toString(), calendrierCreate.getText().toString())){
                    MyApolloClient.getMyApolloClient().mutate(
                            CreatePostTMutation.builder()
                                    .title(titleCreate.getText().toString())
                                    .content(contentCreate.getText().toString())
                                    .date(calendrierCreate.getText().toString())
                                    .color(retourColor)
                                    .favoris(false)
                                    .position(newPos).build())
                            .enqueue(new ApolloCall.Callback<CreatePostTMutation.Data>() {
                                @Override
                                public void onResponse(@NotNull Response<CreatePostTMutation.Data> response) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);

                                    intent.putExtra(Constantes.ID_KEY, response.data().createDraft().id);
                                    intent.putExtra(Constantes.TITLE_KEY, titleCreate.getText().toString());
                                    intent.putExtra(Constantes.CONTENT_KEY, contentCreate.getText().toString());
                                    intent.putExtra(Constantes.DATE_KEY, calendrierCreate.getText().toString());
                                    intent.putExtra(Constantes.COLOR_KEY, retourColor);
                                    intent.putExtra("NEWPOSRETOUR", newPos);
                                    Log.e("SAMER", "POSITION : " + response.data().createDraft().id);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }

                                @Override
                                public void onFailure(@NotNull ApolloException e) {

                                }
                            });
                }
            }
        });
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.FRANCE);

        calendrierCreate.setText(sdf.format(myCalendar.getTime()));
    }

    private Boolean checkFields(String title, String contenu, String date){
        if (title.isEmpty()){
            Toast.makeText(CreateActivity.this, "Complete the title.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (contenu.isEmpty()){
            Toast.makeText(CreateActivity.this, "Choose the content.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (date.isEmpty()){
            Toast.makeText(CreateActivity.this, "Choose the date.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}
