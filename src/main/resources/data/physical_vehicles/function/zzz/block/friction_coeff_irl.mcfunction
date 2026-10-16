execute if block ~ ~ ~ #physical_vehicles:sound_material/road run return 95
execute if block ~ ~ ~ minecraft:snow run return 85
execute if block ~ ~ ~ #minecraft:planks run return 85
execute if block ~ ~ ~ #minecraft:slabs run return 85
execute if block ~ ~ ~ minecraft:soul_campfire{Items:[{components:{"minecraft:custom_data":{phys_veh.road:true}}}]} run return 95
execute if block ~ ~ ~ minecraft:soul_campfire{Items:[{components:{"minecraft:custom_data":{phys_veh.vertical_slab:true}}}]} run return 95
execute if block ~ ~ ~ minecraft:soul_campfire run return 80

