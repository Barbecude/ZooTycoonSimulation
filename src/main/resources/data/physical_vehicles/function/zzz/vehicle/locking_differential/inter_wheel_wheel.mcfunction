execute if entity @s[tag=!phys_veh.wheel.driving] if entity @e[type=#physical_vehicles:refers_to_vehicle/track,tag=phys_veh.track,tag=phys_veh.track_broken,tag=phys_veh.selected_part,predicate=physical_vehicles:same_scores/wheel_axle_id,limit=1,x=0] run return fail
scoreboard players operation #temp main_score = @s phys_veh.spin.x
function physical_vehicles:zzz/vehicle/locking_differential/inter_wheel_wheel_neg
scoreboard players operation #temp main_score *= @s phys_veh.moment_of_inertia.x
scoreboard players operation #m main_score += @s phys_veh.moment_of_inertia.x
execute if score @s phys_veh.spin.x matches 1.. run return run scoreboard players operation #w main_score += #temp main_score
scoreboard players operation #w main_score -= #temp main_score
