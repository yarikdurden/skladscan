#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Сборка PPTX-версии презентации СкладСкан (14 слайдов, тёмная тема 16:9)."""
from pathlib import Path

from PIL import Image
from pptx import Presentation
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE
from pptx.enum.text import MSO_ANCHOR, PP_ALIGN
from pptx.util import Emu, Inches, Pt

ROOT = Path(__file__).resolve().parent
SCREENS = ROOT / "presentation" / "assets" / "screens"
ASSETS = ROOT / "presentation" / "assets"
OUT = ROOT / "SkladScan-presentation.pptx"

# ---------------------------------------------------------------- палитра
INK = RGBColor(0x07, 0x0A, 0x0F)
INK_SOFT = RGBColor(0x0A, 0x0E, 0x15)
SURFACE = RGBColor(0x10, 0x17, 0x22)
SURFACE2 = RGBColor(0x17, 0x21, 0x2F)
TEXT = RGBColor(0xF2, 0xF6, 0xE9)
DIM = RGBColor(0x98, 0xA4, 0xB2)
MUTED = RGBColor(0x69, 0x75, 0x85)
LIME = RGBColor(0xC6, 0xFF, 0x4A)
MINT = RGBColor(0x7B, 0xF0, 0xB4)
CYAN = RGBColor(0x63, 0xE6, 0xFF)
AMBER = RGBColor(0xFF, 0xC2, 0x4B)
CORAL = RGBColor(0xFF, 0x6B, 0x7A)
VIOLET = RGBColor(0xB6, 0x9C, 0xFF)
WHITE = RGBColor(0xFF, 0xFF, 0xFF)
FONT = "Segoe UI"
TOTAL = 14

prs = Presentation()
prs.slide_width = Inches(13.333)
prs.slide_height = Inches(7.5)
SW = prs.slide_width
SH = prs.slide_height
BLANK = prs.slide_layouts[6]


# ---------------------------------------------------------------- хелперы
def box(slide, x, y, w, h, fill=None, line=None, radius=None, line_w=1.0, shape=None):
    if shape is None:
        shape = MSO_SHAPE.ROUNDED_RECTANGLE if radius else MSO_SHAPE.RECTANGLE
    shp = slide.shapes.add_shape(shape, Inches(x), Inches(y), Inches(w), Inches(h))
    if radius:
        try:
            shp.adjustments[0] = radius
        except (IndexError, ValueError):
            pass
    if fill is None:
        shp.fill.background()
    else:
        shp.fill.solid()
        shp.fill.fore_color.rgb = fill
    if line is None:
        shp.line.fill.background()
    else:
        shp.line.color.rgb = line
        shp.line.width = Pt(line_w)
    shp.shadow.inherit = False
    if shp.has_text_frame:
        shp.text_frame.word_wrap = True
    return shp


def text(slide, x, y, w, h, runs, size=14, color=TEXT, bold=False, align=PP_ALIGN.LEFT,
         anchor=MSO_ANCHOR.TOP, line_spacing=1.15, space_after=0, caps=False):
    """runs: строка или список строк/списков run-словарей."""
    tb = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = tb.text_frame
    tf.word_wrap = True
    tf.margin_left = 0
    tf.margin_right = 0
    tf.margin_top = 0
    tf.margin_bottom = 0
    tf.vertical_anchor = anchor
    if isinstance(runs, str):
        runs = [runs]
    first = True
    for line in runs:
        p = tf.paragraphs[0] if first else tf.add_paragraph()
        first = False
        p.alignment = align
        p.line_spacing = line_spacing
        p.space_after = Pt(space_after)
        items = line if isinstance(line, list) else [line]
        for item in items:
            if isinstance(item, str):
                item = {"t": item}
            r = p.add_run()
            r.text = item["t"].upper() if caps and item.get("caps", True) else item["t"]
            f = r.font
            f.name = FONT
            f.size = Pt(item.get("s", size))
            f.bold = item.get("b", bold)
            f.italic = item.get("i", False)
            f.color.rgb = item.get("c", color)
    return tb


def new_slide(page, section=None, header=True):
    s = prs.slides.add_slide(BLANK)
    bg = s.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, SW, SH)
    bg.fill.solid()
    bg.fill.fore_color.rgb = INK
    bg.line.fill.background()
    bg.shadow.inherit = False
    # декоративные пятна
    for (dx, dy, w, h, col) in (
        (9.4, -3.4, 8.0, 8.0, RGBColor(0x16, 0x1F, 0x0C)),
        (-3.6, 4.0, 7.2, 7.2, RGBColor(0x0C, 0x18, 0x20)),
    ):
        glow = s.shapes.add_shape(MSO_SHAPE.OVAL, Inches(dx), Inches(dy), Inches(w), Inches(h))
        glow.fill.solid()
        glow.fill.fore_color.rgb = col
        glow.line.fill.background()
        glow.shadow.inherit = False
    if header:
        mark = box(s, 0.65, 0.42, 0.29, 0.29, fill=LIME, radius=0.32)
        text(s, 1.03, 0.44, 3.2, 0.28, "СКЛАДСКАН", size=10, color=TEXT, bold=True)
        if section:
            text(s, 7.6, 0.46, 5.08, 0.26, section, size=9.5, color=MUTED, bold=True,
                 align=PP_ALIGN.RIGHT, caps=True)
    return s


