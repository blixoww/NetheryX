#!/usr/bin/env python3
"""
Genere toutes les ressources de NetheryX dans src/main/resources.

Usage : python3 tools/generate_resources.py
Le script est deterministe : relancez-le apres toute modification du contenu (tools/netheryx_gen/content.py).
"""
import shutil
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from netheryx_gen import book, common, data, docs, entities, gui, models, particles, structures, textures, worldgen  # noqa: E402


def main():
    for generated in (common.ASSETS, common.DATA):
        if generated.exists():
            shutil.rmtree(generated)
    textures.generate()
    gui.generate()
    models.generate()
    entities.generate()
    particles.generate()
    data.generate()
    worldgen.generate()
    structures.generate()
    book.generate()
    docs.generate()
    art = common.copy_art()
    if art:
        print(f"NetheryX : {art} textures faites main copiees depuis art/.")
    print(f"NetheryX : {common.written_count()} fichiers JSON generes + textures et structures.")


if __name__ == "__main__":
    main()
