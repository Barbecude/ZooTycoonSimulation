scoreboard players set #tracked_control main_score 1
execute if entity @s[tag=phys_veh.have_on_board_clutch_lock] run scoreboard players set #have_on_board_clutch_lock main_score 1
execute store result score @s phys_veh.track_brake.a run data get storage midwut:main Data.data.phys_veh.track_brake.a
execute store result score @s phys_veh.track_brake.d run data get storage midwut:main Data.data.phys_veh.track_brake.d
scoreboard players operation #track_brake.a main_score = @s phys_veh.track_brake.a
scoreboard players operation #track_brake.d main_score = @s phys_veh.track_brake.d