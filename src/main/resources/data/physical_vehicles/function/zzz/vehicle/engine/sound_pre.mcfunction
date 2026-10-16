data modify storage midwut:main macros.sound set from storage midwut:main Data.data.phys_veh.sounds[{id:"engine"}]
execute store result score #base_rpm main_score run data get storage midwut:main macros.sound.base_rpm
execute store result score #base_volume main_score run data get storage midwut:main macros.sound.base_volume 100
execute store result score #multiplier main_score run data get storage midwut:main macros.sound.multiplier 1
execute store result score @s phys_veh.engine_sound_timer run data get storage midwut:main macros.sound.duration 100

scoreboard players operation #mul main_score = @s phys_veh.engine_rpm
scoreboard players operation #mul main_score -= #base_rpm main_score
scoreboard players operation #mul2 main_score = #mul main_score
scoreboard players operation #mul main_score /= #multiplier main_score
scoreboard players operation #mul2 main_score /= #multiplier main_score
scoreboard players add #mul main_score 100
execute store result storage midwut:main macros.sound.pitch float 0.01 run scoreboard players add #mul2 main_score 100
execute store result storage midwut:main macros.sound.volume float 0.0001 run scoreboard players operation #base_volume main_score *= #mul main_score
execute positioned as @e[type=minecraft:text_display,tag=phys_veh.hp,tag=phys_veh.selected_part,distance=..96] run function physical_vehicles:zzz/vehicle/engine/sound_macro with storage midwut:main macros.sound

scoreboard players operation @s phys_veh.engine_sound_timer /= #mul2 main_score
execute if score #mul2 main_score matches ..99 run scoreboard players remove @s phys_veh.engine_sound_timer 1
execute if score #mul2 main_score matches 1.. run scoreboard players add @s phys_veh.engine_sound_timer 1

