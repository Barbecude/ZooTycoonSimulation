execute if entity @s[tag=!phys_veh.tracked_control] run return run function physical_vehicles:zzz/vehicle/at_parts/wheel/wheel_spin_ops

scoreboard players operation #wheel_axle_id main_score = @s phys_veh.id.wheel_axle
execute if entity @e[type=#physical_vehicles:refers_to_vehicle/track,tag=phys_veh.track,tag=phys_veh.track_broken,tag=phys_veh.selected_part,predicate=physical_vehicles:same_scores/wheel_axle_id,x=0] run return run function physical_vehicles:zzz/vehicle/at_parts/wheel/wheel_spin_track

scoreboard players operation #radius main_score = @s phys_veh.radius
execute as @e[type=#physical_vehicles:refers_to_vehicle/wheel,tag=phys_veh.wheel.driving,tag=phys_veh.selected_part,predicate=physical_vehicles:same_scores/wheel_axle_id,x=0] run function physical_vehicles:zzz/vehicle/at_parts/wheel/track_spin