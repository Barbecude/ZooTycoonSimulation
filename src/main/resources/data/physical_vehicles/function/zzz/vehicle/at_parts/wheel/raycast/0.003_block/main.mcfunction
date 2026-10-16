#> physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/0.001_block/main
execute unless function physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/0.003_block/block run return run function physical_vehicles:zzz/vehicle/at_parts/wheel/end
scoreboard players remove #distance main_score 3
execute positioned ^ ^ ^-0.003 run function physical_vehicles:zzz/vehicle/at_parts/wheel/raycast/0.003_block/main
