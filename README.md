# Hanzi Learner — learn to read and write the 1000 most frequent Chinese characters

A single-page app written in **ClojureScript + Reagent only** (no other
front-end framework or library). The compiled page is `public/index.html`
plus `public/js/main.js`. There is no server and no network call at run
time: all data is bundled, and all user data stays in the browser.

## What it teaches, and how

Characters are taught **in MTSU frequency order, at most 10 at a time**.
The current batch is the first 10 characters in rank order that are not
yet Learned. When all 10 are Learned, the next 10 unlock automatically.

Each character has one of three states, per account:

- **New** — never studied.
- **Learning** — studied, or back from a failed review, but has not yet
  passed both a reading test and a writing test.
- **Learned** — passed a reading test *and* a writing test.

Flow:

1. **Learn** — large character, pinyin with tone marks, MTSU numeric
   pinyin, definition, MTSU English gloss, radical, decomposition,
   stroke count, rank and frequency. A "Play stroke order" animation
   walks a dot along each stroke's median, in order. A "Hear it"
   button uses the browser's built-in speech synthesis (zh-CN).
2. **Practise writing** — draw on the writing pad, one stroke at a time.
   The faint shape is a hint; the orange dot is where the next stroke
   starts. A stroke counts when its direction is generally right
   (see "How stroke checking works" below). Wrong strokes can be
   retried as often as you like in practice.
3. **Read quiz** — for characters in the current batch that are in
   Learning: pick the pinyin and the meaning from multiple-choice
   options (distractors are drawn from other top-1000 characters).
4. **Write quiz** — write from pinyin + meaning only, no template.
   Each stroke is scored once, in order, then the next stroke is
   attempted. A pass needs at least 80% of strokes correct
   (100% for 1–2 stroke characters).
5. **Review** — Learned characters come back on a spaced schedule:
   1, 3, 7, 14, 30, then 60 days after the previous pass. A review
   tests reading and then writing; passing pushes the next review
   out, failing either returns the character to Learning, so it
   re-enters the current batch.
6. **Browse** — a searchable reference list of all 1000, in rank
   order. Browsing does not change the teaching order.

## How stroke checking works (relaxed, as requested)

The expected stroke is the median (centreline) from
`graphics.txt`, converted to display coordinates (see below).
A user stroke passes when:

- it is a short, dot-like mark close to the median's centre, or
- its overall direction (first few points → last few points, so a
  shaky start/finish does not decide it) is within about 70° of the
  median's direction **and** it is loosely near the median
  (centroid, start or end within 450 units on the 1024 grid).

Stroke order is enforced by scoring strokes in order; stroke count
is enforced by the total in the writing test. Exact shape, size and
position are deliberately *not* required.

## Accounts and localStorage

All user account data is saved to **this browser's localStorage**,
under the single key `hanzi-learner-v1`, as one EDN string. Nothing
is sent anywhere. Accounts are just names (no server, so no real
password would be meaningful); each account has its own progress,
and you can switch, delete, or download a backup (EDN) from the
Accounts tab. Clearing this site's browser data deletes the accounts.

Stored shape (per account, per character):

```clojure
{:state "learning" :read-pass 1 :write-pass 0
 :reviews 0 :level 0 :learned-at 0 :next-review 0 :last-seen 0}
```

Absence of a progress entry means **New**.

## Data sources and formats

### 1. Top 1000 characters — MTSU / Jun Da

Source: <https://lingua.mtsu.edu/chinese-computing/statistics/char/list.php>
(the page is GB2312-encoded; the extraction decodes it as GBK).

Each row in its `<pre>` block is:

```
rank TAB character TAB raw-frequency TAB cumulative-% TAB pinyin TAB English-gloss
```

Pinyin there uses numeric tones (`zhi1`, `bu4/bu2`) and some rows
have no English gloss. The first 1000 rows are used, in that order:
#1 之 … #1000 专. All 1000 were verified to exist in both
makemeahanzi files below, with a matching SVG file each.

### 2. Make Me a Hanzi — skishore/makemeahanzi

Source: <https://github.com/skishore/makemeahanzi>
Files used: `dictionary.txt`, `graphics.txt`, and the `svgs/` and
`svgs-still/` folders. The full repo was fetched during development;
`data-raw/` now keeps `dictionary.txt`, `graphics.txt`, and only the
top-1000 SVGs (`data-raw/top1000-svgs/svgs/` and
`data-raw/top1000-svgs/svgs-still/`), because the app itself only
ships the extracted data, see below.

- `dictionary.txt` — one JSON object per line:
  `character`, `definition`, `pinyin` (tone-marked, e.g. `["zhī"]`),
  `decomposition` (e.g. `⿱丶？`), `radical`, optional `etymology`,
  `matches`.
- `graphics.txt` — one JSON object per line:
  `character`, `strokes` (SVG path strings, in order),
  `medians` (one centreline per stroke, `[[x, y], …]`).
  `strokes[i]` and `medians[i]` are the same stroke.
