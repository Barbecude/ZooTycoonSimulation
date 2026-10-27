tag @s add phys_veh.shell.hit
tag @s add phys_veh.shell.hit_entity
scoreboard players set #ray main_score -2
scoreboard players set #shell_responce main_score 40
# forceload add ~ ~
tp @s ~ ~ ~
execute if entity @s[tag=phys_veh.shell.hit_sound] if entity @a[distance=..64,limit=1] run function physical_vehicles:zzz/shell/hit/sound with entity @s data.phys_veh.shell.hit_sound
