import sys
sys.stdout.reconfigure(encoding="utf-8")
from pptx import Presentation
p=Presentation(r"C:\ws\SkladScan-presentation.pptx")
s=p.slides[13]
for sh in s.shapes:
    if sh.has_text_frame and "github" in sh.text_frame.text.lower():
        print("TEXT:", sh.text_frame.text, "| y=", round(sh.top/914400,2))
    if sh.shape_type==13:
        print("PICTURE at x=", round(sh.left/914400,2), "y=", round(sh.top/914400,2), "w=", round(sh.width/914400,2))
