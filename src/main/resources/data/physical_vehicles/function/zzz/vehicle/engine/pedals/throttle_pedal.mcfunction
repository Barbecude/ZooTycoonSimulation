execute unless entity @s[tag=phys_veh.ignition] run scoreboard players set #throttle_pedal main_score 0

execute unless score #throttle_pedal main_score matches 0 run return run execute if score @s phys_veh.control.throttle matches ..99 run scoreboard players operation @s phys_veh.control.throttle += #pedal_deltatime main_score
execute if score @s phys_veh.control.throttle matches 1.. run scoreboard players operation @s phys_veh.control.throttle -= #pedal_deltatime main_score