- `svgs/<decimal-codepoint>.svg` is the animated version and
  `svgs-still/<decimal-codepoint>-still.svg` the still version of
  the same strokes. Example: 一 is U+4E00 = 19968, so
  `svgs/19968.svg`.

**Coordinate note (important):** makemeahanzi coordinates have y
pointing up. Its SVG files render the paths inside
`<g transform="scale(1,-1) translate(0,-900)">`, i.e. displayed
y = 900 − y. This app does the same: stroke paths are drawn inside
that transform, and medians are flipped with `display-median` in
`src/hanzi/strokes.cljs`. Without the flip, characters would appear
upside down.

`scripts/extract_top1000.py` documents and reproduces the
extraction (it writes the intermediate `data-raw/top1000.json`);
`src/hanzi/data.cljs` is the resulting top-1000 dataset bundled
into the app (2.4 MB: 1000 characters, 7,824 strokes in total,
about 7.8 strokes per character on average).

## Build and run

Verified in this workspace. Requirements:

- Node.js 24 and npm (already in the container; `node -v` → 24.22.1,
  `npm -v` → 10.9.4)
- Java 21 or newer — Java 17 is **not** enough: the Closure Compiler
  jar that shadow-cljs 3.5.11 pulls in was compiled for Java 21
  (class file version 65), and Java 17 fails with
  `UnsupportedClassVersionError`.
- shadow-cljs 3.5.11 and React 18.2.0 (both in `package.json`)

From this directory, in a normal environment with Maven/Clojars
reachable:

```sh
npm install          # shadow-cljs, react, react-dom
npx shadow-cljs release app
```

That writes `public/js/main.js` (4.1 MB, 0 warnings on the verified
build). Serve the `public/` directory with any static server, for
example:

```sh
python3 -m http.server --directory public 8000
# open http://localhost:8000/
```

For development with live reload instead:

```sh
npx shadow-cljs watch app
```

### Java in this workspace (persistent, no apt needed)

This container is Ubuntu 24.04, not Debian, and system packages
installed with apt do **not** survive a VM replacement — only the
home directory does. So Java is installed as a tarball under
`~/workspace/tools/`, which does survive:

```sh
# Temurin 21 LTS, downloaded once from Adoptium
~/workspace/tools/jdk-21.0.13+9/bin/java -version
# openjdk version "21.0.13" 2026-10-20 LTS
```

An apt install was also tried earlier (Ubuntu mirror switched to
`us.archive.ubuntu.com`, openjdk-17 installed successfully), but it
was lost on a VM replacement and, in any case, Java 17 cannot run
the Closure Compiler jar above.

### Offline Maven workaround used here

In this container, Java cannot tunnel through the egress proxy
(Java's `HttpURLConnection` / Maven wagon fail on the proxy's
CONNECT response, while curl and Python through the same proxy
work fine), so `npx shadow-cljs release app` fails while trying
to download its Maven/Clojars dependencies. The verified build
therefore pre-downloads those dependencies with a proxy-capable
resolver and then runs shadow-cljs directly, skipping Maven
resolution:

```sh
# 1. Pre-download shadow-cljs + reagent dependencies into ~/.m2
#    (also persists in the home directory)
python3 scripts/resolve_maven.py

# 2. Run shadow-cljs's compiler directly, with the downloaded jars
export JAVA_HOME=~/workspace/tools/jdk-21.0.13+9
export PATH=$JAVA_HOME/bin:$PATH
CP=$(find ~/.m2/repository -name '*.jar' ! -name '*-aot.jar' \
       ! -path '*thheller/shadow-cljs/*' | tr '\n' ':')
CP="$CP$HOME/.m2/repository/thheller/shadow-cljs/3.5.11/shadow-cljs-3.5.11-aot.jar:src"
java -cp "$CP" clojure.main -m shadow.cljs.devtools.cli release app
# [:app] Build completed. (78 files, 2 compiled, 0 warnings, 44.94s)
```

One source fix was found by that build and is already in the
tree: `src/hanzi/storage.cljs` originally had its docstring after
the `:require` clause, which is an invalid `ns` form; it now has
the docstring first, then `:require`.

## Project layout

```
public/index.html          single page, styles, <div id="app">
public/js/main.js          compiled output (generated, not in git)
src/hanzi/data.cljs        top 1000 characters + strokes + medians
src/hanzi/strokes.cljs     median geometry + relaxed stroke scoring
src/hanzi/storage.cljs     localStorage schema + spaced-review intervals
src/hanzi/ui.cljs          stroke animation + writing pad (Reagent)
src/hanzi/core.cljs        accounts, batches, quizzes, review, views
shadow-cljs.edn            shadow-cljs build, Reagent 1.2.0 dependency
scripts/extract_top1000.py data extraction, documented
scripts/resolve_maven.py   proxy-capable Maven pre-download (offline build)
data-raw/                  fetched sources (development only)
```

Only ClojureScript and Reagent are used for the app itself; the
browser's built-in speech synthesis is used for pronunciation, and
no other front-end library is involved.
