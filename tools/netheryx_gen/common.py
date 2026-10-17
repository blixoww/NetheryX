"""Utilitaires partages : chemins, ecriture JSON, constantes du mod."""
import json
import os
from pathlib import Path

MOD_ID = "netheryx"
ROOT = Path(__file__).resolve().parents[2]
RESOURCES = ROOT / "src" / "main" / "resources"
ASSETS = RESOURCES / "assets" / MOD_ID
DATA = RESOURCES / "data"

_written = []


def write_json(path: Path, obj) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8") as handle:
        json.dump(obj, handle, indent=2, ensure_ascii=False)
        handle.write("\n")
    _written.append(path)


def data_path(*parts: str, namespace: str = MOD_ID) -> Path:
    return DATA.joinpath(namespace, *parts)


def asset_path(*parts: str) -> Path:
    return ASSETS.joinpath(*parts)


def mid(name: str) -> str:
    """Identifiant complet dans l'espace de noms du mod."""
    return name if ":" in name else f"{MOD_ID}:{name}"


def written_count() -> int:
    return len(_written)
