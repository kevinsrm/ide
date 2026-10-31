package com.kevinsrm.ide.terminal

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.kevinsrm.ide.IdeFragment
import com.kevinsrm.ide.R
import com.kevinsrm.ide.databinding.FragmentTerminalBinding
import kotlinx.coroutines.launch

/**
 * Fragmento do Terminal embutido.
 *
 * Executa comandos diretamente no diretorio do projeto ativo, com suporte a
 * Node.js, NPM e MariaDB/MySQL via ambiente Termux.
 */
class TerminalFragment : IdeFragment() {

    private var _binding: FragmentTerminalBinding? = null
    private val binding get() = _binding!!

    private lateinit var terminalSession: TerminalSession

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTerminalBinding.inflate(inflater, container, false)
        val view = binding.root

        terminalSession = ViewModelProvider(this)[TerminalSession::class.java]

        // Vincula ao diretorio do projeto ativo
        projectManager.activeProjectDir?.let { dir ->
            terminalSession.setWorkingDirectory(dir)
            binding.terminalStatus.text = "Terminal ativo: ${dir.name}"
        } ?: run {
            binding.terminalStatus.text = "Sem projeto ativo"
        }

        // Movimento de rolagem na saida
        binding.terminalOutput.movementMethod = ScrollingMovementMethod()

        // Botao executar
        binding.btnExecute.setOnClickListener {
            val command = binding.terminalInput.text.toString().trim()
            if (command.isNotEmpty()) {
                executeCommand(command)
            }
        }

        // Tecla Enter no teclado
        binding.terminalInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                val command = binding.terminalInput.text.toString().trim()
                if (command.isNotEmpty()) {
                    executeCommand(command)
                }
                true
            } else {
                false
            }
        }

        return view
    }

    private fun executeCommand(command: String) {
        binding.terminalInput.text.clear()
        appendOutput("\$ $command\n")

        // Execucao assincrona (corrotina)
        lifecycleScope.launch {
            terminalSession.execute(command).let { result ->
                appendOutput(result.output)
                if (result.error.isNotEmpty()) {
                    appendOutput("ERRO: ${result.error}\n", error = true)
                }
                binding.terminalStatus.text = if (result.exitCode == 0) {
                    "Terminal ativo"
                } else {
                    "Comando finalizou com codigo ${result.exitCode}"
                }
            }
        }
    }

    private fun appendOutput(text: String, error: Boolean = false) {
        val current = binding.terminalOutput.text.toString()
        binding.terminalOutput.text = current + text
        binding.terminalScroll.post {
            binding.terminalScroll.fullScroll(View.FOCUS_DOWN)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}