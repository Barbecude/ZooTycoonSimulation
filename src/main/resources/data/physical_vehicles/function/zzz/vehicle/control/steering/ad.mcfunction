scoreboard players set #temp main_score 0

execute if score #w_rot main_score matches 1.. if score #rot main_score matches ..-1 run function physical_vehicles:zzz/vehicle/control/steering/no_ad
execute if score #w_rot main_score matches ..-1 if score #rot main_score matches 1.. run function physical_vehicles:zzz/vehicle/control/steering/no_ad

execute if score #temp main_score matches 0 run function physical_vehicles:zzz/vehicle/control/steering/ad_rot

