# ide

Android IDE focado em **Node.js**, com ambiente Termux integrado, terminal embutido,
Node.js/NPM pré-configurados e serviço de banco de dados **MariaDB/MySQL**.

## Estrutura do projeto

```
ide/
├── app/                              # Módulo principal do aplicativo Android (Kotlin)
│   ├── src/main/
│   │   ├── java/com/kevinsrm/ide/
│   │   │   ├── MainActivity.kt       # Atividade principal (navegação Editor/Terminal/Projeto/DB)
│   │   │   ├── project/              # Gerenciamento de projetos e diretório ativo
│   │   │   │   └── ProjectManager.kt
│   │   │   ├── explorer/             # Explorador de arquivos do projeto
│   │   │   │   └── FileExplorer.kt
│   │   │   ├── editor/               # Editor de código
│   │   │   │   └── CodeEditor.kt
│   │   │   ├── terminal/             # Terminal embutido e ponte com o Termux
│   │   │   │   ├── TerminalSession.kt
│   │   │   │   └── TermuxBridge.kt
│   │   │   ├── runtime/              # Wrapper do runtime Node.js
│   │   │   │   └── NodeRuntime.kt
│   │   │   ├── database/             # Gerenciamento do serviço MariaDB/MySQL
│   │   │   │   └── DatabaseService.kt
│   │   │   └── termux/               # Serviço de integração com o Termux
│   │   │       └── TermuxService.kt
│   │   ├── res/
│   │   │   ├── layout/               # Layouts (atividade principal, fragmentos)
│   │   │   ├── values/               # strings, cores, temas, dimensões
│   │   │   ├── menu/                 # Menu principal
│   │   │   └── navigation/           # Grafo de navegação
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── termux/                           # Bootstrap e configuração do ambiente Termux
│   ├── bootstrap-packages.txt        # Lista de pacotes (nodejs, npm, mariadb, …)
│   ├── setup-termux.sh               # Script de provisionamento do rootfs
│   └── README.md                     # Instruções de instalação do Termux
├── build.gradle.kts                  # Build script raiz
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat             # Gradle Wrapper
├── .gitignore
└── README.md                         # Este arquivo
```

## Pré-requisitos

- Android Studio (Giraffe ou superior) com SDK 34+
- JDK 17
- Dispositivo ou emulador Android (API 24+)
- Aplicativo **Termux** instalado (F-Droid ou Google Play)
- Complemento **Termux:API** recomendado

## Como construir

```bash
# Clone e entre no diretório
git clone https://github.com/kevinsrm/ide.git
cd ide

# Dê permissão de execução ao wrapper (Linux/macOS)
chmod +x gradlew

# Faça o build de debug
./gradlew assembleDebug

# Instale no dispositivo conectado
./gradlew installDebug
```

## Configurando o Termux (Node.js + NPM + MariaDB)

No dispositivo Android, abra o Termux e execute:

```bash
# 1. Atualiza os pacotes do rootfs do Termux
pkg update && pkg upgrade -y

# 2. Instala Node.js, NPM e MariaDB/MySQL
pkg install nodejs npm mariadb -y

# 3. (Opcional) Instala a API do Termux para integração direta com o app
pkg install termux-api -y

# 4. Inicializa o banco de dados MariaDB
mysql_install_db --user=root --datadir=$PREFIX/var/mysql

# 5. Inicia o serviço MariaDB
mysqld_safe --user=root &
# ou, via serviço do Termux:
# termux-service start mariadb
```

O script `termux/setup-termux.sh` automatiza esses passos. Consulte `termux/README.md` para detalhes.

## Como usar o IDE

1. Abra o aplicativo e conceda as permissões de armazenamento.
2. Crie ou abra um projeto (o diretório ativo será usado pelo terminal).
3. Use a aba **Editor** para escrever código JavaScript/TypeScript/Node.js.
4. Use a aba **Terminal** para executar comandos (`node`, `npm`, `mysql`, etc.) diretamente no diretório do projeto ativo.
5. Use a aba **Banco de Dados** para iniciar/parar o MariaDB e executar consultas SQL.

O terminal embutido se comunica com o ambiente Termux (via ponte `TermuxBridge`) e executa todos os comandos no diretório de trabalho do projeto aberto, com interação total com o runtime Node.js e o serviço de banco de dados.

## Licença

MIT