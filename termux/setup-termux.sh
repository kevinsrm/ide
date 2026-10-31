#!/data/data/com.termux/files/usr/bin/sh
#
# setup-termux.sh
# Provisiona o ambiente Termux (rootfs) para o IDE Node.js:
#   - Atualiza pacotes
#   - Instala Node.js, NPM e MariaDB/MySQL
#   - Inicializa o banco de dados
#
# Execute no Termux do dispositivo Android:
#   sh setup-termux.sh
#
set -e

echo "[1/5] Atualizando pacotes do rootfs do Termux..."
pkg update && pkg upgrade -y

echo "[2/5] Instalando pacotes (nodejs, npm, mariadb, termux-api, ...)"
while IFS= read -r pkg || [ -n "$pkg" ]; do
    # Ignora linhas em branco e comentarios
    case "$pkg" in
        ''|\#*) continue ;;
    esac
    pkg install -y "$pkg"
done < bootstrap-packages.txt

echo "[3/5] Inicializando o diretorio de dados do MariaDB..."
mkdir -p $PREFIX/var/mysql
if [ ! -d "$PREFIX/var/mysql/mysql" ]; then
    mysql_install_db --user=root --datadir=$PREFIX/var/mysql
fi

echo "[4/5] Configurando variáveis de ambiente para Node.js..."
grep -q 'export NODE_PATH' $HOME/.bashrc 2>/dev/null || \
    echo 'export NODE_PATH=$PREFIX/lib/node_modules' >> $HOME/.bashrc
grep -q 'export PATH' $HOME/.bashrc 2>/dev/null || \
    echo 'export PATH=$PREFIX/bin:$PATH' >> $HOME/.bashrc

echo "[5/5] Concluido!"
echo ""
echo "Para iniciar o MariaDB manualmente:"
echo "  mysqld_safe --user=root &"
echo ""
echo "Para usar com o IDE Android, conceda permissao de armazenamento ao Termux:"
echo "  termux-setup-storage"