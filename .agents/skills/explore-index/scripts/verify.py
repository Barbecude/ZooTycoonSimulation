#!/usr/bin/env python3
import sys
import os

prune = "--prune" in sys.argv
root = os.environ.get("EXPLORE_INDEX_ROOT", ".")
index_path = os.path.join(root, ".explore", "INDEX.md")

if not os.path.isfile(index_path):
    print(f"no index at {index_path}", file=sys.stderr)
    sys.exit(1)

ok = 0
bad = 0
dead_list = []
malformed = []

with open(index_path, "r", encoding="utf-8") as f:
    lines = [line.rstrip("\r\n") for line in f]

clean_lines = []
for line in lines:
    if line.startswith("#") or line.startswith("---") or not line.strip():
        clean_lines.append(line)
        continue

    is_row = any(f" :: {k} :: " in line for k in ["ABSENT", "REGION", "RULE", "REAL"])
    if not is_row:
        clean_lines.append(line)
        continue

    parts = line.split(" :: ")
    if len(parts) != 4:
        malformed.append(line)
        continue

    label, kind, target, note = parts
    label = label.strip()
    kind = kind.strip()
    target = target.strip()

    is_ok = True
    if kind in ["ABSENT", "REGION", "RULE"]:
        if kind == "REGION":
            full_target = os.path.join(root, target)
            if not os.path.exists(full_target):
                is_ok = False
        else:
            is_ok = True
    elif kind == "REAL":
        full_target = os.path.join(root, target)
        if not os.path.exists(full_target):
            is_ok = False

    if is_ok:
        ok += 1
        clean_lines.append(line)
    else:
        bad += 1
        dead_list.append((label, line))

print(f"verify: {ok} live, {bad} dead")

if malformed:
    print("verify: MALFORMED rows -- fix the journal:", file=sys.stderr)
    for m in malformed:
        print(f"  {m}", file=sys.stderr)
    sys.exit(3)

if prune and bad > 0:
    dead_labels = {d[0] for d in dead_list}
    final_lines = [l for l in clean_lines if not any(l.startswith(f"{lbl} :: ") for lbl in dead_labels)]
    with open(index_path, "w", encoding="utf-8") as f:
        f.write("\n".join(final_lines) + "\n")
    print(f"pruned {bad} dead row(s)")
