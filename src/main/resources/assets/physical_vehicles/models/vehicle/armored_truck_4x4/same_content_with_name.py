
cus_dir = "D:/Minecraft/home/Fabric 1.21.8/resourcepacks/Physical-Vehicles_RP_v0-1-0_1-21-8/assets/physical_vehicles/models/vehicle/armored_truck_4x4/skins/black/wheel_right"
content = '{"parent": "physical_vehicles:vehicle/armored_truck_4x4/wheel_right/<NAME>","textures": {"skin": "physical_vehicles:vehicle/armored_truck_4x4/black"}}'


from os import walk
import json
for dirpath, dirnames, filenames in walk(cus_dir):
    for filename in filenames:
        with open(f"{dirpath}/{filename}","w",encoding="utf-8") as file:
            data = eval(content.replace("<NAME>",'.'.join(filename.split('.')[:-1])))
            json.dump(data, file, indent=4)