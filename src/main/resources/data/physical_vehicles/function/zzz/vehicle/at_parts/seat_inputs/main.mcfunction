scoreboard players set #a main_score 0
scoreboard players set #d main_score 0
scoreboard players set #sprint main_score 0
scoreboard players set #s main_score 0
scoreboard players set #w main_score 0
scoreboard players set #jump main_score 0
scoreboard players set #attack main_score 0
scoreboard players set #use main_score 0
function physical_vehicles:zzz/vehicle/at_parts/seat_inputs/interaction_choice
execute on passengers if entity @s[type=minecraft:player] run function physical_vehicles:zzz/player/driver/main_passenger
execute if score @s phys_veh.id.input matches 0 run function physical_vehicles:zzz/vehicle/at_parts/seat_inputs/0
execute if score @s phys_veh.id.input matches 1 run function physical_vehicles:zzz/vehicle/at_parts/seat_inputs/1
execute if score @s phys_veh.id.input matches 2 run function physical_vehicles:zzz/vehicle/at_parts/seat_inputs/2
execute if score @s phys_veh.id.input matches 3 run function physical_vehicles:zzz/vehicle/at_parts/seat_inputs/3
execute if score @s phys_veh.id.input matches 4 run function physical_vehicles:zzz/vehicle/at_parts/seat_inputs/4