package com.kevinsrm.ide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.kevinsrm.ide.databinding.ActivityMainBinding
import com.kevinsrm.ide.database.DatabaseService
import com.kevinsrm.ide.termux.TermuxService
import com.kevinsrm.ide.terminal.TermuxBridge
import com.kevinsrm.ide.project.ProjectManager

/**
 * Atividade principal do IDE.
 *
 * Fornece navegacao inferior entre as telas:
 *   - Editor      : escrita de codigo Node.js
 *   - Terminal    : terminal embutido (executa no diretorio do projeto ativo)
 *   - Projeto     : explorador de arquivos e gerenciamento de projetos
 *   - Banco de Dados: servico MariaDB/MySQL
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // Instancias compartilhadas dos servicos do IDE (visao modelo simplificada)
    private val projectManager: ProjectManager by viewModels()
    private val termuxBridge: TermuxBridge by viewModels()
    private val nodeRuntime: NodeRuntime by viewModels()
    private val databaseService: DatabaseService by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Inicializa servicos de infraestrutura (Termux, Node.js, Banco)
        TermuxService.initialize(applicationContext)
        TermuxBridge.initialize(applicationContext)
        nodeRuntime.initialize(projectManager.activeProjectDir)
        DatabaseService.initialize(applicationContext)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.editorFragment, R.id.terminalFragment, R.id.projectFragment, R.id.databaseFragment)
        )

        // ActionBar ligada ao grafo de navegacao
        setupActionBarWithNavController(navController, appBarConfiguration)

        // BottomNavigationView ligada ao grafo de navegacao
        binding.bottomNav.setupWithNavController(navController)

        // Acao "up" da ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * Retorna o servico de ponte com o Termux (para uso dos fragmentos).
     */
    fun getTermuxBridge(): TermuxBridge = termuxBridge

    /**
     * Retorna o gerenciador de projetos (diretorio ativo).
     */
    fun getProjectManager(): ProjectManager = projectManager

    /**
     * Retorna o wrapper do runtime Node.js.
     */
    fun getNodeRuntime(): NodeRuntime = nodeRuntime

    /**
     * Retorna o servico de banco de dados MariaDB/MySQL.
     */
    fun getDatabaseService(): DatabaseService = databaseService

    companion object {
        const int REQUEST_TERMUX_PERMISSIONS = 1001
    }
}

/**
 * Fragmento base generico para as telas do IDE. Mantem referencia aos servicos
 * compartilhados pela MainActivity.
 */
abstract class IdeFragment : Fragment() {

    protected val mainActivity: MainActivity
        get() = requireActivity() as MainActivity

    protected val projectManager: ProjectManager
        get() = mainActivity.getProjectManager()

    protected val termuxBridge: TermuxBridge
        get() = mainActivity.getTermuxBridge()

    protected val nodeRuntime: NodeRuntime
        get() = mainActivity.getNodeRuntime()

    protected val databaseService: DatabaseService
        get() = mainActivity.getDatabaseService()
}

/**
 * Navegador de fragmentos auxiliar. Permite trocar de tela mantendo o estado
 * da pilha de navegacao do componente Navigation.
 */
object FragmentNavigator {

    fun navigateTo(controller: NavController, destinationId: Int) {
        controller.navigate(destinationId)
    }
}

/**
 * Resultado padronizado de executacao de comandos no terminal / runtime.
 */
data class CommandResult(
    val exitCode: Int,
    val output: String,
    val error: String
)

/**
 * Estado de um servico de infraestrutura (Node.js, MariaDB, Termux).
 */
enum class ServiceStatus {
    STOPPED,
    STARTING,
    RUNNING,
    ERROR
}

/**
 * Diretorios padronizados utilizados pelo IDE.
 */
object AppPaths {

    const val TERMUX_HOME = "/data/data/com.termux/files/usr"
    const val NODE_BIN = "$TERMUX_HOME/bin/node"
    const val NPM_BIN = "$TERMUX_HOME/bin/npm"
    const val MYSQL_BIN = "$TERMUX_HOME/bin/mysql"
    const val MYSQLADMIN_BIN = "$TERMUX_HOME/bin/mysqladmin"
    const val MARIADB_SAFE = "$TERMUX_HOME/bin/mysqld_safe"

    fun projectsDir(externalFilesDir: java.io.File?): java.io.File {
        val dir = externalFilesDir?.let { java.io.File(it, "projects") }
            ?: java.io.File("/data/data/com.kevinsrm.ide/files/projects")
        dir.mkdirs()
        return dir
    }
}