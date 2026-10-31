package com.kevinsrm.ide.editor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kevinsrm.ide.IdeFragment
import com.kevinsrm.ide.R

/**
 * Placeholder do editor de codigo.
 *
 * Nesta versao da estrutura, o editor e representado por um TextView/EditText
 * simples. Em producao, substitua por um editor de sintaxe (ex.: EditorComponent
 * do ETLIView, WebView com Monaco/CodeMirror ou SyntaxHighlighter personalizado).
 */
class CodeEditor {

    private var currentFilePath: String? = null
    private var content: String = ""

    fun loadFile(path: String): String {
        currentFilePath = path
        val file = java.io.File(path)
        content = if (file.exists()) file.readText() else ""
        return content
    }

    fun saveFile(path: String, text: String): Boolean {
        try {
            java.io.File(path).writeText(text)
            currentFilePath = path
            content = text
            return true
        } catch (e: Exception) {
            return false
        }
    }

    val currentContent: String
        get() = content

    val currentPath: String?
        get() = currentFilePath
}

/**
 * Adapter minimo para listar arquivos abertos no editor (abas).
 */
class OpenFileAdapter(
    private val files: MutableList<String> = mutableListOf()
) : RecyclerView.Adapter<OpenFileAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tabTitle: TextView = itemView.findViewById(R.id.file_name)
        fun bind(path: String) {
            tabTitle.text = path.substringAfterLast("/")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int = files.size

    fun addFile(path: String) {
        if (!files.contains(path)) {
            files.add(path)
            notifyItemInserted(files.lastIndex)
        }
    }

    fun removeFile(path: String) {
        val index = files.indexOf(path)
        if (index >= 0) {
            files.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}

/**
 * Fragmento base para telas de editor (mantido como referencia de integracao).
 */
abstract class EditorFragmentBase : IdeFragment() {
    protected val codeEditor = CodeEditor()
}