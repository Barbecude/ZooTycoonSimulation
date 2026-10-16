execute store result storage midwut:main macros.x float 0.0001 run scoreboard players get @s phys_veh.m02
execute store result storage midwut:main macros.y float 0.0001 run scoreboard players get @s phys_veh.m12
execute store result storage midwut:main macros.z float 0.0001 run scoreboard players get @s phys_veh.m22
execute as 0000051d-0000-0fb7-0000-001500000014 run function physical_vehicles:zzz/vehicle/at_parts/order/entity_rotation_marker
#scoreboard players operation #y main_score %= #360000 const
#scoreboard players operation #x main_score %= #360000 const
scoreboard players operation @s phys_veh.entity_rotation.y = #y main_score
scoreboard players operation @s phys_veh.entity_rotation.x = #x main_score