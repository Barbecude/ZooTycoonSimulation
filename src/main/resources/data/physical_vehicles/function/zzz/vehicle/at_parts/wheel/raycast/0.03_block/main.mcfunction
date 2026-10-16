#> physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/0.03_block/main
execute unless function physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/0.03_block/block run return run function physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/0.03_block/end
scoreboard players remove #distance main_score 30
execute positioned ^ ^ ^-0.03 run function physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/0.03_block/main
