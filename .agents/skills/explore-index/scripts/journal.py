#!/usr/bin/env python3
import sys
import os

if len(sys.argv) < 4:
    print("usage: journal.py <label> <kind> <target> [note]", file=sys.stderr)
    print("kinds: ABSENT REGION RULE REAL", file=sys.stderr)
    sys.exit(2)

label = sys.argv[1].replace('|', ' ').strip()
kind = sys.argv[2].strip()
target = sys.argv[3].strip()
note = sys.argv[4].replace('|', '/').strip() if len(sys.argv) > 4 else ""

if kind not in ["ABSENT", "REGION", "RULE", "REAL"]:
    print(f"invalid kind: {kind} (expected ABSENT|REGION|RULE|REAL)", file=sys.stderr)
    sys.exit(2)

root = os.environ.get("EXPLORE_INDEX_ROOT", ".")
journal_path = os.path.join(root, ".explore", "journal.md")
os.makedirs(os.path.dirname(journal_path), exist_ok=True)

with open(journal_path, "a", encoding="utf-8") as f:
    f.write(f"{label} :: {kind} :: {target} :: {note}\n")

print(f"journalled: {label} ({kind})")
