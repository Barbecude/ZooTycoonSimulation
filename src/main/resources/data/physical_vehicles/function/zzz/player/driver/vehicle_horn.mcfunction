advancement revoke @s only physical_vehicles:using/vehicle_controller

execute store result score #temp main_score run time query gametime
scoreboard players operation #temp main_score -= @s phys_veh.horn_sound_timer

execute if score #temp main_score matches 0.. run function physical_vehicles:zzz/player/driver/vehicle_horn_seat



