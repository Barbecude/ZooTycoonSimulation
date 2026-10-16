execute positioned ~-0.01 ~-0.01 ~-0.01 as @e[type=#midwut:living/all,tag=!phys_veh.step_ignore,limit=1,dx=0] positioned ~-0.98 ~-0.98 ~-0.98 if entity @s[dx=0] run function physical_vehicles:zzz/shell/hit/entity_damage_choice

scoreboard players remove #hit_velocity main_score 1
execute if score #hit_velocity main_score matches 1.. positioned ^ ^ ^0.1 run function physical_vehicles:zzz/shell/hit_vehicle/ray_loop