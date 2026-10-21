#!/usr/bin/env python3
"""Write a DataStore Preferences file holding the given string settings.

Usage: prefs.py OUT_FILE key=value [key=value ...]

The UI test pushes this into the app's files/datastore/settings.preferences_pb before the first
launch, so a run starts on a known Now Playing style without driving the Settings screen. The
format is androidx.datastore's PreferenceMap protobuf: a map<string, Value>, where a string value is
field 5 of Value. Only string values are written; every key the app reads here is a string.
"""

import sys


def varint(n: int) -> bytes:
    out = bytearray()
    while True:
        byte = n & 0x7F
        n >>= 7
        if n:
            out.append(byte | 0x80)
        else:
            out.append(byte)
            return bytes(out)


def length_delimited(field: int, data: bytes) -> bytes:
    return varint((field << 3) | 2) + varint(len(data)) + data


def main() -> None:
    out = bytearray()
    for arg in sys.argv[2:]:
        key, value = arg.split("=", 1)
        entry = length_delimited(1, key.encode()) + length_delimited(2, length_delimited(5, value.encode()))
        out += length_delimited(1, entry)
    with open(sys.argv[1], "wb") as f:
        f.write(out)


if __name__ == "__main__":
    main()
