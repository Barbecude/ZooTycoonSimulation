execute positioned ~ 10000 ~ as @e[type=marker,tag=phys_veh.road_maker.block,sort=nearest] positioned as @s run function physical_vehicles:zzz/tools/road_maker/make/loop

#kill @e[tag=phys_veh.road_maker,tag=!phys_veh.road_maker.block,predicate=physical_vehicles:same_scores/seat_id]
kill @e[tag=phys_veh.road_maker,predicate=physical_vehicles:same_scores/seat_id]