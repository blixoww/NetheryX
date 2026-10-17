"""Ecrivain NBT minimal (format Java, gzip, big-endian) pour les gabarits de structures."""
import gzip
import struct

TAG_END, TAG_BYTE, TAG_SHORT, TAG_INT, TAG_LONG, TAG_FLOAT, TAG_DOUBLE = 0, 1, 2, 3, 4, 5, 6
TAG_BYTE_ARRAY, TAG_STRING, TAG_LIST, TAG_COMPOUND, TAG_INT_ARRAY = 7, 8, 9, 10, 11


class Byte(int):
    pass


class Short(int):
    pass


class Long(int):
    pass


class Float(float):
    pass


class Double(float):
    pass


class TypedList(list):
    """Liste NBT dont le type d'element est force (utile pour les listes vides)."""

    def __init__(self, tag_type, items=()):
        super().__init__(items)
        self.tag_type = tag_type


def _tag_type(value):
    if isinstance(value, Byte) or isinstance(value, bool):
        return TAG_BYTE
    if isinstance(value, Short):
        return TAG_SHORT
    if isinstance(value, Long):
        return TAG_LONG
    if isinstance(value, int):
        return TAG_INT
    if isinstance(value, Float):
        return TAG_FLOAT
    if isinstance(value, (Double, float)):
        return TAG_DOUBLE
    if isinstance(value, str):
        return TAG_STRING
    if isinstance(value, dict):
        return TAG_COMPOUND
    if isinstance(value, (list, tuple)):
        return TAG_LIST
    raise TypeError(f"Type NBT non supporte : {type(value)}")


def _write_string(out, text):
    raw = text.encode("utf-8")
    out.append(struct.pack(">H", len(raw)))
    out.append(raw)


def _write_payload(out, value, tag_type):
    if tag_type == TAG_BYTE:
        out.append(struct.pack(">b", int(value)))
    elif tag_type == TAG_SHORT:
        out.append(struct.pack(">h", value))
    elif tag_type == TAG_INT:
        out.append(struct.pack(">i", value))
    elif tag_type == TAG_LONG:
        out.append(struct.pack(">q", value))
    elif tag_type == TAG_FLOAT:
        out.append(struct.pack(">f", value))
    elif tag_type == TAG_DOUBLE:
        out.append(struct.pack(">d", value))
    elif tag_type == TAG_STRING:
        _write_string(out, value)
    elif tag_type == TAG_LIST:
        if isinstance(value, TypedList):
            element_type = value.tag_type
        else:
            element_type = _tag_type(value[0]) if value else TAG_END
        out.append(struct.pack(">bi", element_type, len(value)))
        for item in value:
            _write_payload(out, item, element_type)
    elif tag_type == TAG_COMPOUND:
        for key, item in value.items():
            item_type = _tag_type(item)
            out.append(struct.pack(">b", item_type))
            _write_string(out, key)
            _write_payload(out, item, item_type)
        out.append(struct.pack(">b", TAG_END))
    else:
        raise TypeError(tag_type)


def write_nbt(path, root: dict) -> None:
    out = [struct.pack(">b", TAG_COMPOUND)]
    _write_string(out, "")
    _write_payload(out, root, TAG_COMPOUND)
    path.parent.mkdir(parents=True, exist_ok=True)
    with gzip.open(path, "wb") as handle:
        handle.write(b"".join(out))
