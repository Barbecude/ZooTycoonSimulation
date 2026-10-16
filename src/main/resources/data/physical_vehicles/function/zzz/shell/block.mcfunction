scoreboard players remove #ray main_score 1

execute if loaded ~ ~ ~ unless function midwut:help/check/air_only_collision run function physical_vehicles:zzz/shell/hit/block
execute if function physical_vehicles:zzz/shell/hit/check_entity_hitbox run function physical_vehicles:zzz/shell/hit/entity
execute if entity @e[type=minecraft:text_display,tag=phys_veh.main,tag=phys_veh.have_armor_collision,tag=!phys_veh.step_ignore,limit=1,distance=..32] run function physical_vehicles:zzz/shell/hit_vehicle/pre

execute if score #ray main_score matches 0 run function physical_vehicles:zzz/shell/block_ray_end