def footer(s, label, page):
    line = s.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.65), Inches(6.93), Inches(10.2), Pt(0.8))
    line.fill.solid()
    line.fill.fore_color.rgb = RGBColor(0x1C, 0x22, 0x2C)
    line.line.fill.background()
    line.shadow.inherit = False
    text(s, 11.0, 6.82, 1.68, 0.26, label, size=9, color=MUTED, bold=True, align=PP_ALIGN.RIGHT)
    text(s, 11.98, 6.82, 0.7, 0.26, f"{page:02d} / {TOTAL}", size=9, color=DIM, bold=True,
         align=PP_ALIGN.RIGHT)


def kicker(s, x, y, w, label, color=LIME):
    bar = s.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(x), Inches(y + 0.135), Inches(0.24), Pt(1.6))
    bar.fill.solid()
    bar.fill.fore_color.rgb = color
    bar.line.fill.background()
    bar.shadow.inherit = False
    text(s, x + 0.34, y, w, 0.26, label, size=10, color=color, bold=True, caps=True)


def h2(s, x, y, w, runs, size=33, h=1.2):
    return text(s, x, y, w, h, runs, size=size, color=TEXT, bold=True, line_spacing=1.02)


def lead(s, x, y, w, runs, size=12.5, h=0.8):
    return text(s, x, y, w, h, runs, size=size, color=DIM, line_spacing=1.32)


def phone(slide, x, y, w, h, name, circle=True, frame=True):
    path = SCREENS / name
    if frame:
        box(slide, x - 0.045, y - 0.045, w + 0.09, h + 0.09, fill=RGBColor(0x15, 0x1B, 0x25), radius=0.055)
    pic = slide.shapes.add_picture(str(path), Inches(x), Inches(y), Inches(w), Inches(h))
    if circle:
        r = min(w, h) * 0.045
        notch = slide.shapes.add_shape(MSO_SHAPE.OVAL, Emu(int(Inches(x + w / 2 - r))),
                                       Emu(int(Inches(y + 0.06))), Emu(int(Inches(2 * r))),
                                       Emu(int(Inches(2 * r))))
        notch.fill.solid()
        notch.fill.fore_color.rgb = RGBColor(0x05, 0x07, 0x0A)
        notch.line.fill.background()
        notch.shadow.inherit = False
    return pic


# ======================================================== 01. Титул
s = new_slide(1, header=False)
glow = s.shapes.add_shape(MSO_SHAPE.OVAL, Inches(8.3), Inches(-1.2), Inches(7.6), Inches(9.6))
glow.fill.solid()
glow.fill.fore_color.rgb = RGBColor(0x14, 0x1C, 0x0E)
glow.line.fill.background()
glow.shadow.inherit = False

kicker(s, 0.75, 1.55, 6.0, "Мобильный терминал складского учёта")
text(s, 0.75, 1.95, 7.4, 1.7, [[{"t": "Склад"}, {"t": "Скан", "c": LIME}]], size=84, bold=True,
     line_spacing=0.95)
lead(s, 0.75, 3.42, 6.2,
     "Сканируй штрихкод камерой телефона, меняй остаток в одно касание и не останавливай склад, "
     "когда пропал Wi-Fi.", size=13.5, h=1.0)

chips = ["Android 8+", "Kotlin", "Jetpack Compose", "CameraX", "ML Kit", "Room", "Offline-first"]
cx = 0.75
for c in chips:
    wch = 0.16 + len(c) * 0.072
    if cx + wch > 6.9:
        break
    ch = box(s, cx, 4.62, wch, 0.32, fill=RGBColor(0x14, 0x1B, 0x26), line=RGBColor(0x26, 0x2E, 0x3A), radius=0.5)
    text(s, cx, 4.66, wch, 0.24, c, size=9, color=DIM, bold=True, align=PP_ALIGN.CENTER)
    cx += wch + 0.12

text(s, 0.75, 5.5, 6.4, 0.3, "2026  ·  v1.0.0  ·  APK 43 МБ", size=9.5, color=MUTED, bold=True, caps=True)
phone(s, 9.05, 0.72, 3.72, 6.05, "scanner.png")

# ======================================================== 02. Проблема
s = new_slide(2, "Проблема")
footer(s, "Проблема", 2)
kicker(s, 0.65, 0.98, 5.4, "Что не так с учётом")
h2(s, 0.65, 1.34, 6.1, ["Склад знает всё.", [{"t": "Только не в момент операции.", "c": MUTED}]], size=31)
lead(s, 0.65, 2.68, 5.9,
     "Между полкой и системой — бумага, Excel и человеческая память. Остаток становится «примерным», "
     "а расхождения находятся на инвентаризации.", h=1.0)
box(s, 0.65, 3.95, 5.9, 1.42, fill=RGBColor(0x1C, 0x12, 0x15), line=RGBColor(0x4A, 0x27, 0x2E), radius=0.06)
text(s, 0.9, 4.12, 5.4, 0.26, "Три источника правды", size=10, color=CORAL, bold=True, caps=True)
text(s, 0.9, 4.42, 5.4, 0.42, "Полка, тетрадь и Excel показывают разное.", size=15, bold=True)
text(s, 0.9, 4.9, 5.4, 0.3, "Итог: пересорт, недостачи и часы сверок вместо работы.", size=10.5, color=DIM)

