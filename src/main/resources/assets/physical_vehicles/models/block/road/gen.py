from os.path import abspath, basename
from os import walk,rename,mkdir
import shutil
import json
dir = abspath(__file__).replace(f"\\{basename(__file__)}","")

materials = [
"black_concrete_powder",
"coarse_dirt",
"cobblestone",
"gray_concrete_powder",
"light_gray_concrete_powder",
"red_concrete_powder",
"smooth_stone",
"white_concrete_powder",
"yellow_concrete_powder"
]

data = '{"parent": "physical_vehicles:block/road/model/<HEIGHT>","textures": {"texture": "minecraft:block/<MATERIAL>"}}'

for material in materials:
    material_dir = f"{dir}/{material}"
    shutil.rmtree(material_dir,ignore_errors=True)
    mkdir(material_dir)
    for height in range(1,16):
        with open(f"{material_dir}/{height}.json","w",encoding="utf-8") as file:
            data_material = eval(data.replace("<HEIGHT>",str(height)).replace("<MATERIAL>",material))
            json.dump(data_material, file, indent=4)



#for dirpath, dirnames, filenames in walk(dir):
#    if len(filenames) == 1: continue
#    for filename in filenames[::-1]:
#        if filename.endswith(".json"):
#            rename(f"{dirpath}/{filename}",f"{dirpath}/{str(int(filename.split('.')[0])+1)}.json")
    