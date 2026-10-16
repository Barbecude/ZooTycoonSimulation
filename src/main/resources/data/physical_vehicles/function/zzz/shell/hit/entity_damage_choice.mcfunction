tag @s add phys_veh.step_ignore

execute if entity @a[limit=1,predicate=midwut:check/score_ranges/midwut.player_id] run return run function physical_vehicles:zzz/shell/hit/entity_damage_player with storage midwut:main macros
execute if entity @e[type=minecraft:text_display,tag=phys_veh.main,limit=1,predicate=physical_vehicles:same_scores/vehicle_id] run return run function physical_vehicles:zzz/shell/hit/entity_damage_vehicle with storage midwut:main macros
function physical_vehicles:zzz/shell/hit/entity_damage with storage midwut:main macros