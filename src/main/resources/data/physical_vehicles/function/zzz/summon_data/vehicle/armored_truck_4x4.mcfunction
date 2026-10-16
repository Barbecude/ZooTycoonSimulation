execute if entity @s[type=minecraft:player] run return fail
execute if data entity @s data.phys_veh."minecraft:custom_model_data"{strings:["black"]} run function physical_vehicles:summon/armored_truck_4x4/black
execute if data entity @s data.phys_veh."minecraft:custom_model_data"{strings:["gray-red"]} run function physical_vehicles:summon/armored_truck_4x4/gray-red
