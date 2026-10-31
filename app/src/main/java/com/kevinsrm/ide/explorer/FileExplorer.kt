package com.kevinsrm.ide.explorer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kevinsrm.ide.IdeFragment
import com.kevinsrm.ide.R
import java.io.File

/**
 * Modelo de um item do explorador de arquivos.
 */
data class FileItem(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val size: Long
)

/**
 * Adapter do RecyclerView para listar arquivos do projeto ativo.
 */
class FileExplorerAdapter(
    private val items: MutableList<FileItem> = mutableListOf(),
    private val onItemClick: (FileItem) -> Unit = {}
) : RecyclerView.Adapter<FileExplorerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.file_name)
        private val detailView: TextView = itemView.findViewById(R.id.file_detail)

        fun bind(item: FileItem) {
            nameView.text = item.name
            detailView.text = if (item.isDirectory) "pasta" else "${item.size} bytes"
            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<FileItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}

/**
 * Logica de listagem de diretorio para o explorador de arquivos.
 */
object FileExplorer {

    /**
     * Lista o conteudo de um diretorio, ordenando pastas primeiro e depois arquivos.
     */
    fun listDirectory(dir: java.io.File): List<FileItem> {
        if (!dir.exists() || !dir.isDirectory) return emptyList()

        return dir.listFiles()
            ?.map { file ->
                FileItem(
                    path = file.absolutePath,
                    name = file.name,
                    isDirectory = file.isDirectory,
                    size = file.length()
                )
            }
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            .orEmpty()
    }
}