from os.path import abspath, basename
from os import walk,rename
dir = abspath(__file__).replace(f"\\{basename(__file__)}","")

for dirpath, dirnames, filenames in walk(dir):
    if len(filenames) == 1: continue
    for filename in filenames[::-1]:
        if filename.endswith(".json"):
            rename(f"{dirpath}/{filename}",f"{dirpath}/{str(int(filename.split('.')[0])+1)}.json")
    