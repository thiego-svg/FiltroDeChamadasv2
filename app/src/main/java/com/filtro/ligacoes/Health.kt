package com.filtro.ligacoes

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

/** Confere se a proteção está realmente de pé e avisa quando algo foi desfeito. */
object Health {

    private const val CHANNEL_ID = "filtro_alertas"
    private const val NOTIF_ID = 1001

    fun roleHeld(ctx: Context): Boolean {
        val rm = ctx.getSystemService(RoleManager::class.java) ?: return false
        return rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    fun contactsGranted(ctx: Context): Boolean =
        ctx.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    fun notificationsGranted(ctx: Context): Boolean =
        (Build.VERSION.SDK_INT < 33) ||
            (ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)

    fun isReady(ctx: Context): Boolean = roleHeld(ctx) && contactsGranted(ctx)

    fun checkAndNotify(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java) ?: return

        if (isReady(ctx)) {
            nm.cancel(NOTIF_ID)
            return
        }
        if (!Prefs(ctx).enabled) return
        if (!notificationsGranted(ctx)) return

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Alertas do filtro",
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )

        val open = PendingIntent.getActivity(
            ctx,
            0,
            Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = Notification.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Filtro de chamadas desativado")
            .setContentText("Toque para reativar a proteção contra números desconhecidos.")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()

        nm.notify(NOTIF_ID, notification)
    }
}
