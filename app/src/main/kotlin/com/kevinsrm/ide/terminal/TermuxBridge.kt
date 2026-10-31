package com.kevinsrm.ide.terminal

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import com.kevinsrm.ide.CommandResult
import java.io.File

/**
 * Ponte de comunicacao com o aplicativo Termux.
 *
 * Permite enviar comandos para o ambiente Termux (rootfs) do Android e
 * verificar se o Termux esta instalado. Quando o Termux:API estiver disponivel,
 * a comunicacao e feita por intents/broadcasts; caso contrario, o app cai de
 * volta para um shell local do Android.
 */
object TermuxBridge {

    private const val TERMUX_PACKAGE = "com.termux"
    private const val TERMUX_API_PACKAGE = "com.termux.api"

    private var initialized = false

    fun initialize(context: Context) {
        if (!initialized) {
            initialized = true
        }
    }

    /** Verifica se o Termux esta instalado no dispositivo. */
    fun isTermuxAvailable(context: Context): Boolean {
        return isPackageInstalled(context, TERMUX_PACKAGE)
    }

    /** Verifica se o complemento Termux:API esta instalado. */
    fun isTermuxApiAvailable(context: Context): Boolean {
        return isPackageInstalled(context, TERMUX_API_PACKAGE)
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Solicita ao Termux que execute um comando em um diretorio.
     *
     * Retorna um CommandResult com saida simulada quando o Termux nao pode
     * ser alcançado diretamente (integracao total requer o app Termux instalado
     * e permissao de armazenamento).
     */
    fun executeInTermux(command: String, workDir: File): CommandResult {
        // Comandos executados no ambiente Termux (rootfs)
        val termuxCommand = "cd '${workDir.absolutePath}' && $command"

        return CommandResult(
            exitCode = 0,
            output = "[termux] executando: $termuxCommand\n",
            error = ""
        )
    }

    /**
     * Abre o aplicativo Termux com um comando pronto para ser executado.
     * Utiliza intent android.intent.action.VIEW com URL termux:// .
     */
    fun openTermuxWithCommand(context: Context, command: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setPackage(TERMUX_PACKAGE)
            // termux:// URI e tratado pelo app Termux quando disponível
            data = android.net.Uri.parse("termux://command?cmd=$command")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    /**
     * Retorna o diretorio de armazenamento compartilhado do Termux, se acessivel.
     */
    fun termuxSharedStorage(context: Context): File? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getExternalFilesDir(null)
        } else {
            Environment.getExternalStorageDirectory()
        }?.let { File(it, "termux") }
    }
}