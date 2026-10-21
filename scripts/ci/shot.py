#!/usr/bin/env python3
"""Turn an emulator screenshot into a GitHub notice annotation.

Usage: shot.py PNG NAME

Artifacts from this repository cannot be downloaded from the agent's sandbox, but check-run
annotations can be read through the API. So each screenshot leaves the job as a small JPEG,
base64-encoded into the message of a notice titled "shot:NAME". Quality steps down until the
message fits well inside the annotation size limit.
"""

import base64
import io
import sys

from PIL import Image

LIMIT = 60_000


def main() -> None:
    path, name = sys.argv[1], sys.argv[2]
    image = Image.open(path).convert("RGB")
    width = 360
    image = image.resize((width, round(image.height * width / image.width)), Image.LANCZOS)
    for quality in (70, 60, 50, 40, 30):
        buffer = io.BytesIO()
        image.save(buffer, format="JPEG", quality=quality, optimize=True)
        encoded = base64.b64encode(buffer.getvalue()).decode()
        if len(encoded) <= LIMIT:
            break
    print(f"::notice title=shot:{name}::{encoded}")


if __name__ == "__main__":
    main()
