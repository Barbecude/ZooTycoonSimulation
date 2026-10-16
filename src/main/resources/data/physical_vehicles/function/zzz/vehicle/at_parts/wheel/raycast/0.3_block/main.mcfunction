#> physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/0.1_block/main
execute unless function physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/0.3_block/block run return run function physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/0.3_block/end
scoreboard players remove #distance main_score 300
execute positioned ^ ^ ^-0.3 run function physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/0.3_block/main
