package com.kevinsrm.ide.runtime

import androidx.lifecycle.ViewModel
import com.kevinsrm.ide.AppPaths
import com.kevinsrm.ide.CommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Wrapper do runtime Node.js.
 *
 * Expõe execucao de `node` e `npm` no diretorio do projeto ativo, utilizando
 * os binarios instalados no rootfs do Termux.
 */
class NodeRuntime : ViewModel() {

    /** Diretorio de trabalho (projeto ativo) onde node/npm sao executados. */
    var workDir: java.io.File = AppPaths.projectsDir(null)

    private var nodeBin: String = AppPaths.NODE_BIN
    private var npmBin: String = AppPaths.NPM_BIN

    fun initialize(projectDir: java.io.File?) {
        workDir = projectDir ?: AppPaths.projectsDir(null)
    }

    /** Verifica se o Node.js esta disponível no ambiente. */
    fun isNodeAvailable(): Boolean {
        return java.io.File(AppPaths.NODE_BIN).exists()
    }

    /** Verifica se o NPM esta disponível no ambiente. */
    fun isNpmAvailable(): Boolean {
        return java.io.File(AppPaths.NPM_BIN).exists()
    }

    /**
     * Executa um arquivo JavaScript/TypeScript com Node.js.
     */
    suspend fun runNode(script: String, vararg args: String): CommandResult {
        return withContext(Dispatchers.Default) {
            val cmd = buildString {
                append(nodeBin)
                append(" ")
                append(script)
                args.forEach { append(" $it") }
            }
            runCommand(cmd)
        }
    }

    /**
     * Executa um comando NPM (ex.: `npm install`, `npm start`).
     */
    suspend fun runNpm(vararg args: String): CommandResult {
        return withContext(Dispatchers.Default) {
            val cmd = buildString {
                append(npmBin)
                args.forEach { append(" $it") }
            }
            runCommand(cmd)
        }
    }

    /**
     * Instala dependencias do package.json do projeto ativo.
     */
    suspend fun installDependencies(): CommandResult = runNpm("install")

    /**
     * Inicia o projeto (script "start" do package.json).
     */
    suspend fun start(): CommandResult = runNpm("start")

    private suspend fun runCommand(command: String): CommandResult {
        return withContext(Dispatchers.Default) {
            try {
                val process = Runtime.getRuntime().exec(
                    arrayOf("sh", "-c", "cd '${workDir.absolutePath}' && $command"),
                    null,
                    workDir
                )
                val output = readStream(process.inputStream)
                val error = readStream(process.errorStream)
                val exitCode = process.waitFor()
                CommandResult(exitCode, output, error)
            } catch (e: Exception) {
                CommandResult(-1, "", e.message ?: "Erro ao executar Node.js/NPM")
            }
        }
    }

    private fun readStream(stream: java.io.InputStream): String {
        val reader = BufferedReader(InputStreamReader(stream))
        val lines = StringBuilder()
        var line = reader.readLine()
        while (line != null) {
            lines.append(line).append("\n")
            line = reader.readLine()
        }
        return lines.toString()
    }
}

/**
 * Resultado da execucao de um script Node.js.
 */
data class NodeExecution(
    val script: String,
    val exitCode: Int,
    val output: String,
    val error: String
)