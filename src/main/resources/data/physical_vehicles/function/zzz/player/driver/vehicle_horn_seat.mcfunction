execute on vehicle run scoreboard players operation #id_test main_score = @s phys_veh.id.vehicle
execute if entity @e[type=minecraft:text_display,tag=phys_veh.main,tag=!phys_veh.have_horn_sound,limit=1,predicate=physical_vehicles:same_scores/vehicle_id,x=0] run return fail
execute as @e[type=minecraft:text_display,tag=phys_veh.main,limit=1,predicate=physical_vehicles:same_scores/vehicle_id,x=0] run data modify storage midwut:main macros.sound set from entity @s data.phys_veh.sounds[{id:"horn"}]
execute store result score #cooldown main_score run time query gametime
execute positioned as @s run function physical_vehicles:zzz/player/driver/vehicle_horn_engine with storage midwut:main macros.sound
scoreboard players operation @s phys_veh.horn_sound_timer = #cooldown main_score