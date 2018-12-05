package com.example.vladup.appdit;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
    Button timeCreate;
    Switch notifCreate;
    int isNotif;
    public DBHandler myDB;
    public String newIDNotif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        final EditText titleCreate = (EditText)findViewById(R.id.titleCreate);
        final EditText contentCreate = (EditText)findViewById(R.id.contentCreate);
        calendrierCreate = (Button) findViewById(R.id.calendrierCreate);
        timeCreate = (Button) findViewById(R.id.timeCreate);
        notifCreate = (Switch) findViewById(R.id.notifCreate);
        isNotif= 0;
        myCalendar = Calendar.getInstance();
        final Button colorCreate = (Button) findViewById(R.id.colorCreate);
        Button createButton = (Button) findViewById(R.id.createButton);
        newPos = getIntent().getExtras().getInt("NEWPOS");
        myDB = new DBHandler(this);
        newIDNotif = String.valueOf(myDB.getMaxIdNotif() + 1);

        Log.e("IDNOTIF", "IDNOTIF : " + myDB.getMaxIdNotif());

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

        timeCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(CreateActivity.this, onStartTimeListener, myCalendar
                        .get(Calendar.HOUR), myCalendar.get(Calendar.MINUTE), true).show();
            }
        });

        notifCreate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    isNotif = 1;
                }
                else {
                    isNotif = 0;
                }
            }
        });

        cp.setCallback(new ColorPickerCallback() {
            @Override
            public void onColorChosen(@ColorInt int color) {
                retourColor = String.format("#%06X", (0xFFFFFF & color));
                colorCreate.setBackgroundColor(Color.parseColor(retourColor));
            }
        });

        createNotificationChannel();
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkFields(titleCreate.getText().toString(), contentCreate.getText().toString(), calendrierCreate.getText().toString(), timeCreate.getText().toString())){
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
                                    //Back to the MainActivity
                                    intent.putExtra(Constantes.ID_KEY, response.data().createDraft().id);
                                    intent.putExtra(Constantes.TITLE_KEY, titleCreate.getText().toString());
                                    intent.putExtra(Constantes.CONTENT_KEY, contentCreate.getText().toString());
                                    intent.putExtra(Constantes.DATE_KEY, calendrierCreate.getText().toString());
                                    intent.putExtra(Constantes.COLOR_KEY, retourColor);
                                    intent.putExtra("NEWPOSRETOUR", newPos);
                                    Log.e("SAMER", "POSITION : " + response.data().createDraft().id);

                                    //CreateNotification
                                    if (isNotif == 1) {
                                        SimpleDateFormat spf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                                        Date currentTime = Calendar.getInstance().getTime();
                                        try {
                                            Date mDate = spf.parse(convertDate(calendrierCreate.getText().toString() + " " + timeCreate.getText().toString().trim()));
                                            long diff = mDate.getTime() - currentTime.getTime();
                                            scheduleNotification(CreateActivity.this, diff, Integer.valueOf(newIDNotif), titleCreate.getText().toString(), contentCreate.getText().toString());
                                            myDB.addNotif(calendrierCreate.getText().toString(), timeCreate.getText().toString().trim(), newIDNotif, String.valueOf(isNotif), response.data().createDraft().id);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    //finish
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

    TimePickerDialog.OnTimeSetListener onStartTimeListener = new TimePickerDialog.OnTimeSetListener() {

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            timeCreate.setText(hourOfDay + ":" + minute);
            myCalendar.set(Calendar.HOUR, hourOfDay);
            myCalendar.set(Calendar.MINUTE, minute);
        }
    };

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.FRANCE);
        calendrierCreate.setText(sdf.format(myCalendar.getTime()));
    }

    private String convertDate(String date) throws ParseException {
        SimpleDateFormat spfOld = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
        Date newDate = spfOld.parse(date);
        spfOld = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
        date = spfOld.format(newDate);
        return date;
    }


    private Boolean checkFields(String title, String contenu, String date, String time){
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
        if (time.isEmpty()){
            Toast.makeText(CreateActivity.this, "Choose the time.", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Creation";
            String description = "Channel de création";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("0", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void scheduleNotification(Context context, long delay, int notificationId, String title, String content) {//delay is after how much time(in millis) from current time you want to schedule the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "0")
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.fdp)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent activity = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(activity);

        Notification notification = builder.build();

        Intent notificationIntent = new Intent(context, NotifyHandlerReceiver.class);
        notificationIntent.putExtra(NotifyHandlerReceiver.NOTIFICATION_ID, notificationId);
        notificationIntent.putExtra(NotifyHandlerReceiver.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }
}
