scoreboard players set #fire main_score 0
scoreboard players set #charge main_score 0
execute if score @s phys_veh.id.input matches 0 run function physical_vehicles:zzz/vehicle/at_parts/cannon/input0
execute if score @s phys_veh.id.input matches 1 run function physical_vehicles:zzz/vehicle/at_parts/cannon/input1
execute if score @s phys_veh.id.input matches 2 run function physical_vehicles:zzz/vehicle/at_parts/cannon/input2
execute if score @s phys_veh.id.input matches 3 run function physical_vehicles:zzz/vehicle/at_parts/cannon/input3
execute if score @s phys_veh.id.input matches 4 run function physical_vehicles:zzz/vehicle/at_parts/cannon/input4

execute if entity @s[tag=phys_veh.cannon.is_recoiling] run function physical_vehicles:zzz/vehicle/at_parts/cannon/recoil

execute if score @s phys_veh.sound_timer.cannon_reload matches 1.. run scoreboard players remove @s phys_veh.sound_timer.cannon_reload 1
execute if entity @s[tag=phys_veh.shell_cherged] run return run function physical_vehicles:zzz/vehicle/at_parts/cannon/charged

execute if score #charge main_score matches 1 run return run function physical_vehicles:zzz/vehicle/at_parts/cannon/charging
execute if score @s phys_veh.normalize_timer matches 1.. run function physical_vehicles:zzz/vehicle/at_parts/cannon/charging_back
