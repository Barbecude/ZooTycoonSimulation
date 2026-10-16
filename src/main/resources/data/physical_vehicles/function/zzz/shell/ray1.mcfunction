execute if entity @s[tag=phys_veh.shell.have_tracer] run particle minecraft:smoke ~ ~ ~ 0 0 0 0 1 force
execute positioned ^ ^ ^0.1 run function physical_vehicles:zzz/shell/block
execute if score #ray main_score matches 1.. positioned ^ ^ ^0.2 run function physical_vehicles:zzz/shell/block
execute if score #ray main_score matches 1.. positioned ^ ^ ^0.3 run function physical_vehicles:zzz/shell/block
execute if score #ray main_score matches 1.. positioned ^ ^ ^0.4 run function physical_vehicles:zzz/shell/block
execute if score #ray main_score matches 1.. positioned ^ ^ ^0.5 run function physical_vehicles:zzz/shell/block
execute if score #ray main_score matches 1.. positioned ^ ^ ^0.6 run function physical_vehicles:zzz/shell/block
execute if score #ray main_score matches 1.. positioned ^ ^ ^0.7 run function physical_vehicles:zzz/shell/block
execute if score #ray main_score matches 1.. positioned ^ ^ ^0.8 run function physical_vehicles:zzz/shell/block
execute if score #ray main_score matches 1.. positioned ^ ^ ^0.9 run function physical_vehicles:zzz/shell/block
execute if score #ray main_score matches 1.. positioned ^ ^ ^1.0 run function physical_vehicles:zzz/shell/block