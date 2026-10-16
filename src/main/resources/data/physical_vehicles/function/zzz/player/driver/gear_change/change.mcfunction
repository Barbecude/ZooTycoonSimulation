scoreboard players operation @s phys_veh.gear_changing_timer = @s phys_veh.gear_changing_cooldown
scoreboard players operation #gear_num main_score -= #selected_slot main_score
execute store result score @s phys_veh.selected_slot run data get entity @s SelectedItemSlot
