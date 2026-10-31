package com.kevinsrm.ide.project

import android.content.Context
import androidx.lifecycle.ViewModel
import com.kevinsrm.ide.AppPaths
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Gerencia os projetos do IDE.
 *
 * Responsabilidades:
 *   - Manter o diretorio do projeto ativo
 *   - Listar/criar/abrir projetos
 *   - Fornecer o diretorio de trabalho (cwd) usado pelo terminal e pelo runtime
 */
class ProjectManager : ViewModel() {

    private val projects = ConcurrentHashMap<String, ProjectInfo>()

    // Diretorio ativo (projeto aberto no momento)
    private var activeProject: ProjectInfo? = null

    // Contexto da aplicacao (setado via init)
    private var context: Context? = null

    // Diretorio raiz onde os projetos sao armazenados
    val projectsRoot: File
        get() = AppPaths.projectsDir(context?.externalFilesDir)

    /** Retorna o projeto ativo, se houver. */
    val activeProjectInfo: ProjectInfo?
        get() = activeProject

    /** Caminho do diretorio do projeto ativo (cwd para comandos). */
    val activeProjectDir: File?
        get() = activeProject?.directory

    /** Nome do projeto ativo. */
    val activeProjectName: String?
        get() = activeProject?.name

    /** Todos os projetos conhecidos. */
    val allProjects: List<ProjectInfo>
        get() = projects.values.toList()

    fun init(context: Context) {
        this.context = context
        loadExistingProjects()
    }

    /**
     * Abre (define como ativo) um projeto pelo nome.
     */
    fun openProject(name: String): ProjectInfo? {
        val project = projects[name] ?: createProject(name)
        activeProject = project
        return project
    }

    /**
     * Cria um novo projeto (diretorio + package.json basico).
     */
    fun createProject(name: String): ProjectInfo {
        val dir = File(projectsRoot, name)
        dir.mkdirs()

        // Cria package.json basico se nao existir
        val pkgFile = File(dir, "package.json")
        if (!pkgFile.exists()) {
            pkgFile.writeText("""{
  "name": "$name",
  "version": "1.0.0",
  "description": "Projeto Node.js criado pelo IDE",
  "main": "index.js",
  "scripts": {
    "start": "node index.js"
  },
  "dependencies": {}
}
""".trimIndent())
        }

        // Cria arquivo de entrada basico
        val entry = File(dir, "index.js")
        if (!entry.exists()) {
            entry.writeText("console.log('Hello Node.js from IDE!');\n")
        }

        val project = ProjectInfo(name, dir)
        projects[name] = project
        activeProject = project
        return project
    }

    /**
     * Fecha o projeto ativo (limpa a referencia de diretorio ativo).
     */
    fun closeProject() {
        activeProject = null
    }

    /**
     * Retorna o diretorio de trabalho a ser usado para executar comandos.
     * Se nenhum projeto estiver ativo, retorna o diretorio raiz de projetos.
     */
    fun workingDirectory(): File {
        return activeProject?.directory ?: projectsRoot
    }

    private fun loadExistingProjects() {
        if (!projectsRoot.exists()) {
            projectsRoot.mkdirs()
            return
        }
        projectsRoot.listFiles { file -> file.isDirectory }?.forEach { dir ->
            val info = ProjectInfo(dir.name, dir)
            projects[dir.name] = info
        }
    }
}

/**
 * Informacoes de um projeto.
 */
data class ProjectInfo(
    val name: String,
    val directory: File,
    val createdAt: Long = System.currentTimeMillis()
)