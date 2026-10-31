package com.kevinsrm.ide.editor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.kevinsrm.ide.IdeFragment
import com.kevinsrm.ide.R
import com.kevinsrm.ide.databinding.FragmentEditorBinding

/**
 * Fragmento do Editor de codigo.
 *
 * Permite carregar, editar e salvar arquivos de codigo do projeto ativo.
 */
class EditorFragment : IdeFragment() {

    private var _binding: FragmentEditorBinding? = null
    private val binding get() = _binding!!

    private val codeEditor = CodeEditor()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.editorCode.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.editorEmpty.visibility = View.GONE
                binding.editorCode.visibility = View.VISIBLE
            }
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}