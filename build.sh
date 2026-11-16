#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
# This fixed generated directory is relative to the project root selected above.
rm -rf -- build/classes
mkdir -p build/classes
find src tests -name '*.java' -print | sort > build/sources.txt
javac --release 17 -encoding UTF-8 -Xlint:all -Werror -d build/classes @build/sources.txt
jar --create --file build/BlockchainWallet.jar --main-class wallet.Main -C build/classes .
