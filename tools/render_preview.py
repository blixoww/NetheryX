"""Vues isometriques pour juger sans lancer le jeu : gabarits de structures et biomes reellement generes.

    python3 tools/render_preview.py structure onyx_citadel/keep_0 sortie.png
    python3 tools/render_preview.py dump run-gametest/biome_dump sortie.png      (une planche de tous les biomes)

Les couleurs viennent des vraies textures (celles du mod, puis celles du jar de Minecraft). C'est un apercu : ni lumiere
dynamique ni particules, mais les formes, les densites et les ambiances de couleur s'y lisent.
"""
import gzip
import io
import json
import struct
import sys
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src" / "main" / "resources"
ASSETS = RES / "assets" / "netheryx" / "textures" / "block"
JAR = Path.home() / ".gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client-extra.jar"

TOP_SUFFIX = ("_top", "")
SIDE_SUFFIX = ("_side", "", "_front", "_top")
FIXED = {"minecraft:lava": (230, 96, 18), "minecraft:water": (50, 90, 200), "minecraft:fire": (255, 150, 30), "minecraft:soul_fire": (80, 220, 240),
         "minecraft:bedrock": (40, 40, 40), "netheryx:warded_bricks": (60, 50, 80), "minecraft:chest": (160, 110, 50), "minecraft:spawner": (30, 50, 70),
         "minecraft:barrel": (120, 85, 45), "minecraft:glowstone": (240, 210, 130), "minecraft:shroomlight": (250, 170, 90)}
ALIASES = {"nether_grass_block": "nether_grass_block", "ember_hyphae": "ember_stem", "stripped_ember_hyphae": "stripped_ember_stem",
           "marrow_hyphae": "marrow_stem", "wartstalk_hyphae": "wartstalk_stem", "withered_hyphae": "withered_stem",
           "crimson_hyphae": "crimson_stem", "warped_hyphae": "warped_stem", "onyx_brick_slab": "onyx_bricks", "onyx_brick_stairs": "onyx_bricks",
           "onyx_brick_wall": "onyx_bricks", "magma_brick_slab": "magma_bricks", "magma_brick_stairs": "magma_bricks", "magma_brick_wall": "magma_bricks",
           "magma_block": "magma", "ember_slab": "ember_planks", "ember_stairs": "ember_planks", "ember_fence": "ember_planks", "ember_fence_gate": "ember_planks",
           "withered_slab": "withered_planks", "withered_stairs": "withered_planks", "withered_fence": "withered_planks", "withered_fence_gate": "withered_planks",
           "marrow_slab": "marrow_planks", "marrow_stairs": "marrow_planks", "marrow_fence": "marrow_planks", "wartstalk_slab": "wartstalk_planks",
           "wartstalk_stairs": "wartstalk_planks", "wartstalk_fence": "wartstalk_planks", "rimestone_brick_slab": "rimestone_bricks",
           "rimestone_brick_stairs": "rimestone_bricks", "rimestone_brick_wall": "rimestone_bricks", "polished_blackstone_brick_stairs": "polished_blackstone_bricks",
           "polished_blackstone_brick_wall": "polished_blackstone_bricks", "polished_blackstone_slab": "polished_blackstone", "polished_blackstone_stairs": "polished_blackstone",
           "red_nether_brick_stairs": "red_nether_bricks", "red_nether_brick_wall": "red_nether_bricks", "red_nether_brick_slab": "red_nether_bricks",
           "nether_brick_stairs": "nether_bricks", "nether_brick_slab": "nether_bricks", "nether_brick_wall": "nether_bricks", "nether_brick_fence": "nether_bricks",
           "quartz_stairs": "quartz_block_side", "quartz_slab": "quartz_block_side", "quartz_block": "quartz_block_side", "soul_campfire": "soul_campfire_log_lit",
           "campfire": "campfire_log", "lantern": "lantern", "soul_lantern": "soul_lantern", "skeleton_skull": "bone_block_side", "wither_skeleton_skull": "obsidian",
           "bone_block": "bone_block_side", "crimson_nylium": "crimson_nylium", "warped_nylium": "warped_nylium", "basalt": "basalt_side", "polished_basalt": "polished_basalt_side",
           "smooth_basalt": "smooth_basalt", "ancient_debris": "ancient_debris_side", "chain": "chain", "iron_bars": "iron_bars", "candle": "candle", "white_wool": "white_wool"}


