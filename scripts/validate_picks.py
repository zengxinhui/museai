import re, sys
sys.path.insert(0, __file__.rsplit("/", 1)[0])
from picks_extra import EXTRA

keats = [l.strip() for l in open("scripts/keats500.txt", encoding="utf-8") if l.strip()]
keats_set = set(keats)
punct = set("，。？！、；：")
errors = []
for kind, text, pinyin, meaning in EXTRA:
    chars = [c for c in text if c not in punct]
    missing = [c for c in chars if c not in keats_set]
    if missing:
        errors.append(f"NOT IN TOP500 {text}: {missing}")
    toks = re.sub(r"[,?.]", "", pinyin).split()
    if len(toks) != len(chars):
        errors.append(f"PINYIN COUNT {text}: {pinyin} -> {len(toks)} vs {len(chars)}")
    if kind not in ("phrase", "sentence"):
        errors.append(f"BAD KIND {text}: {kind}")
# duplicates within EXTRA and vs existing bank
import pathlib
existing = pathlib.Path("src/hanzi/picks.cljs").read_text(encoding="utf-8")
existing_texts = set(re.findall(r':text "([^"]+)"', existing))
seen = set()
for kind, text, pinyin, meaning in EXTRA:
    if text in existing_texts:
        errors.append(f"DUPLICATE OF EXISTING {text}")
    if text in seen:
        errors.append(f"DUPLICATE IN EXTRA {text}")
    seen.add(text)
print(f"entries: {len(EXTRA)}")
print(f"errors: {len(errors)}")
for e in errors:
    print(e)
# coverage
all_texts = existing_texts | seen
covered = set(c for t in all_texts for c in t if c not in punct)
print(f"distinct chars covered: {len(covered)}/500")
for n in (50, 100, 200, 300, 500):
    sub = keats[:n]
    cov = sum(1 for c in sub if c in covered)
    print(f"first {n}: {cov}/{n} {cov/n:.0%}")
print("still uncovered:", "".join(c for c in keats if c not in covered))
