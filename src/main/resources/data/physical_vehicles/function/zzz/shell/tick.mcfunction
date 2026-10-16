execute store result score #gametime main_score run time query gametime
execute if score #shell_responce main_score matches 1.. run scoreboard players remove #shell_responce main_score 1
execute as @e[type=minecraft:item_display,tag=phys_veh.shell] at @s run function physical_vehicles:zzz/shell/tick_shell
execute as @e[tag=phys_veh.step_ignore,x=0] run tag @s remove phys_veh.step_ignore

execute if score #shell_responce main_score matches 1.. run return run schedule function physical_vehicles:zzz/shell/tick 1t replace
execute if entity @e[type=minecraft:item_display,tag=phys_veh.shell,limit=1] run return run schedule function physical_vehicles:zzz/shell/tick 1t replace
forceload remove all