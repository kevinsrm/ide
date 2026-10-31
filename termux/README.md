# Termux — Bootstrap do ambiente Node.js + MariaDB

Este diretório contém os scripts e a lista de pacotes necessários para
provisionar o ambiente Termux (rootfs) utilizado pelo IDE Android.

## Pacotes (`bootstrap-packages.txt`)

- `nodejs` — runtime Node.js
- `npm` — gerenciador de pacotes Node
- `mariadb` — servidor e cliente MySQL/MariaDB
- `termux-api` — integração com apps Android (opcional)
- `openssh`, `git`, `curl`, `wget`, `vim`, `nano`, `which`, `proot`, `tsu` — utilitários

## Execução

No Termux do dispositivo Android:

```bash
cd /sdcard/Download/ide/termux   # ou onde o projeto estiver
sh setup-termux.sh
```

O script instala os pacotes, inicializa o banco de dados MariaDB e configura
as variáveis de ambiente (`PATH`, `NODE_PATH`).

## Integração com o IDE Android

O aplicativo se comunica com o Termux por intents (`TermuxBridge`) e,
quando disponível, pela API do Termux (`am start-service` / broadcasts).
Todos os comandos do terminal embutido são executados no **diretório do
projeto ativo**, permitindo uso direto de `node`, `npm`, `mysql`, etc.