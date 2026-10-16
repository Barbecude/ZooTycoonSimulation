tp @s ~ ~ ~
data modify storage midwut:main DataShell set from entity @s
execute store result score #x main_score run data get storage midwut:main DataShell.Pos[0] 1000
execute store result score #y main_score run data get storage midwut:main DataShell.Pos[1] 1000
execute store result score #z main_score run data get storage midwut:main DataShell.Pos[2] 1000
scoreboard players operation #velocity main_score = @s phys_veh.velocity.z
scoreboard players operation #impulse main_score = @s phys_veh.velocity.z
execute if entity @s[tag=phys_veh.shell.explode_on_entity_impact] run scoreboard players set #velocity main_score -1

execute as @e[type=minecraft:text_display,tag=phys_veh.main,tag=phys_veh.have_collision,tag=!phys_veh.step_ignore,distance=..32] run function physical_vehicles:zzz/shell/hit_vehicle/radius with entity @s data.phys_veh

execute if score #phys_veh.shell_hit_marker main_score matches 1 if score #ray main_score matches -4..-3 run summon marker ~ ~ ~ {Tags:["phys_veh.shell_hit"]}
execute if score #ray main_score matches -3 run function physical_vehicles:zzz/shell/hit_vehicle/explode
execute if score #ray main_score matches -4 if entity @s[tag=phys_veh.shell.explode_on_entity_impact] positioned ^ ^ ^0.1 run function physical_vehicles:zzz/shell/hit_vehicle/explode
execute if score #ray main_score matches -4 if entity @s[tag=!phys_veh.shell.explode_on_entity_impact] positioned ^ ^ ^1 run function physical_vehicles:zzz/shell/hit_vehicle/explode