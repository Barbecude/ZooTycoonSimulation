data modify storage midwut:main aiming set from entity @s data.phys_veh.aiming
#execute if score #phys_veh.angle_x_changed main_score matches 1 run 
function physical_vehicles:zzz/vehicle/at_parts/aiming/build_text
data modify storage midwut:main text set from storage midwut:main aiming.builded_title

scoreboard players operation #object_id main_score = @s phys_veh.id.turret
execute as @e[type=#physical_vehicles:refers_to_vehicle/all,tag=phys_veh.selected_part,limit=1,predicate=physical_vehicles:same_scores/object_id] run function physical_vehicles:zzz/vehicle/at_parts/aiming/rotate_vector
scoreboard players operation #object_id main_score = @s phys_veh.id.seat
execute as @e[type=minecraft:text_display,tag=phys_veh.seat,tag=phys_veh.selected_part,limit=1,predicate=physical_vehicles:same_scores/object_id] on passengers on passengers if entity @s[type=minecraft:player] run return run function physical_vehicles:zzz/vehicle/at_parts/aiming/tick_player
function physical_vehicles:zzz/vehicle/at_parts/aiming/disable