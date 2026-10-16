execute if score #IN:arctan2.X main_score matches ..599 unless function physical_vehicles:zzz/vehicle/at_parts/wheel/sounds/chance run return fail

scoreboard players operation #pitch main_score = #IN:arctan2.X main_score
scoreboard players remove #pitch main_score 400
scoreboard players operation #pitch main_score /= #20 const
execute store result storage midwut:main macros.pitch float 0.001 run scoreboard players add #pitch main_score 950
#scoreboard players set @s phys_veh.normalize_timer 4800
#scoreboard players operation @s phys_veh.normalize_timer /= #pitch main_score
execute if score @s phys_veh.ground_material_type matches 1 run function physical_vehicles:zzz/vehicle/at_parts/wheel/sounds/playsound_hard with storage midwut:main macros
execute if score @s phys_veh.ground_material_type matches 2 run function physical_vehicles:zzz/vehicle/at_parts/wheel/sounds/playsound_rough with storage midwut:main macros
execute if score @s phys_veh.ground_material_type matches 3 run function physical_vehicles:zzz/vehicle/at_parts/wheel/sounds/playsound_loose with storage midwut:main macros
execute if score @s phys_veh.ground_material_type matches 4 run function physical_vehicles:zzz/vehicle/at_parts/wheel/sounds/playsound_friable with storage midwut:main macros