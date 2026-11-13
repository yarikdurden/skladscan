@echo off
rem Пересобрать QR-код на финальном слайде:  tools\make_qr.bat https://github.com/username/repo
setlocal
set URL=%1
if "%URL%"=="" set URL=https://github.com/username/skladscan
python "%~dp0make_qr.py" "%URL%"
