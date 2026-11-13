import sys
sys.stdout.reconfigure(encoding="utf-8")
import qrcode
img = qrcode.make("https://github.com/yarikdurden/skladscan")
img.save(r"C:\ws\presentation\assets\qr-github.png")
print("qr size", img.size)
