tag @s add phys_veh.shell.hit
tag @s add phys_veh.shell.hit_block
scoreboard players set #ray main_score -1
scoreboard players set #shell_responce main_score 40
forceload add ~ ~
tp @s ~ ~ ~
execute if block ~ ~ ~ #minecraft:leaves run tp @p[tag=phys_veh.debug.leaves] ~ ~ ~
#execute store result score #temp main_score run data get entity @s Pos[2] 100
#scoreboard players operation #temp main_score -= @s phys_veh.pos.z
#tellraw @p {score:{name:"#temp",objective:"main_score"}}
execute if entity @s[tag=phys_veh.shell.explode_on_impact] run function physical_vehicles:zzz/shell/hit/explode_on_impact_block with entity @s data.phys_veh.shell
execute if entity @s[tag=phys_veh.shell.hit_sound] if entity @a[distance=..64,limit=1] run function physical_vehicles:zzz/shell/hit/sound with entity @s data.phys_veh.shell.hit_sound
