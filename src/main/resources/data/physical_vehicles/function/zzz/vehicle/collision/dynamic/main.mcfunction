scoreboard players operation #collision_radius_0 main_score = @s phys_veh.collision_box.radius

execute store result storage midwut:main macros.vx float 0.00005 run scoreboard players get @s phys_veh.velocity.x
execute store result storage midwut:main macros.vy float 0.00005 run scoreboard players get @s phys_veh.velocity.y
execute store result storage midwut:main macros.vz float 0.00005 run scoreboard players get @s phys_veh.velocity.z

function physical_vehicles:zzz/vehicle/collision/dynamic/radius_check_velosity_macro with storage midwut:main macros

execute if entity @e[type=minecraft:text_display,tag=phys_veh.main,tag=phys_veh.collision_in_radius,tag=!phys_veh.selected_part,limit=1,distance=..96] run function physical_vehicles:zzz/vehicle/collision/dynamic/main_data