execute as @e[type=minecraft:item_display,tag=phys_veh.road_maker.spawn] at @s run function physical_vehicles:zzz/tools/road_maker/spawn

execute store result score #veh_count main_score if entity @e[type=minecraft:text_display,tag=phys_veh.main,tag=!phys_veh.kinetic]
execute if score #veh_count main_score > #phys_veh.max_veh_count main_score if entity @s[tag=!phys_veh.admin] run kill @e[type=minecraft:pig,tag=phys_veh.spawn,tag=!phys_veh.remover]
execute as @e[type=minecraft:pig,tag=phys_veh.spawn] at @s run function #physical_vehicles:summon