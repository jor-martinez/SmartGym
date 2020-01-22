package mx.infornet.smartgym;

import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class BroadcastAvancePerderPeso extends BroadcastReceiver {

    private int idNotify = 2;
    private String channelId = "my_channel_02";
    private NotificationCompat.Builder mBuilder;

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder = new NotificationCompat.Builder(context, null);

        Intent notificationIntent = new Intent(context, AvancePerderPesoActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent1 = PendingIntent.getActivity(context, 1, notificationIntent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            //nombre visible del canal
            CharSequence name = "Avance";

            //descripcion visible del canal
            String descripcion = "Abre esta notificacion para que registres tu avance";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);

            //se configura el canal de la notificacion
            mChannel.setDescription(descripcion);
            mChannel.enableLights(true);

            //se determina luces, vibracion, etc
            mChannel.setLightColor(Color.BLACK);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[] {100,200,300,400,500,400,300,200,400});
            manager.createNotificationChannel(mChannel);

            mBuilder = new NotificationCompat.Builder(context, channelId);

        }

        mBuilder
                .setSmallIcon(R.mipmap.icon)
                .setContentTitle("Avance")
                .setContentText("Cuéntanos como te está llendo")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Abre esta notificacion para que registres tu avance"))
                .setAutoCancel(true)
                .setContentIntent(intent1);

        manager.notify(idNotify, mBuilder.build());

    }
}
