#!/usr/bin/env python3
"""Пересобрать QR-код для финального слайда презентации.

Использование:
    python tools/make_qr.py https://github.com/username/skladscan

Результат: presentation/assets/qr-github.png
"""
import sys
from pathlib import Path

try:
    import qrcode
except ImportError:  # pragma: no cover
    sys.exit("Установите зависимость: pip install qrcode[pil]")


def main() -> None:
    url = sys.argv[1] if len(sys.argv) > 1 else "https://github.com/username/skladscan"
    root = Path(__file__).resolve().parent.parent
    out = root / "presentation" / "assets" / "qr-github.png"

    qr = qrcode.QRCode(
        version=None,
        error_correction=qrcode.constants.ERROR_CORRECT_M,
        box_size=10,
        border=2,
    )
    qr.add_data(url)
    qr.make(fit=True)
    qr.make_image(fill_color="black", back_color="white").save(out)

    print(f"QR сохранён: {out}")
    print(f"URL: {url}")


if __name__ == "__main__":
    main()
