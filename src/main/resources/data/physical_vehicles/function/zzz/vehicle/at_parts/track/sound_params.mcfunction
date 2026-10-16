data modify storage midwut:main macros.sound set from storage midwut:main Data.data.phys_veh.sounds[{id:"track"}]
scoreboard players set #spin main_score 0
scoreboard players set #spin2 main_score 0
scoreboard players operation #spin main_score > @e[type=#physical_vehicles:refers_to_vehicle/track,tag=phys_veh.track,tag=phys_veh.selected_part,x=0] phys_veh.spin.x
scoreboard players operation #spin2 main_score < @e[type=#physical_vehicles:refers_to_vehicle/track,tag=phys_veh.track,tag=phys_veh.selected_part,x=0] phys_veh.spin.x
scoreboard players operation #spin2 main_score *= #-1 const
execute if score #spin2 main_score > #spin main_score run scoreboard players operation #spin main_score = #spin2 main_score
scoreboard players operation #spin main_score /= #100 const
execute if score #spin main_score matches ..-1 run scoreboard players operation #spin main_score *= #-1 const
execute store result score #min_spin main_score run data get storage midwut:main macros.sound.min_spin 100
execute if score #spin main_score < #min_spin main_score run return run execute store result score @s phys_veh.sound_timer.track run data get storage midwut:main macros.sound.duration 1
execute store result score #base_spin main_score run data get storage midwut:main macros.sound.base_spin 100
execute store result score #base_volume main_score run data get storage midwut:main macros.sound.base_volume 100
execute store result score #multiplier main_score run data get storage midwut:main macros.sound.multiplier 1
execute store result score #min_pitch main_score run data get storage midwut:main macros.sound.min_pitch 100
execute store result score #max_pitch main_score run data get storage midwut:main macros.sound.max_pitch 100
execute store result score @s phys_veh.sound_timer.track run data get storage midwut:main macros.sound.duration 100

scoreboard players operation #mul main_score = #spin main_score
scoreboard players operation #mul main_score -= #base_spin main_score
scoreboard players operation #mul2 main_score = #mul main_score
scoreboard players operation #mul main_score /= #multiplier main_score
scoreboard players operation #mul2 main_score /= #multiplier main_score
scoreboard players add #mul main_score 100
scoreboard players add #mul2 main_score 100
scoreboard players operation #mul2 main_score < #max_pitch main_score
scoreboard players operation #mul2 main_score > #min_pitch main_score
execute store result storage midwut:main macros.sound.pitch float 0.01 run scoreboard players get #mul2 main_score
execute store result storage midwut:main macros.sound.volume float 0.0001 run scoreboard players operation #base_volume main_score *= #mul main_score
function physical_vehicles:zzz/vehicle/at_parts/track/sound_macro with storage midwut:main macros.sound

scoreboard players operation @s phys_veh.sound_timer.track /= #mul2 main_score
execute if score #mul2 main_score matches ..99 run scoreboard players remove @s phys_veh.sound_timer.track 1
execute if score #mul2 main_score matches 1.. run scoreboard players add @s phys_veh.sound_timer.track 1
