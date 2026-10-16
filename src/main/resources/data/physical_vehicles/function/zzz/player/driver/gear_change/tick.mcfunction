scoreboard players remove @s phys_veh.gear_changing_timer 1

execute store result score #selected_slot main_score run data get entity @s SelectedItemSlot
scoreboard players operation #selected_slot main_score -= @s phys_veh.selected_slot
execute if score #selected_slot main_score matches 5.. run scoreboard players remove #selected_slot main_score 9
execute if score #selected_slot main_score matches ..-5 run scoreboard players add #selected_slot main_score 9
execute unless score #selected_slot main_score matches 0 run function physical_vehicles:zzz/player/driver/gear_change/change

execute if score @s phys_veh.gear_changing_timer matches ..0 run return run function physical_vehicles:zzz/player/driver/gear_change/finish with storage midwut:main Data.data.phys_veh.sounds[{id:"switching_gear_finish"}]
scoreboard players set #gear_changing main_score 1