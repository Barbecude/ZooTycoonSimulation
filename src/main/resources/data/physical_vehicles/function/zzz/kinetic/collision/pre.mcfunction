scoreboard players operation #main.velocity.x main_score = @s phys_veh.velocity.x
scoreboard players operation #main.velocity.y main_score = @s phys_veh.velocity.y
scoreboard players operation #main.velocity.z main_score = @s phys_veh.velocity.z

execute if entity @e[type=minecraft:text_display,tag=phys_veh.main,tag=!phys_veh.selected_part,limit=1,distance=..96] run function physical_vehicles:zzz/vehicle/collision/dynamic/main

data modify storage midwut:main collision set from storage midwut:main Data.data.phys_veh.collisions
function physical_vehicles:zzz/vehicle/collision/vertices/loop_main

execute if entity @s[tag=phys_veh.dynamic_collision_check] run function physical_vehicles:zzz/kinetic/collision/dynamic/remove_tags