pains = [
    ("01", "Бумага и Excel", "Записали на лист — потеряли лист. Остаток в системе живёт отдельно от полки."),
    ("02", "Дорогие ТСД", "Промышленный терминал — 30–60 тыс. ₽ плюс лицензия на каждое устройство."),
    ("03", "Нет сети — нет работы", "Wi-Fi в цеху и на складе падает, а приёмку останавливать нельзя."),
    ("04", "Ручной ввод", "Цифры, пересорт, опечатки. Ошибки всплывают через неделю и стоят денег."),
    ("05", "Сложные интерфейсы", "Кладовщик обходит ERP: «долго, непонятно, проще в тетрадь»."),
]
py = 0.98
for n, t, d in pains:
    box(s, 7.05, py, 5.63, 1.03, fill=RGBColor(0x0D, 0x13, 0x1C), line=RGBColor(0x1D, 0x25, 0x30), radius=0.07)
    text(s, 7.25, py + 0.16, 0.4, 0.24, n, size=10, color=CORAL, bold=True)
    text(s, 7.7, py + 0.12, 4.8, 0.26, t, size=12.5, bold=True)
    text(s, 7.7, py + 0.45, 4.85, 0.5, d, size=9.5, color=DIM, line_spacing=1.25)
    py += 1.13

# ======================================================== 03. Решение
s = new_slide(3, "Решение")
footer(s, "Решение", 3)
kicker(s, 0.65, 0.98, 6.0, "Что делает приложение")
h2(s, 0.65, 1.34, 9.5, ["Три действия — и остаток сходится"], size=33)
lead(s, 0.65, 2.02, 9.0,
     "Один инструмент вместо бумаги, Excel и звонков «а сколько у нас на самом деле?».", h=0.5)

steps = [
    (LIME, "01", "Сканируй",
     "Камера телефона вместо ТСД. Распознавание на устройстве: 11 форматов, включая EAN-13, Code 128, QR и Data Matrix.",
     "Подсветка · вибрация · ручной ввод"),
    (CYAN, "02", "Подтверди",
     "Приход, списание или пересчёт. Количество, комментарий и проверка: списать больше остатка нельзя.",
     "Валидация · история по каждому SKU"),
    (AMBER, "03", "Синхронизируй",
     "Операция сразу сохраняется на устройстве и уходит на сервер. Без сети — ждёт в очереди и отправляется сама.",
     "Room · WorkManager · автоповтор"),
]
sx = 0.65
for col, n, t, d, fn in steps:
    box(s, sx, 2.7, 3.98, 2.62, fill=RGBColor(0x0E, 0x14, 0x1D), line=RGBColor(0x1E, 0x26, 0x32), radius=0.05)
    text(s, sx + 2.75, 2.72, 1.0, 0.7, n, size=34, color=RGBColor(0x1E, 0x26, 0x33), bold=True, align=PP_ALIGN.RIGHT)
    tile = box(s, sx + 0.28, 2.96, 0.5, 0.5, fill=RGBColor(0x13, 0x1A, 0x24), line=col, radius=0.22)
    text(s, sx + 0.28, 3.07, 0.5, 0.28, n, size=11, color=col, bold=True, align=PP_ALIGN.CENTER)
    text(s, sx + 0.28, 3.62, 3.4, 0.34, t, size=16.5, bold=True)
    text(s, sx + 0.28, 4.03, 3.42, 0.85, d, size=10, color=DIM, line_spacing=1.3)
    text(s, sx + 0.28, 4.92, 3.42, 0.26, fn, size=8.5, color=MUTED, bold=True)
    sx += 4.07

facts = [("0 ₽", "за устройство — подойдёт любой смартфон"),
         ("11", "форматов штрихкодов «из коробки»"),
         ("офлайн", "учёт не зависит от Wi-Fi"),
         ("6", "экранов, понятных без обучения")]
fx = 0.65
for big, small in facts:
    box(s, fx, 5.55, 3.05, 0.92, fill=RGBColor(0x11, 0x18, 0x21), line=RGBColor(0x25, 0x30, 0x3D), radius=0.09)
    text(s, fx + 0.22, 5.66, 1.0, 0.4, big, size=16, color=LIME, bold=True)
    text(s, fx + 1.25, 5.7, 1.65, 0.6, small, size=8.5, color=DIM, line_spacing=1.2)
    fx += 3.16

# ======================================================== 04. Скрины: сканирование
s = new_slide(4, "Интерфейс · сканирование")
footer(s, "Интерфейс", 4)
kicker(s, 0.65, 0.98, 6.0, "Экраны приложения")
h2(s, 0.65, 1.34, 10.0, ["Сканер, который всегда в кармане"], size=33)
shots = [("scanner.png", "Сканер", "Камера, рамка и подсказки. Тёмная тема не слепит в цеху."),
         ("scan-result.png", "Результат сканирования", "Товар, остаток и действие — в одном нижнем листе."),
         ("stock.png", "Остатки", "Стоимость запасов, фильтры и список позиций.")]
px = 1.05
for name, cap, desc in shots:
    phone(s, px, 2.28, 2.05, 3.95, name)
    text(s, px - 0.15, 6.32, 2.35, 0.26, cap, size=11.5, bold=True, align=PP_ALIGN.CENTER)
    text(s, px - 0.15, 6.58, 2.35, 0.3, desc, size=8.5, color=MUTED, align=PP_ALIGN.CENTER, line_spacing=1.1)
    px += 3.72

# ======================================================== 05. Скрины: учёт
s = new_slide(5, "Интерфейс · учёт")
footer(s, "Интерфейс", 5)
kicker(s, 0.65, 0.98, 6.0, "Экраны приложения")
h2(s, 0.65, 1.34, 10.0, ["Каждое движение — в истории"], size=33)
shots = [("product.png", "Карточка товара", "Остаток, минимум, стоимость и все операции по SKU."),
         ("history.png", "История", "Очередь отправки, фильтры и хронология движений."),
         ("analytics.png", "Сводка", "Стоимость запасов, динамика за неделю и дефицит."),
         ("settings.png", "Настройки", "Адрес API, демо-режим, звук и вибрация, темы.")]
