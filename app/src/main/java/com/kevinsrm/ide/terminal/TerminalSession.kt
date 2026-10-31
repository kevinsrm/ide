package com.kevinsrm.ide.terminal

import android.content.Context
import androidx.lifecycle.ViewModel
import com.kevinsrm.ide.AppPaths
import com.kevinsrm.ide.CommandResult
import com.kevinsrm.ide.ServiceStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Sessao de terminal.
 *
 * Executa comandos shell em um diretorio de trabalho (cwd) e captura saida/
 * erro. Integra-se com o ambiente Termux quando disponivel.
 *
 * O contexto do Android e injetado via {@link #init} (chamado pela
 * MainActivity), pois ViewModels nao aceitam parametros no construtor por
 * padrao.
 */
class TerminalSession : ViewModel() {

    private var context: Context? = null

    private var currentWorkingDir: java.io.File = AppPaths.projectsDir(null)

    // Status da sessao (Termux conectado ou shell local)
    var status: ServiceStatus = ServiceStatus.STOPPED
        private set

    fun init(context: Context) {
        this.context = context
        currentWorkingDir = AppPaths.projectsDir(context.externalFilesDir)
    }

    /** Define o diretorio de trabalho (cwd) dos comandos. */
    fun setWorkingDirectory(dir: java.io.File) {
        currentWorkingDir = dir
    }

    /** Retorna o diretorio de trabalho atual. */
    fun getWorkingDirectory(): java.io.File = currentWorkingDir

    /**
     * Executa um comando no diretorio de trabalho.
     *
     * Se o Termux estiver disponivel e configurado, o comando e encaminhado
     * via TermuxBridge; caso contrario, e executado via Process do Android.
     */
    suspend fun execute(command: String): CommandResult {
        status = ServiceStatus.STARTING

        val result = withContext(Dispatchers.Default) {
            try {
                if (context != null && TermuxBridge.isTermuxAvailable(context!!)) {
                    TermuxBridge.executeInTermux(command, currentWorkingDir)
                } else {
                    runLocalShell(command, currentWorkingDir)
                }
            } catch (e: Exception) {
                CommandResult(-1, "", e.message ?: "Erro ao executar comando")
            }
        }

        status = if (result.exitCode == 0) ServiceStatus.RUNNING else ServiceStatus.ERROR
        return result
    }

    private suspend fun runLocalShell(command: String, dir: java.io.File): CommandResult {
        return withContext(Dispatchers.Default) {
            val process = Runtime.getRuntime().exec(
                arrayOf("sh", "-c", command),
                null,
                dir
            )
            val output = readStream(process.inputStream)
            val error = readStream(process.errorStream)
            val exitCode = process.waitFor()
            CommandResult(exitCode, output, error)
        }
    }

    private fun readStream(stream: java.io.InputStream): String {
        val reader = BufferedReader(InputStreamReader(stream))
        val lines = StringBuilder()
        var line: String?
        while { line = reader.readLine() } != null {
            lines.append(line).append("\n")
        }
        return lines.toString()
    }
}

/**
 * Resultado de uma operacao de terminal (alias para CommandResult).
 */
typealias TerminalResult = CommandResult