class Colors:
    def __init__(self):
        self.jar = zipfile.ZipFile(JAR)
        self.names = set(self.jar.namelist())
        self.cache = {}

    def _average(self, img):
        img = img.convert("RGBA")
        img = img.crop((0, 0, img.width, img.width))
        total, count = [0, 0, 0], 0
        for p in img.getdata():
            if p[3] > 40:
                total[0] += p[0]
                total[1] += p[1]
                total[2] += p[2]
                count += 1
        return tuple(v // count for v in total) if count else None

    def _texture(self, namespace, base):
        if namespace == "netheryx":
            path = ASSETS / f"{base}.png"
            return Image.open(path) if path.is_file() else None
        name = f"assets/minecraft/textures/block/{base}.png"
        return Image.open(io.BytesIO(self.jar.read(name))) if name in self.names else None

    def _lookup(self, namespace, base, suffixes):
        base = ALIASES.get(base, base)
        for suffix in suffixes:
            img = self._texture(namespace, base + suffix)
            if img is None:
                img = self._texture("minecraft", base + suffix)
            if img is not None:
                return self._average(img)
        return None

    def get(self, name):
        if name in self.cache:
            return self.cache[name]
        namespace, base = name.split(":") if ":" in name else ("minecraft", name)
        if name in FIXED:
            top = side = FIXED[name]
        else:
            top = self._lookup(namespace, base, TOP_SUFFIX)
            side = self._lookup(namespace, base, SIDE_SUFFIX) or top
            top = top or side
            if top is None:
                top = side = (200, 0, 200)
        self.cache[name] = (top, side)
        return self.cache[name]


PLANTS = ("grass", "fern", "roots", "sprouts", "fungus", "bloom", "daisy", "tulip", "bellflower", "shrub", "bush", "lily", "glowcap", "wheat", "pepper", "tuber", "vines",
          "cluster", "crystal", "candle", "chain", "lantern", "skull", "torch", "bars", "sapling", "fig", "plum", "mushroom", "pressure_plate", "lever", "ladder", "rail", "sign")


def thin(name):
    return any(p in name for p in PLANTS) and not name.endswith("_block") and "wart_block" not in name


def render(voxels, size, colors, scale=3, y_max=None):
    """voxels : dict (x, y, z) -> nom. Vue isometrique, de l'avant vers l'arriere par recouvrement."""
    sx, sy, sz = size
    width = (sx + sz) * scale * 2 + 8
    height = (sx + sz) * scale + sy * scale * 2 + 8
    img = Image.new("RGB", (width, height), (26, 14, 16))
    draw = ImageDraw.Draw(img)
    ox, oy = sz * scale * 2 + 4, sy * scale * 2 + 4

    def shade(c, f):
        return (min(255, int(c[0] * f)), min(255, int(c[1] * f)), min(255, int(c[2] * f)))

    # Lumiere qui baisse avec la profondeur : les creux se lisent mieux
    for (x, y, z), name in sorted(voxels.items(), key=lambda kv: (kv[0][0] + kv[0][2], kv[0][1])):
        if y_max is not None and y > y_max:
            continue
        if ((x, y + 1, z) in voxels and (y_max is None or y + 1 <= y_max)) and (x + 1, y, z) in voxels and (x, y, z + 1) in voxels:
            continue
        top, side = colors.get(name)
        px = ox + (x - z) * scale * 2
        py = oy + (x + z) * scale - y * scale * 2
        s = scale
        if thin(name):
            draw.polygon([(px - s, py), (px + s, py), (px + s, py + 2 * s), (px - s, py + 2 * s)], fill=shade(top, 1.15))
            continue
        draw.polygon([(px, py - s), (px + 2 * s, py), (px, py + s), (px - 2 * s, py)], fill=shade(top, 1.1))
        draw.polygon([(px - 2 * s, py), (px, py + s), (px, py + 3 * s), (px - 2 * s, py + 2 * s)], fill=shade(side, 0.72))
        draw.polygon([(px + 2 * s, py), (px, py + s), (px, py + 3 * s), (px + 2 * s, py + 2 * s)], fill=shade(side, 0.5))
    return img


def read_nbt(path):
    """Lecteur NBT minimal : renvoie la racine du gabarit."""
    data = gzip.decompress(Path(path).read_bytes())
    pos = [0]

    def take(fmt):
        value = struct.unpack_from(">" + fmt, data, pos[0])
        pos[0] += struct.calcsize(">" + fmt)
        return value[0]

    def string():
        n = take("H")
        s = data[pos[0]:pos[0] + n].decode("utf-8")
        pos[0] += n
        return s

    def payload(t):
        if t == 1:
            return take("b")
        if t == 2:
            return take("h")
        if t == 3:
            return take("i")
        if t == 4:
            return take("q")
        if t == 5:
            return take("f")
        if t == 6:
            return take("d")
        if t == 7:
            n = take("i")
            pos[0] += n
            return None
        if t == 8:
            return string()
        if t == 9:
            inner, n = take("b"), take("i")
            return [payload(inner) for _ in range(n)]
        if t == 10:
            out = {}
            while True:
                tag = take("b")
                if tag == 0:
                    return out
                key = string()
                out[key] = payload(tag)
        if t == 11:
            n = take("i")
            return [take("i") for _ in range(n)]
        raise ValueError(t)

    take("b")
    string()
    return payload(10)


def template_voxels(root):
    palette = [p["Name"] for p in root["palette"]]
    voxels = {}
    for b in root["blocks"]:
        name = palette[b["state"]]
        if name not in ("minecraft:air", "minecraft:cave_air", "minecraft:structure_void", "minecraft:light"):
            voxels[tuple(b["pos"])] = name
    return voxels, tuple(root["size"])


def structure(ref, out, scale):
    namespace_dir, file = ref.split("/")
    root = read_nbt(RES / "data" / "netheryx" / "structures" / namespace_dir / f"{file}.nbt")
    voxels, size = template_voxels(root)
    colors = Colors()
    y_max = int(sys.argv[5]) if len(sys.argv) > 5 else None
    render(voxels, size, colors, scale, y_max).save(out)


def load_dump(path):
    head = json.loads(Path(str(path) + ".json").read_text())
    data = Path(path).read_bytes()
    sx, sy, sz = head["size"]
    voxels = {}
    i = 0
    for x in range(sx):
        for z in range(sz):
            for y in range(sy):
                value = data[i] << 8 | data[i + 1]
                i += 2
                name = head["palette"][value]
                if name not in ("minecraft:air", "minecraft:cave_air", "minecraft:void_air", "minecraft:light", "minecraft:bedrock"):
                    voxels[(x, y, z)] = name
    return voxels, (sx, sy, sz), head


def peel(voxels, size):
    """Retire la voute et la masse au-dessus de la premiere caverne au-dessus de la lave : on voit les cavernes de l'interieur."""
    out = {}
    sx, sy, sz = size
    for x in range(sx):
        for z in range(sz):
            run = 0
            state = 0  # 0 : on cherche le sol, 1 : dans la caverne
            for y in range(20, sy):
                name = voxels.get((x, y, z))
                if name is None:
                    state = 1
                    run = 0
                    continue
                if state == 0:
                    if y >= 16:
                        out[(x, y, z)] = name
                    continue
                run += 1
                if run >= 6:
                    # Roche massive : c'est la voute, on enleve ce qu'on a garde au-dessus du vide
                    for k in range(y - run + 1, y + 1):
                        out.pop((x, k, z), None)
                    break
                out[(x, y, z)] = name
    return out


def dump_sheet(folder, out, scale=2):
    colors = Colors()
    tiles = []
    for file in sorted(Path(folder).glob("*.bin")):
        voxels, size, head = load_dump(file)
        # On coupe au-dessus du sol pour voir dedans : le plafond du Nether cacherait tout
        heights = {}
        for (x, y, z) in voxels:
            if head["floor"] - 12 <= y <= head["floor"] + 8:
                heights[(x, z)] = max(heights.get((x, z), 0), y)
        tile = render(peel(voxels, size), size, colors, scale)
        draw = ImageDraw.Draw(tile)
        draw.text((6, 6), file.stem.replace("netheryx_", "").replace("minecraft_", "(v) "), fill=(255, 240, 200))
        tiles.append(tile)
    if not tiles:
        sys.exit("Aucun releve : lancez les GameTests avec NETHERYX_DUMP=1")
    cols = 4 if len(tiles) > 4 else len(tiles)
    w, h = tiles[0].size
    # On retire la bande vide du haut
    trim = int(h * 0.42)
    sheet = Image.new("RGB", (cols * w, ((len(tiles) + cols - 1) // cols) * (h - trim)), (10, 6, 8))
    for i, tile in enumerate(tiles):
        sheet.paste(tile.crop((0, trim, w, h)), ((i % cols) * w, (i // cols) * (h - trim)))
        ImageDraw.Draw(sheet).text(((i % cols) * w + 6, (i // cols) * (h - trim) + 4), Path(sorted(Path(folder).glob('*.bin'))[i]).stem.replace("netheryx_", ""), fill=(255, 240, 200))
    sheet.save(out)
    print(sheet.size)


if __name__ == "__main__":
    mode = sys.argv[1]
    if mode == "structure":
        structure(sys.argv[2], sys.argv[3], int(sys.argv[4]) if len(sys.argv) > 4 else 3)
    else:
        dump_sheet(sys.argv[2], sys.argv[3])