px = 0.5
for name, cap, desc in shots:
    phone(s, px, 2.35, 1.98, 3.8, name)
    text(s, px - 0.1, 6.3, 2.18, 0.26, cap, size=11, bold=True, align=PP_ALIGN.CENTER)
    text(s, px - 0.1, 6.56, 2.18, 0.3, desc, size=8, color=MUTED, align=PP_ALIGN.CENTER, line_spacing=1.1)
    px += 3.09

# ======================================================== 06. Возможности
s = new_slide(6, "Возможности")
footer(s, "Возможности", 6)
kicker(s, 0.65, 0.98, 6.0, "Что уже работает")
h2(s, 0.65, 1.34, 10.0, ["Не прототип, а собранное приложение"], size=33)
feats = [
    (LIME, "Камера вместо ТСД",
     "EAN-13, EAN-8, Code 128/39/93, QR, Data Matrix, UPC-A/E, ITF, PDF417. Распознавание на устройстве — кадры никуда не уходят.",
     "CameraX · ML Kit · 11 форматов"),
    (AMBER, "Offline-first",
     "Room — источник правды. Каждая операция попадает в очередь и уходит на сервер, когда появляется сеть. WorkManager повторяет отправку.",
     "Room · WorkManager · retry"),
    (CYAN, "Учёт без ошибок",
     "Приход, списание и инвентаризация. Проверка остатка, запрет отрицательных значений, полная история по каждому товару.",
     "StockMath · валидация · история"),
    (VIOLET, "Аналитика",
     "Стоимость запасов, движение за 7 дней, дефицит по минимумам и структура категорий. Всё считается локально.",
     "Compose Canvas · Flow"),
    (CORAL, "Настройки под склад",
     "Адрес API, демо-режим, звук и вибрация, непрерывное сканирование, три темы. Всё меняется без пересборки.",
     "DataStore · 3 темы · API URL"),
    (MINT, "Демо из коробки",
     "20 реальных складских позиций с валидными EAN-13 и 30 операций. Приложение можно запустить и показать без сервера.",
     "20 SKU · 30 операций · 0 настройки"),
]
gx, gy = 0.65, 2.24
for i, (col, t, d, tag) in enumerate(feats):
    x = gx + (i % 3) * 4.07
    y = gy + (i // 3) * 2.24
    box(s, x, y, 3.9, 2.1, fill=RGBColor(0x0E, 0x14, 0x1D), line=RGBColor(0x1E, 0x26, 0x32), radius=0.055)
    box(s, x + 0.26, y + 0.26, 0.12, 0.12, fill=col, radius=0.5)
    text(s, x + 0.46, y + 0.2, 3.2, 0.3, t, size=13.5, bold=True)
    text(s, x + 0.26, y + 0.66, 3.4, 1.0, d, size=9.5, color=DIM, line_spacing=1.32)
    text(s, x + 0.26, y + 1.74, 3.4, 0.24, tag, size=8.5, color=col, bold=True)

# ======================================================== 07. Кому подходит
s = new_slide(7, "Кому подходит")
footer(s, "Кому подходит", 7)
kicker(s, 0.65, 0.98, 6.0, "Позиционирование")
h2(s, 0.65, 1.34, 10.0, ["Для тех, у кого склад меньше, чем ERP"], size=32)
lead(s, 0.65, 2.0, 9.4, "Небольшие склады и участки, где нужен быстрый учёт, а не полугодовое внедрение.", h=0.4)
personas = [
    ("01", "Небольшой склад и даркстор", "1–5 кладовщиков, сотни SKU. Нужны остатки, приёмка и списания без покупки ТСД."),
    ("02", "Производственный участок", "Выдача в цех и расходники. Важно быстро списать и видеть, что осталось."),
    ("03", "Мобильная торговля", "Выездной агент или курьер: остатки, приёмка и пересчёт прямо на телефоне."),
    ("04", "Пилот перед автоматизацией", "Проверить процесс, данные и дисциплину до покупки терминалов и лицензий."),
]
gx, gy = 0.65, 2.5
for i, (n, t, d) in enumerate(personas):
    x = gx + (i % 2) * 6.1
    y = gy + (i // 2) * 1.19
    box(s, x, y, 5.93, 1.07, fill=RGBColor(0x0E, 0x14, 0x1D), line=RGBColor(0x1E, 0x26, 0x32), radius=0.07)
    text(s, x + 0.22, y + 0.14, 0.5, 0.24, n, size=10, color=LIME, bold=True)
    text(s, x + 0.75, y + 0.11, 4.9, 0.28, t, size=12.5, bold=True)
    text(s, x + 0.75, y + 0.46, 4.95, 0.5, d, size=9.5, color=DIM, line_spacing=1.24)
box(s, 0.65, 5.05, 12.03, 0.72, fill=RGBColor(0x14, 0x12, 0x0D), line=RGBColor(0x3A, 0x30, 0x1E), radius=0.09)
text(s, 0.9, 5.17, 11.6, 0.5,
     [[{"t": "Когда не подойдёт: ", "b": True, "c": AMBER},
       {"t": "партии и сроки годности, RFID, весы, печать этикеток, обмен документами с 1С «из коробки». Это задачи другого класса — и другого бюджета.", "c": DIM}]],
     size=10.5)
text(s, 0.65, 5.98, 12.0, 0.4,
     [[{"t": "Цель — не заменить ERP, а ", "c": DIM}, {"t": "убрать разрыв", "b": True, "c": TEXT},
       {"t": " между полкой и учётной системой.", "c": DIM}]], size=12.5)

# ======================================================== 08. Архитектура
s = new_slide(8, "Архитектура")
footer(s, "Архитектура", 8)
kicker(s, 0.65, 0.98, 6.0, "Как это устроено")
h2(s, 0.65, 1.34, 10.0, ["Офлайн — не режим, а основа"], size=33)

stack = [(None, "UI", "Jetpack Compose · MVVM", "6 экранов, 5 вкладок"),
         (None, "Домен", "Repository · StockMath", "валидация и правила учёта"),
         (LIME, "Room", "локальный кэш", "источник правды"),
         (CYAN, "Sync", "Retrofit · WorkManager", "очередь и ретраи"),
         (None, "API", "REST · 1С · ERP", "внешний контур")]
sy = 2.24
for i, (accent, tag, name, sub) in enumerate(stack):
    if i == 3:
        box(s, 3.28, sy, 2.55, 0.72, fill=RGBColor(0x0E, 0x14, 0x1D), line=RGBColor(0x1E, 0x26, 0x32), radius=0.07)
        text(s, 3.4, sy + 0.09, 2.3, 0.22, tag, size=8.5, color=CYAN, bold=True, caps=True)
        text(s, 3.4, sy + 0.3, 2.4, 0.22, name, size=9.5, bold=True)
        text(s, 3.4, sy + 0.5, 2.3, 0.2, sub, size=8, color=MUTED)
        sy += 0.82
        continue
    width = 2.55 if i != 2 else 2.55
    x0 = 0.72 if i != 3 else 0.72
    box(s, x0, sy, width, 0.72, fill=RGBColor(0x0E, 0x14, 0x1D), line=RGBColor(0x1E, 0x26, 0x32), radius=0.07)
    tag_col = accent if accent else MUTED
    text(s, x0 + 0.12, sy + 0.09, 2.3, 0.22, tag, size=8.5, color=tag_col, bold=True, caps=True)
    text(s, x0 + 0.12, sy + 0.3, 2.4, 0.22, name, size=9.5, bold=True)
    text(s, x0 + 0.12, sy + 0.5, 2.3, 0.2, sub, size=8, color=MUTED)
    if i == 2:
        box(s, 3.28, sy, 2.55, 0.72, fill=RGBColor(0x0E, 0x14, 0x1D), line=RGBColor(0x1E, 0x26, 0x32), radius=0.07)
        text(s, 3.4, sy + 0.09, 2.3, 0.22, "Sync", size=8.5, color=CYAN, bold=True, caps=True)
        text(s, 3.4, sy + 0.3, 2.4, 0.22, "Retrofit · WorkManager", size=9.5, bold=True)
        text(s, 3.4, sy + 0.5, 2.3, 0.2, "очередь и ретраи", size=8, color=MUTED)
    sy += 0.82

notes = [("Room — единственный источник правды", "UI читает только локальные данные. Сервер не нужен, чтобы показать остаток или провести операцию."),
         ("Очередь операций", "Каждая операция хранится с clientOpId и статусом. Повторная отправка не создаёт дубль."),
         ("Синхронизация фоном", "WorkManager и NetworkMonitor поднимают отправку при появлении сети. Ошибка не теряет данные."),
         ("Демо-режим", "REST эмулируется на устройстве: приложение можно показать и протестировать без бэкенда.")]
ny = 2.24
for t, d in notes:
    dot = box(s, 5.55, ny + 0.11, 0.1, 0.1, fill=LIME, radius=0.5)
    text(s, 5.78, ny, 6.9, 0.28, t, size=12, bold=True)
    text(s, 5.78, ny + 0.33, 6.85, 0.6, d, size=9.5, color=DIM, line_spacing=1.28)
    ny += 1.05

flow = ["Сканирование", "Товар найден", "Операция", "Отправка", "Синхронизировано"]
fx = 0.72
for i, step in enumerate(flow):
    box(s, fx, 6.18, 1.55, 0.42, fill=RGBColor(0x14, 0x1D, 0x10) if i == 2 else RGBColor(0x0E, 0x14, 0x1D),
        line=RGBColor(0x46, 0x5C, 0x1E) if i == 2 else RGBColor(0x1E, 0x26, 0x32), radius=0.14)
    text(s, fx, 6.27, 1.55, 0.24, step, size=8.5,
         color=LIME if i == 2 else DIM, bold=True, align=PP_ALIGN.CENTER)
    if i < 4:
        text(s, fx + 1.55, 6.24, 0.35, 0.3, "→", size=11, color=MUTED, bold=True, align=PP_ALIGN.CENTER)
    fx += 1.9

# ======================================================== 09. Альтернативы
s = new_slide(9, "Альтернативы")
footer(s, "Альтернативы", 9)
kicker(s, 0.65, 0.98, 6.0, "С кем сравниваем")
h2(s, 0.65, 1.34, 10.0, ["Рынок не пустой — и это хорошо"], size=33)
alts = [
    ("наш проект", "СкладСкан", "Мобильный клиент для быстрых операций: камера, офлайн-очередь, понятный интерфейс, открытый код.", "Стоимость: 0 ₽ · запуск: один вечер", True),
    ("1С", "1С:Мобильная касса", "Мобильное приложение экосистемы 1С. Сильная сторона — обмен с уже работающей базой 1С.", "Цена: от ~15 тыс. ₽ + лицензии и настройка", False),
    ("enterprise", "Cleverence MobileSMARTS", "Промышленная платформа складской автоматизации: ТСД, RFID, 1С, сложные сценарии.", "Цена: от ~50 тыс. ₽ за внедрение", False),
    ("учёт", "Гектор:Склад", "Учёт и инвентаризация с быстрым стартом и обменом данными. Ближе к учётной системе, чем к сканеру.", "Цена: модули от ~10 тыс. ₽", False),
    ("retail", "Штрих-М", "Комплексная автоматизация торговли и склада: ПО, оборудование, внедрение и поддержка.", "Цена: по проекту", False),
    ("бесплатно", "Excel и бумага", "Ноль затрат и полная гибкость. Но нет сканирования, истории и синхронизации — только ручной труд.", "Цена: 0 ₽ · цена ошибки — ваша", False),
]
gx, gy = 0.65, 2.18
for i, (tag, name, desc, price, us) in enumerate(alts):
    x = gx + (i % 3) * 4.07
    y = gy + (i // 3) * 2.18
    border = LIME if us else RGBColor(0x1E, 0x26, 0x32)
    box(s, x, y, 3.9, 2.02, fill=RGBColor(0x12, 0x19, 0x0F) if us else RGBColor(0x0E, 0x14, 0x1D),
        line=border, radius=0.055)
    box(s, x + 0.24, y + 0.2, 0.95, 0.26, fill=RGBColor(0x1A, 0x22, 0x10) if us else RGBColor(0x14, 0x1B, 0x26), radius=0.5)
    text(s, x + 0.24, y + 0.235, 0.95, 0.2, tag, size=7.5, color=LIME if us else MUTED, bold=True,
         align=PP_ALIGN.CENTER, caps=False)
    text(s, x + 0.24, y + 0.58, 3.4, 0.3, name, size=13.5, bold=True, color=LIME if us else TEXT)
    text(s, x + 0.24, y + 0.97, 3.42, 0.7, desc, size=9, color=DIM, line_spacing=1.3)
    text(s, x + 0.24, y + 1.68, 3.42, 0.24, price, size=8.5, color=MUTED, bold=True)
text(s, 0.65, 6.55, 12.0, 0.24,
     "Цены — ориентир по открытым прайсам на ноябрь 2026. Точная стоимость зависит от сценариев и внедрения.",
     size=8.5, color=MUTED)

# ======================================================== 10. Таблица сравнения
s = new_slide(10, "Сравнение")
footer(s, "Сравнение", 10)
kicker(s, 0.65, 0.98, 6.0, "Честное сравнение")
h2(s, 0.65, 1.34, 10.0, ["Сильные и слабые места — рядом"], size=32)

rows = [
    ("Цена входа", "0 ₽", "от 15 т.₽", "от 50 т.₽", "от 10 т.₽", "по проекту", "0 ₽"),
    ("Запуск без интегратора", "✓", "—", "—", "~", "—", "✓"),
    ("Сканирование камерой телефона", "✓", "~", "~", "~", "~", "—"),
    ("Офлайн-очередь операций", "✓", "~", "✓", "✓", "~", "—"),
    ("Интеграция с 1С / ERP", "—", "✓", "✓", "~", "✓", "—"),
    ("ТСД, RFID, весы", "—", "~", "✓", "~", "✓", "—"),
    ("Печать этикеток", "—", "~", "✓", "~", "✓", "—"),
    ("Роли, аудит, партии", "—", "✓", "✓", "~", "✓", "—"),
    ("Поддержка и SLA", "—", "✓", "✓", "✓", "✓", "—"),
    ("Открытый код", "✓", "—", "—", "—", "—", "✓"),
    ("Современный интерфейс", "✓", "~", "~", "—", "—", "—"),
]
headers = ["Критерий", "СкладСкан", "1С", "Cleverence", "Гектор", "Штрих-М", "Excel"]
col_x = [0.65, 3.85, 5.45, 6.95, 8.55, 10.15, 11.75]
col_w = [3.2, 1.6, 1.5, 1.6, 1.6, 1.6, 1.1]
top = 2.18
row_h = 0.355
for j, hd in enumerate(headers):
    box(s, col_x[j], top, col_w[j], 0.4, fill=RGBColor(0x18, 0x22, 0x11) if j == 1 else RGBColor(0x13, 0x1A, 0x24),
        line=RGBColor(0x2A, 0x33, 0x40), radius=0.12)
    text(s, col_x[j], top + 0.09, col_w[j], 0.24, hd, size=9 if j else 8.5,
         color=LIME if j == 1 else DIM, bold=True, align=PP_ALIGN.CENTER if j else PP_ALIGN.LEFT)
yy = top + 0.46
for i, row in enumerate(rows):
    if i % 2 == 0:
        box(s, 0.65, yy, 12.18, row_h, fill=RGBColor(0x0C, 0x11, 0x19), radius=0)
    text(s, 0.78, yy + 0.075, 3.0, 0.24, row[0], size=9.5, color=TEXT)
    for j in range(1, 7):
        v = row[j]
        if v == "✓":
            col, txt = LIME if j == 1 else MINT, "✓"
        elif v == "—":
            col, txt = RGBColor(0x50, 0x5B, 0x69), "—"
        elif v == "~":
            col, txt = AMBER, "~"
        else:
            col, txt = (LIME if j == 1 else DIM), v
        text(s, col_x[j], yy + 0.075, col_w[j], 0.24, txt, size=9.5 if len(txt) > 2 else 10,
             color=col, bold=True, align=PP_ALIGN.CENTER)
    yy += row_h
text(s, 0.65, 6.5, 12.0, 0.26,
     [[{"t": "✓ ", "c": LIME, "b": True}, {"t": "есть «из коробки»      ", "c": MUTED},
       {"t": "~ ", "c": AMBER, "b": True}, {"t": "частично или за доплату      ", "c": MUTED},
       {"t": "— ", "c": RGBColor(0x50, 0x5B, 0x69), "b": True}, {"t": "нет", "c": MUTED}]], size=9)

# ======================================================== 11. Где мы сильнее
s = new_slide(11, "Где мы сильнее")
footer(s, "Преимущества", 11)
kicker(s, 0.65, 0.98, 6.0, "Наши преимущества")
h2(s, 0.65, 1.34, 6.6, ["Быстрее начать.", [{"t": "Дешевле владеть.", "c": MUTED}]], size=31)
strs = [("01", "Старт за один вечер", "APK, адрес API и демо-данные. Не нужен интегратор, чтобы увидеть рабочий сценарий."),
        ("02", "Ноль затрат на железо", "Любой Android 8+ с камерой. Не нужно покупать ТСД и лицензию на каждое устройство."),
        ("03", "Учёт не встаёт без сети", "Операция сохраняется локально и уходит на сервер позже. Смена не зависит от Wi-Fi."),
        ("04", "Понятно с первого экрана", "Три действия и крупные цифры. Обучение кладовщика — минуты, а не дни."),
        ("05", "Полный контроль", "Kotlin и Compose, открытый исходник. Можно доработать под свой процесс, а не ждать вендора.")]
sy = 2.35
for n, t, d in strs:
    text(s, 0.65, sy + 0.02, 0.5, 0.24, n, size=10, color=LIME, bold=True)
    text(s, 1.1, sy - 0.03, 6.3, 0.28, t, size=12.5, bold=True)
    text(s, 1.1, sy + 0.3, 6.3, 0.5, d, size=9.5, color=DIM, line_spacing=1.26)
    sy += 0.87
box(s, 8.05, 2.28, 4.63, 4.28, fill=RGBColor(0x11, 0x19, 0x0D), line=RGBColor(0x39, 0x4A, 0x1C), radius=0.05)
stats = [("0 ₽", "за устройство: телефон уже есть у каждого кладовщика"),
         ("≈15 мин", "от установки APK до первой проведённой операции"),
         ("43 МБ", "размер debug-сборки — без тяжёлых SDK и облака")]
sy = 2.6
for big, cap in stats:
    text(s, 8.35, sy, 4.0, 0.62, big, size=34, color=LIME, bold=True)
    text(s, 8.35, sy + 0.62, 4.0, 0.5, cap, size=9.5, color=DIM, line_spacing=1.25)
    sy += 1.28

# ======================================================== 12. Где сильнее они
s = new_slide(12, "Где сильнее они")
footer(s, "Ограничения", 12)
kicker(s, 0.65, 0.98, 6.0, "Без иллюзий", color=CORAL)
h2(s, 0.65, 1.34, 10.0, ["Чего у нас пока нет"], size=33)
lead(s, 0.65, 2.02, 9.4, "Конкуренты выигрывают там, где начинается промышленный склад и корпоративные процессы.", h=0.4)
weaks = [("Интеграции", "Нет готового обмена с 1С и ERP: нужен API или адаптер под конкретную систему."),
         ("Оборудование", "Не управляет ТСД, RFID, весами и принтерами этикеток."),
         ("Складские процессы", "Нет партий, сроков годности, адресного хранения, ролей и аудита действий."),
         ("Масштаб", "Нет централизованного развёртывания и управления сотнями устройств."),
         ("Поддержка", "Нет SLA, сертификации, вендорского внедрения и обучения."),
         ("Зрелость", "Проект новый: меньше сценариев и накопленного опыта, чем у вендоров с историей.")]
gx, gy = 0.65, 2.52
for i, (t, d) in enumerate(weaks):
    x = gx + (i % 3) * 4.07
    y = gy + (i // 3) * 1.5
    box(s, x, y, 3.9, 1.36, fill=RGBColor(0x0E, 0x14, 0x1D), line=RGBColor(0x1E, 0x26, 0x32), radius=0.07)
    box(s, x + 0.24, y + 0.24, 0.1, 0.1, fill=CORAL, radius=0.5)
    text(s, x + 0.44, y + 0.17, 3.2, 0.28, t, size=12, bold=True)
    text(s, x + 0.24, y + 0.55, 3.44, 0.72, d, size=9.5, color=DIM, line_spacing=1.3)
box(s, 0.65, 5.66, 12.03, 0.72, fill=RGBColor(0x14, 0x1D, 0x10), line=RGBColor(0x39, 0x4A, 0x1C), radius=0.09)
text(s, 0.9, 5.78, 11.6, 0.5,
     [[{"t": "Мы не заменяем ERP и не спорим с ТСД. ", "c": DIM}, {"t": "Мы закрываем последнюю милю", "b": True, "c": LIME},
       {"t": " — быструю и честную операцию у полки.", "c": DIM}]], size=11)

# ======================================================== 13. Технологии
s = new_slide(13, "Технологии")
footer(s, "Технологии", 13)
kicker(s, 0.65, 0.98, 6.0, "Под капотом")
h2(s, 0.65, 1.34, 10.0, ["Современный Android-стек"], size=33)
tech = [("Kotlin 2.0 · Coroutines · Flow", "асинхронность и реактивные данные"),
        ("Jetpack Compose · Material 3", "декларативный UI, свои компоненты"),
        ("CameraX · ML Kit Barcode", "сканирование на устройстве"),
        ("Room · KSP · DataStore", "локальная база и настройки"),
        ("Retrofit · OkHttp · Gson", "REST-клиент и логирование"),
        ("WorkManager · NetworkMonitor", "фоновая синхронизация"),
        ("MVVM · ручной DI", "без Hilt — проще собирать"),
        ("Unit-тесты · demo mode", "правила учёта проверяются")]
ty = 2.24
for i, (t, d) in enumerate(tech):
    y = ty + i * 0.5
    if i % 2 == 0:
        box(s, 0.65, y, 5.55, 0.44, fill=RGBColor(0x0C, 0x11, 0x19), radius=0.06)
    text(s, 0.82, y + 0.055, 3.2, 0.24, t, size=10.5, bold=True)
    text(s, 3.95, y + 0.075, 2.15, 0.24, d, size=8.5, color=MUTED, align=PP_ALIGN.RIGHT)

box(s, 6.6, 2.2, 6.08, 1.45, fill=RGBColor(0x0E, 0x14, 0x1D), line=RGBColor(0x1E, 0x26, 0x32), radius=0.05)
kv = [("Сборка", "debug APK · 43 МБ"), ("Минимальная версия", "Android 8.0 · API 26"),
      ("Целевая версия", "Android 14 · API 34"), ("Экраны", "6 · 5 вкладок"),
      ("Тесты", "StockMath · 11 кейсов")]
for i, (k, v) in enumerate(kv):
    y = 2.33 + i * 0.245
    text(s, 6.82, y, 2.6, 0.22, k, size=9, color=MUTED)
    text(s, 9.4, y, 3.1, 0.22, v, size=9, color=TEXT, bold=True, align=PP_ALIGN.RIGHT)

box(s, 6.6, 3.8, 6.08, 1.62, fill=RGBColor(0x0E, 0x14, 0x1D), line=RGBColor(0x1E, 0x26, 0x32), radius=0.05)
text(s, 6.82, 3.94, 5.6, 0.22, "Дизайн-система", size=8.5, color=MUTED, bold=True, caps=True)
text(s, 6.82, 4.2, 5.65, 0.42, "Manrope, тёмный «чернильный» фон, лаймовый акцент. Кастомные графики "
     "и нижняя навигация — без стокового Material-вида.", size=9.5, color=DIM, line_spacing=1.25)
sw_x = 6.82
for c in (0x070A0F, 0x101722, 0xC6FF4A, 0x7BF0B4, 0x63E6FF, 0xFFC24B, 0xFF6B7A):
    box(s, sw_x, 4.98, 0.72, 0.3, fill=RGBColor((c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF), radius=0.2)
    sw_x += 0.8

box(s, 6.6, 5.56, 6.08, 1.12, fill=RGBColor(0x0E, 0x14, 0x1D), line=RGBColor(0x1E, 0x26, 0x32), radius=0.05)
text(s, 6.82, 5.68, 5.6, 0.22, "Сборка из исходников", size=8.5, color=MUTED, bold=True, caps=True)
text(s, 6.82, 5.94, 5.6, 0.24, "./gradlew :app:assembleDebug", size=9.5, color=LIME)
text(s, 6.82, 6.18, 5.6, 0.24, "./gradlew :app:testDebugUnitTest", size=9.5, color=LIME)
text(s, 6.82, 6.42, 5.6, 0.22, "JDK для сборки: 17 · путь без кириллицы", size=8.5, color=MUTED)

# ======================================================== 14. Финал / GitHub
s = new_slide(14, "Исходный код")
footer(s, "СкладСкан · 2026", 14)
kicker(s, 0.65, 1.24, 6.0, "Итог")
h2(s, 0.65, 1.6, 6.4, ["Быстрый учёт", [{"t": "у каждой полки.", "c": MUTED}]], size=34)
lead(s, 0.65, 2.92, 6.1,
     "Приложение собирается, работает без сервера и решает конкретную задачу: сделать складскую "
     "операцию такой же простой, как скан QR-кода.", h=1.0)
box(s, 0.65, 4.15, 6.0, 1.0, fill=RGBColor(0x0E, 0x14, 0x1D), line=RGBColor(0x1E, 0x26, 0x32), radius=0.08)
text(s, 0.88, 4.27, 5.5, 0.22, "Что дальше", size=8.5, color=MUTED, bold=True, caps=True)
text(s, 0.88, 4.53, 5.55, 0.5, "Адаптер к 1С, печать этикеток, роли и права, партии и сроки годности, "
     "централизованное обновление.", size=9.5, color=DIM, line_spacing=1.25)
text(s, 0.65, 5.45, 6.0, 0.4, "Спасибо за внимание", size=18, bold=True, color=LIME)

box(s, 7.15, 1.62, 5.53, 4.3, fill=RGBColor(0x0E, 0x14, 0x1D), line=RGBColor(0x26, 0x2F, 0x3C), radius=0.05)
text(s, 7.45, 1.9, 3.6, 0.22, "GitHub", size=8.5, color=MUTED, bold=True, caps=True)
text(s, 7.45, 2.15, 3.9, 0.28, "Код, инструкция запуска и APK", size=11, bold=True)
# МЕСТО ПОД ССЫЛКУ: замените текст и значение ниже
gh_url = "github.com/username/skladscan"
text(s, 7.45, 2.75, 4.1, 0.4, gh_url, size=17, bold=True, color=TEXT)
text(s, 7.45, 3.25, 4.0, 0.6, "Исходный код, инструкция по сборке и готовый APK.",
     size=9, color=MUTED, line_spacing=1.25)
qr = s.shapes.add_picture(str(ASSETS / "qr-github.png"), Inches(10.72), Inches(4.35), Inches(1.55), Inches(1.55))
text(s, 10.72, 5.95, 1.55, 0.22, "Проект", size=7.5, color=MUTED, bold=True, align=PP_ALIGN.CENTER, caps=True)
text(s, 7.45, 5.3, 3.2, 0.4, "Ссылка на репозиторий — здесь", size=8.5, color=MUTED)

prs.save(OUT)
print(f"saved: {OUT}  slides={len(prs.slides.__iter__.__self__._sldIdLst)}")
