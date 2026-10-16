tag @s add phys_veh.road_maker.block
tag @s add phys_veh.road_maker
scoreboard players operation @s phys_veh.id.seat = #seat_id main_score
scoreboard players set @s phys_veh.count_wheels 1
scoreboard players operation @s phys_veh.offset.y = #y main_score