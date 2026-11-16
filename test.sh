#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
sh build.sh
java -jar build/BlockchainWallet.jar --test "$@"
