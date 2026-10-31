package com.kevinsrm.ide.database

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kevinsrm.ide.IdeFragment
import com.kevinsrm.ide.R
import com.kevinsrm.ide.databinding.FragmentDatabaseBinding

/**
 * Fragmento do Banco de Dados.
 *
 * Gerencia o servico MariaDB/MySQL (iniciar/parar) e permite executar
 * consultas SQL no diretorio do projeto ativo.
 */
class DatabaseFragment : IdeFragment() {

    private var _binding: FragmentDatabaseBinding? = null
    private val binding get() = _binding!!

    private lateinit var databaseService: DatabaseService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDatabaseBinding.inflate(inflater, container, false)
        val view = binding.root

        databaseService = ViewModelProvider(this)[DatabaseService::class.java]

        binding.databaseOutput.movementMethod = ScrollingMovementMethod()
        updateStatus()

        binding.databaseStart.setOnClickListener {
            lifecycleScope.launchWhenStarted {
                if (!databaseService.isInstalled()) {
                    appendOutput("MariaDB/MySQL nao encontrado no Termux.\n")
                    appendOutput("Rote: instale o pacote 'mariadb' via 'pkg install mariadb'.\n")
                } else {
                    databaseService.startServer().let { result ->
                        appendOutput(result.output)
                        updateStatus()
                    }
                }
            }
        }

        binding.btnQuery.setOnClickListener {
            val sql = binding.inputSql.text.toString().trim()
            if (sql.isNotEmpty()) {
                lifecycleScope.launchWhenStarted {
                    databaseService.query(sql).let { result ->
                        appendOutput(result.output)
                        if (result.error.isNotEmpty()) {
                            appendOutput("ERRO SQL: ${result.error}\n", error = true)
                        }
                    }
                }
            }
        }

        return view
    }

    private fun updateStatus() {
        val status = databaseService.getStatus()
        binding.databaseStatus.text = when (status) {
            com.kevinsrm.ide.ServiceStatus.RUNNING -> "Banco de dados rodando"
            com.kevinsrm.ide.ServiceStatus.STOPPED -> "Banco de dados parado"
            com.kevinsrm.ide.ServiceStatus.ERROR -> "Erro no banco de dados"
            com.kevinsrm.ide.ServiceStatus.STARTING -> "Iniciando banco de dados..."
        }
    }

    private fun appendOutput(text: String, error: Boolean = false) {
        val current = binding.databaseOutput.text.toString()
        binding.databaseOutput.text = current + text
        binding.databaseScroll.post {
            binding.databaseScroll.fullScroll(View.FOCUS_DOWN)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}