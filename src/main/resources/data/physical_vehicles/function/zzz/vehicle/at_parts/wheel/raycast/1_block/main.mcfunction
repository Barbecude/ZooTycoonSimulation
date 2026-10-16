#> physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/1_block/main
execute unless function physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/1_block/block run return run function physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/1_block/end
tag @s add phys_veh.test
scoreboard players remove #distance main_score 1000
execute positioned ^ ^ ^-1 run function physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/1_block/main
