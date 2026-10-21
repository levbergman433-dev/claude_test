#!/usr/bin/env python3
"""Shrink an emulator screenshot into base64 JPEG text.

Usage: shot.py PNG OUT_TXT

Artifacts from this repository cannot be downloaded from the agent's sandbox, but check runs can
be read through the API, and a check run's output text holds up to 65535 characters. So each
screenshot leaves the job as a JPEG small enough for that, base64-encoded into OUT_TXT. Quality
steps down until it fits.
"""

import base64
import io
import sys

from PIL import Image

LIMIT = 64_000


def main() -> None:
    path, out = sys.argv[1], sys.argv[2]
    image = Image.open(path).convert("RGB")
    for width in (540, 450, 360):
        scaled = image.resize((width, round(image.height * width / image.width)), Image.LANCZOS)
        for quality in (75, 65, 55, 45):
            buffer = io.BytesIO()
            scaled.save(buffer, format="JPEG", quality=quality, optimize=True)
            encoded = base64.b64encode(buffer.getvalue()).decode()
            if len(encoded) <= LIMIT:
                with open(out, "w") as f:
                    f.write(encoded)
                return
    raise SystemExit("screenshot does not fit")


if __name__ == "__main__":
    main()
