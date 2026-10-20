#!/usr/bin/env python3
import sys
import os

cap = 40
args = sys.argv[1:]
if "--cap" in args:
    idx = args.index("--cap")
    if idx + 1 < len(args):
        cap = int(args[idx + 1])

root = os.environ.get("EXPLORE_INDEX_ROOT", ".")
journal_path = os.path.join(root, ".explore", "journal.md")
out_path = os.path.join(root, ".explore", "INDEX.md")

if not os.path.isfile(journal_path):
    print(f"no journal at {journal_path}", file=sys.stderr)
    sys.exit(1)

seen = {}
with open(journal_path, "r", encoding="utf-8") as f:
    for line in f:
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        parts = line.split(" :: ")
        if len(parts) >= 3:
            key = parts[0].strip()
            seen[key] = line

buckets = {
    "ABSENT": ("Absent — proven not to exist", "Answer is no. Stop searching; do not retry synonyms."),
    "REGION": ("Regions — a concept lives here", "Open the directory; already narrowed."),
    "RULE": ("Rules — how this repo works", "Follow it before writing code here."),
    "REAL": ("Real paths — repo map missed these", "Read the file.")
}

sorted_entries = sorted(seen.values(), key=lambda x: x.split(" :: ")[0].lower())

os.makedirs(os.path.dirname(out_path), exist_ok=True)
with open(out_path, "w", encoding="utf-8") as f:
    f.write("# Explore Index\n\n")
    f.write("Read before any broad search. Last write wins. Rebuild: python .agents/skills/explore-index/scripts/reindex.py\n")
    f.write("Validate: python .agents/skills/explore-index/scripts/verify.py\n")

    for kind in ["ABSENT", "REGION", "RULE", "REAL"]:
        title, desc = buckets[kind]
        rows = [e for e in sorted_entries if f" :: {kind} :: " in e]
        if rows:
            f.write(f"\n## {title}\n\n{desc}\n\n")
            for r in rows[:cap]:
                f.write(f"{r}\n")

    f.write("\n---\n")
    f.write(f"entries: {len(seen)} journalled\n")
    f.write(f"cap: {cap} per bucket\n")

print(f"reindexed -> {out_path}")
