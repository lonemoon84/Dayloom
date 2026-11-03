"""
Builds the home-bar slogan fonts: `res/font/slogan_long_cang.ttf` (Chinese) cut down to the glyphs of `app_slogan`, so the
APK grows by a few KB instead of 5 MB, and `res/font/slogan_parisienne.ttf` (English) copied whole. The app picks one by
the slogan text (see `sloganFont` in `AppRoot.kt`), not by a `font-zh` folder, which Compose does not re-read on a language switch.
Run from the repository root after changing the slogan: `python3 scripts/fonts/subset_slogan.py` (needs `fonttools`).
生成首页标题栏标语用的字体：`res/font/slogan_long_cang.ttf`（中文）只保留 `app_slogan` 用到的字形，APK 只增加几 KB 而不是
5 MB；`res/font/slogan_parisienne.ttf`（英文）整份复制。App 按标语文字选字体（见 `AppRoot.kt` 的 `sloganFont`），不用
`font-zh` 目录，因为切换语言后 Compose 不会重新读取。
改了标语后在仓库根目录运行：`python3 scripts/fonts/subset_slogan.py`（需要 `fonttools`）。

Both fonts are under the SIL Open Font License 1.1; the licence text is in `res/raw/third_party_licenses.txt`. Parisienne
has the Reserved Font Name "Parisienne", which a modified (subset) copy may not carry, so it ships unmodified; it is only
about 60 KB. Long Cang has no Reserved Font Name, so subsetting it is allowed.
两款字体都采用 SIL Open Font License 1.1，许可证全文在 `res/raw/third_party_licenses.txt`。Parisienne 有保留字体名
"Parisienne"，修改（裁剪）后的版本不能再用这个名字，所以原样打包，它本身只有约 60 KB。龙藏体没有保留字体名，可以裁剪。
"""
import io
import os
import re
import urllib.request
import xml.etree.ElementTree as ET

from fontTools import subset
from fontTools.ttLib import TTFont

# Pinned so a rebuild gives the same glyphs. / 固定提交，重新生成时字形不变。
GOOGLE_FONTS = "https://raw.githubusercontent.com/google/fonts/bd8f81ddb5c74d5c8897b36ad88b440266245103/ofl/"
RES = "app/src/main/res/"


def download(source):
    return urllib.request.urlopen(GOOGLE_FONTS + source).read()


def slogan(strings_path):
    for node in ET.parse(RES + strings_path).getroot().iter("string"):
        if node.get("name") == "app_slogan":
            return re.sub(r"\\(.)", r"\1", node.text or "")
    raise SystemExit(f"app_slogan missing in {strings_path}")


def save(data, out):
    os.makedirs(os.path.dirname(RES + out), exist_ok=True)
    with open(RES + out, "wb") as f:
        f.write(data)
    print(f"{out}: {os.path.getsize(RES + out)} bytes")


def subsetted(source, text):
    font = TTFont(io.BytesIO(download(source)))
    options = subset.Options()
    options.layout_features = ["kern", "liga", "calt"]
    options.name_IDs = ["*"]
    subsetter = subset.Subsetter(options)
    subsetter.populate(text=text)
    subsetter.subset(font)
    buffer = io.BytesIO()
    font.save(buffer)
    return buffer.getvalue()


# The ellipsis is drawn when the slogan is cut. / 标语被截断时要画省略号。
save(subsetted("longcang/LongCang-Regular.ttf", slogan("values-zh/strings.xml") + "…"), "font/slogan_long_cang.ttf")
save(download("parisienne/Parisienne-Regular.ttf"), "font/slogan_parisienne.ttf")
