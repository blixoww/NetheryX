#!/bin/sh
# Publie la version courante : commit, push, puis tag (le tag declenche .github/workflows/release.yml).
# Sous Windows (PowerShell ou cmd), utiliser gitpush.bat.
# Usage : ./gitpush.sh          -> version lue dans gradle.properties (mod_version)
#         ./gitpush.sh "message"  -> message de commit personnalise
set -e

VERSION=$(sed -n 's/^mod_version=//p' gradle.properties | tr -d '\r')
if [ -z "$VERSION" ]; then
    echo "mod_version introuvable dans gradle.properties" >&2
    exit 1
fi
if ! grep -q "^## \[$VERSION\]" CHANGELOG.md; then
    echo "Aucune section [$VERSION] dans CHANGELOG.md : ajoutez les notes de version avant de publier." >&2
    exit 1
fi
if git rev-parse -q --verify "refs/tags/$VERSION" >/dev/null; then
    echo "Le tag $VERSION existe deja : augmentez mod_version dans gradle.properties." >&2
    exit 1
fi

git add -A
git commit -m "${1:-NetheryX $VERSION}"
git push origin main
git tag "$VERSION"
git push origin "$VERSION"
