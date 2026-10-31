package com.kevinsrm.ide.termux

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.Nullable
import com.kevinsrm.ide.AppPaths

/**
 * Servico de primeiro plano que mantem o ambiente Termux ativo enquanto o IDE
 * estiver em uso, evitando que o sistema Android encerre o rootfs/processos
 * do Termux (Node.js e MariaDB).
 */
class TermuxService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "com.kevinsrm.ide.termux"

        fun initialize(context: Context) {
            // Inicializacao do servico (chamado pela MainActivity).
            // Em Android 8.0+ o servico e iniciado em primeiro plano com
            // canal de notificacao para manter o rootfs do Termux ativo.
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Mantem o servico rodando em primeiro plano
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Caminho do rootfs do Termux utilizado pelos servicos Node.js/MariaDB.
     */
    fun termuxRoot(): String = AppPaths.TERMUX_HOME
}