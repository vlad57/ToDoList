package com.example.vladup.appdit;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class DetailActivity extends AppCompatActivity {

    private String id_element;
    private int position;
    private boolean isCheckedD;
    static String getColorDetail;
    Button calendrierDetail;
    Button timeDetail;
    Switch notifDetail;
    private int defaultColorR;
    private int defaultColorG;
    private int defaultColorB;
    DBHandler myDB;
    List<ModelNotification> ListModelNotif;
    int isNotif;
    String IdNotif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        final EditText titleDetail = (EditText)findViewById(R.id.titleDetail);
        final EditText contentDetail = (EditText)findViewById(R.id.contentDetail);
        calendrierDetail = (Button) findViewById(R.id.calendrierDetail);
        timeDetail = (Button) findViewById(R.id.timeDetail);
        notifDetail = (Switch) findViewById(R.id.notifDetail);
        final Button colorDetail = (Button) findViewById(R.id.colorDetail);
        Button editButton = (Button) findViewById(R.id.editButton);
        Button cancelButton = (Button) findViewById(R.id.cancelDetail);
        myDB = new DBHandler(this);

        position = getIntent().getExtras().getInt("MAPOSITION");
        id_element = getIntent().getStringExtra(Constantes.ID_KEY);
        titleDetail.setText(getIntent().getStringExtra(Constantes.TITLE_KEY));
        contentDetail.setText(getIntent().getStringExtra(Constantes.CONTENT_KEY));
        calendrierDetail.setText(getIntent().getStringExtra(Constantes.DATE_KEY));
        getColorDetail = getIntent().getStringExtra(Constantes.COLOR_KEY);
        isCheckedD = getIntent().getExtras().getBoolean(Constantes.FAVORIS_KEY);

        ListModelNotif = myDB.getSpecificNotif(id_element);

        if (ListModelNotif.isEmpty()){
            IdNotif = String.valueOf(myDB.getMaxIdNotif() + 1);
            timeDetail.setText("0:0");
            notifDetail.setChecked(false);
            isNotif = 0;
        }
        if (!ListModelNotif.isEmpty()){
            IdNotif = ListModelNotif.get(0).getNotifid();
            timeDetail.setText(ListModelNotif.get(0).getTime());
            notifDetail.setChecked(true);
            isNotif = 1;
        }

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
                getColorDetail = String.format("#%06X", (0xFFFFFF & color));
                colorDetail.setBackgroundColor(Color.parseColor(getColorDetail));
            }
        });

        calendrierDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(calendrierDetail.getText().toString());
            }
        });

        timeDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(timeDetail.getText().toString());
            }
        });

        notifDetail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    isNotif = 1;
                }
                else{
                    isNotif = 0;
                }
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkFields(titleDetail.getText().toString(), contentDetail.getText().toString(), calendrierDetail.getText().toString())) {
                    MyApolloClient.getMyApolloClient().mutate(
                            UpdatePostTMutation.builder()
                                    .id(id_element)
                                    .title(titleDetail.getText().toString())
                                    .content(contentDetail.getText().toString())
                                    .date(calendrierDetail.getText().toString())
                                    .color(getColorDetail)
                                    .position(position)
                                    .build())
                            .enqueue(new ApolloCall.Callback<UpdatePostTMutation.Data>() {
                                @Override
                                public void onResponse(@NotNull Response<UpdatePostTMutation.Data> response) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);

                                    //finish
                                    intent.putExtra("MAPOSITIONRETOUR", position);
                                    intent.putExtra(Constantes.ID_KEY, id_element);
                                    intent.putExtra(Constantes.TITLE_KEY, titleDetail.getText().toString());
                                    intent.putExtra(Constantes.CONTENT_KEY, contentDetail.getText().toString());
                                    intent.putExtra(Constantes.DATE_KEY, calendrierDetail.getText().toString());
                                    intent.putExtra(Constantes.COLOR_KEY, getColorDetail);
                                    intent.putExtra(Constantes.FAVORIS_KEY, isCheckedD);

                                    //notification
                                    if (isNotif == 1) {
                                        createNotificationChannel();
                                        SimpleDateFormat spf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                                        Date currentTime = Calendar.getInstance().getTime();
                                        try {
                                            Date mDate = spf.parse(convertDate(calendrierDetail.getText().toString() + " " + timeDetail.getText().toString().trim()));
                                            long diff = mDate.getTime() - currentTime.getTime();
                                            if (ListModelNotif.isEmpty()) {
                                                myDB.addNotif(calendrierDetail.getText().toString(), timeDetail.getText().toString().trim(), IdNotif, String.valueOf(isNotif), id_element);
                                                scheduleNotification(DetailActivity.this, diff, Integer.valueOf(IdNotif), titleDetail.getText().toString(), contentDetail.getText().toString());
                                            }
                                            else{
                                                myDB.updateNotif(calendrierDetail.getText().toString(), timeDetail.getText().toString().trim(), IdNotif, String.valueOf(isNotif), id_element);
                                                scheduleNotification(DetailActivity.this, diff, Integer.valueOf(IdNotif), titleDetail.getText().toString(), contentDetail.getText().toString());
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else if (isNotif == 0){
                                        if (!ListModelNotif.isEmpty()) {
                                            scheduleNotification(DetailActivity.this, 0, Integer.valueOf(IdNotif), "Notif deleted", "Notif deleted");
                                            myDB.deleteNotif(id_element);
                                        }
                                    }

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

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                showDate(dayOfMonth, monthOfYear+1, year);
            }
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                dateSetListener, year, month-1, day);
        datePickerDialog.show();
    }

    private String convertDate(String date) throws ParseException {
        SimpleDateFormat spfOld = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
        Date newDate = spfOld.parse(date);
        spfOld = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
        date = spfOld.format(newDate);
        return date;
    }

    private void showDate(int year, int month, int day) {
        calendrierDetail.setText(new StringBuilder().append(year).append("/")
                .append(month).append("/").append(day));
    }

    private void showTimePickerDialog(String time){
        String[] split = time.split(":");
        int hour = Integer.valueOf(split[0]);
        int minute = Integer.valueOf(split[1]);

        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                showTime(hourOfDay, minute);
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, timeSetListener, hour, minute, true);
        timePickerDialog.show();
    }

    private void showTime(int hour, int minute){
        timeDetail.setText(hour+":"+minute);
    }

    private Boolean checkFields(String title, String contenu, String date){
        if (title.isEmpty()){
            Toast.makeText(DetailActivity.this, "Complete the title.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (contenu.isEmpty()){
            Toast.makeText(DetailActivity.this, "Choose the content.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (date.isEmpty()){
            Toast.makeText(DetailActivity.this, "Choose the date.", Toast.LENGTH_LONG).show();
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
