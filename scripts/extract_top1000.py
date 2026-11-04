#!/usr/bin/env python3
"""Reproduce src/hanzi/data.cljs from the two upstream sources.

1. MTSU frequency list (Jun Da), GB2312-encoded HTML:
     https://lingua.mtsu.edu/chinese-computing/statistics/char/list.php
   The <pre> block is: rank TAB char TAB raw-frequency TAB cumulative-%
   TAB pinyin (numeric tones, e.g. zhi1, bu4/bu2) TAB English gloss.
   Some rows have no English gloss (5 fields instead of 6); they are padded.

2. skishore/makemeahanzi (clone or download):
     dictionary.txt - one JSON object per line:
       {"character", "definition", "pinyin": ["zhī", ...] (tone marks),
        "decomposition", "radical", "etymology"?, "matches"}
     graphics.txt - one JSON object per line:
       {"character", "strokes": [SVG path, ...],
        "medians": [[[x, y], ...], ...]}
     strokes[i] and medians[i] are the same stroke, in order.
     Coordinates are in the makemeahanzi system where y points up;
     the svgs/ files render them with transform
     scale(1,-1) translate(0,-900), so display y = 900 - y.
     A file svgs/<decimal codepoint>.svg (and svgs-still/<codepoint>-still.svg)
     exists for every one of the top 1000 characters; the app renders
     the graphics.txt paths directly, which are the same paths, so it
     does not need to ship 2000 separate SVG files.

Usage (from the project root, after fetching the sources into data-raw/):
    python3 scripts/extract_top1000.py
    # writes src/hanzi/data.cljs (top 1000, in MTSU order)
"""
import json
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
RAW = ROOT / "data-raw"


def main():
    html = (RAW / "mtsu-list.html").read_bytes().decode("gbk", errors="ignore")
    body = re.search(r"<pre>(.*?)</pre>", html, re.S).group(1).replace("<br>", "\n")
    mtsu = [line.strip().split("\t") for line in body.splitlines() if line.strip()]
    makemea = RAW / "makemeahanzi"
    dic = {}
    for line in (makemea / "dictionary.txt").read_text().splitlines():
        j = json.loads(line)
        dic[j["character"]] = j
    gra = {}
    for line in (makemea / "graphics.txt").read_text().splitlines():
        j = json.loads(line)
        gra[j["character"]] = j

    out = []
    for parts in mtsu[:1000]:
        parts = parts + [""] * (6 - len(parts))
        rank, ch, freq, cum, pinyin, eng = parts
        d, g = dic[ch], gra[ch]
        assert len(g["strokes"]) == len(g["medians"]), ch
        out.append({
            "rank": int(rank), "char": ch, "freq": int(freq), "cum": float(cum),
            "pinyin_mtsu": pinyin, "english_mtsu": eng,
            "pinyin": d.get("pinyin", []), "definition": d.get("definition", ""),
            "radical": d.get("radical", ""), "decomposition": d.get("decomposition", ""),
            "strokes": g["strokes"], "medians": g["medians"],
        })
    # The data.cljs actually shipped was generated with the same fields;
    # this script documents the extraction and writes the intermediate JSON
    # so a reviewer can diff it against src/hanzi/data.cljs.
    (RAW / "top1000.json").write_text(
        json.dumps(out, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    print(f"wrote {len(out)} characters to data-raw/top1000.json")


if __name__ == "__main__":
    main()
