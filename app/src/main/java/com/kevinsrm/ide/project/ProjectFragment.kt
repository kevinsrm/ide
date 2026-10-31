package com.kevinsrm.ide.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kevinsrm.ide.IdeFragment
import com.kevinsrm.ide.R
import com.kevinsrm.ide.databinding.FragmentProjectBinding
import com.kevinsrm.ide.explorer.FileExplorer
import com.kevinsrm.ide.explorer.FileExplorerAdapter

/**
 * Fragmento do Projeto.
 *
 * Lista os projetos disponiveis, permite criar/abrir projetos e exibe o
 * explorador de arquivos do projeto ativo.
 */
class ProjectFragment : IdeFragment() {

    private var _binding: FragmentProjectBinding? = null
    private val binding get() = _binding!!

    private lateinit var projectManagerViewModel: ProjectManager
    private lateinit var adapter: FileExplorerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectBinding.inflate(inflater, container, false)
        val view = binding.root

        projectManagerViewModel = ViewModelProvider(this)[ProjectManager::class.java]

        // Inicializa o gerenciador com o contexto da aplicacao
        projectManagerViewModel.init(requireContext().applicationContext)

        adapter = FileExplorerAdapter(onItemClick = { item ->
            if (item.isDirectory) {
                projectManagerViewModel.openProject(item.name)
                refreshProjectList()
                // Atualiza o diretorio de trabalho do terminal/runtime
                val mainActivity = requireActivity() as MainActivity
                mainActivity.getNodeRuntime().setWorkDir(
                    projectManagerViewModel.activeProjectDir!!
                )
            }
        })

        binding.projectList.layoutManager = LinearLayoutManager(requireContext())
        binding.projectList.adapter = adapter

        refreshProjectList()

        // Botao criar projeto
        binding.btnCreateProject.setOnClickListener {
            val name = binding.projectName.text.toString().trim()
            if (name.isNotEmpty()) {
                projectManagerViewModel.createProject(name)
                refreshProjectList()
                binding.projectName.text.clear()
            }
        }

        // Botao abrir projeto
        binding.btnOpenProject.setOnClickListener {
            val name = binding.projectName.text.toString().trim()
            if (name.isNotEmpty()) {
                projectManagerViewModel.openProject(name)
                refreshProjectList()
                // Atualiza o diretorio de trabalho do terminal
                val mainActivity = requireActivity() as MainActivity
                mainActivity.getNodeRuntime().setWorkDir(
                    projectManagerViewModel.activeProjectDir!!
                )
            }
        }

        return view
    }

    private fun refreshProjectList() {
        val projects = projectManagerViewModel.allProjects
        if (projects.isEmpty()) {
            binding.projectEmpty.visibility = View.VISIBLE
            binding.projectList.visibility = View.GONE
        } else {
            binding.projectEmpty.visibility = View.GONE
            binding.projectList.visibility = View.VISIBLE
            val items = projects.map {
                com.kevinsrm.ide.explorer.FileItem(
                    path = it.directory.absolutePath,
                    name = it.name,
                    isDirectory = true,
                    size = 0
                )
            }
            adapter.setItems(items)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}