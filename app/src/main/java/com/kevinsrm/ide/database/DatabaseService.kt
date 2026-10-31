package com.kevinsrm.ide.database

import androidx.lifecycle.ViewModel
import com.kevinsrm.ide.AppPaths
import com.kevinsrm.ide.CommandResult
import com.kevinsrm.ide.ServiceStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Gerenciamento do servico MariaDB/MySQL via Termux.
 *
 * Permite iniciar/parar o servidor e executar consultas SQL no banco de dados
 * do projeto ativo.
 */
class DatabaseService : ViewModel() {

    private var status: ServiceStatus = ServiceStatus.STOPPED
        private set

    /** Retorna o status atual do servico de banco. */
    fun getStatus(): ServiceStatus = status

    /** Verifica se os binarios do MariaDB estao disponíveis. */
    fun isInstalled(): Boolean {
        return java.io.File(AppPaths.MYSQL_BIN).exists() &&
            java.io.File(AppPaths.MYSQLADMIN_BIN).exists()
    }

    /**
     * Inicia o servidor MariaDB (mysqld_safe em segundo plano).
     */
    suspend fun startServer(): CommandResult {
        return withContext(Dispatchers.Default) {
            status = ServiceStatus.STARTING
            val result = runCommand("mysqld_safe --user=root --datadir=${AppPaths.TERMUX_HOME}/var/mysql &")
            status = if (result.exitCode == 0) ServiceStatus.RUNNING else ServiceStatus.ERROR
            result
        }
    }

    /**
     * Para o servidor MariaDB (mysqladmin shutdown).
     */
    suspend fun stopServer(): CommandResult {
        return withContext(Dispatchers.Default) {
            val result = runCommand("mysqladmin -u root shutdown")
            status = ServiceStatus.STOPPED
            result
        }
    }

    /**
     * Executa uma consulta SQL no servidor MariaDB local.
     */
    suspend fun query(sql: String): CommandResult {
        return withContext(Dispatchers.Default) {
            runCommand("mysql -u root -e \"$sql\"")
        }
    }

    /**
     * Cria um banco de dados para o projeto ativo (nome baseado no projeto).
     */
    suspend fun createDatabase(dbName: String): CommandResult {
        return query("CREATE DATABASE IF NOT EXISTS $dbName CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;")
    }

    private suspend fun runCommand(command: String): CommandResult {
        return withContext(Dispatchers.Default) {
            try {
                val process = Runtime.getRuntime().exec(
                    arrayOf("sh", "-c", command),
                    null,
                    java.io.File(AppPaths.TERMUX_HOME)
                )
                val output = readStream(process.inputStream)
                val error = readStream(process.errorStream)
                val exitCode = process.waitFor()
                CommandResult(exitCode, output, error)
            } catch (e: Exception) {
                CommandResult(-1, "", e.message ?: "Erro ao acessar o MariaDB")
            }
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
 * Informacao de uma conexao com o banco de dados.
 */
data class DatabaseConnection(
    val host: String = "127.0.0.1",
    val port: Int = 3306,
    val user: String = "root",
    val password: String = "",
    val database: String? = null
)