scoreboard players operation #id_test main_score = @s phys_veh.id.object
scoreboard players operation #id_test main_score = @s phys_veh.id.vehicle
execute store result storage midwut:main macros.damage float 0.1 run scoreboard players get @s phys_veh.velocity.z
execute positioned ~-0.01 ~-0.01 ~-0.01 as @e[type=#midwut:living/all,tag=!phys_veh.step_ignore,limit=1,dx=0] positioned ~-0.98 ~-0.98 ~-0.98 if entity @s[dx=0] run function physical_vehicles:zzz/shell/hit/entity_damage_choice

execute if entity @s[tag=phys_veh.shell.explode_on_entity_impact] run return run function physical_vehicles:zzz/shell/hit/explode_on_impact_entity
execute if entity @s[tag=phys_veh.shell.stop_by_entity_hit] run return run function physical_vehicles:zzz/shell/hit/stop_by_entity


