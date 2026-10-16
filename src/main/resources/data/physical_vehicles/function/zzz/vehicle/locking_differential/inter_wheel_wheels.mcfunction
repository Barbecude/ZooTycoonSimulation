execute if entity @s[tag=!phys_veh.locking_center_differential] run scoreboard players add #c main_score 1
scoreboard players set #w main_score 0
scoreboard players set #m main_score 0

execute as @e[type=minecraft:item_display,tag=phys_veh.wheel,tag=phys_veh.selected_part,scores={phys_veh.id.wheel_axle=0..},predicate=physical_vehicles:same_scores/wheel_axle_id,x=0] run function physical_vehicles:zzz/vehicle/locking_differential/inter_wheel_wheel
execute if score #w main_score matches ..-1 run scoreboard players operation #m main_score *= #-1 const
scoreboard players operation #w main_score /= #m main_score
execute if score #m main_score matches ..-1 run scoreboard players operation #w main_score *= #-1 const
#execute if entity @s[tag=phys_veh.have_on_board_clutch_lock] unless score #phys_veh.control.brake main_score matches 0 if score #phys_veh.control.brake_a main_score matches 1.. if score #wheel_axle_id main_score = @s phys_veh.track_brake.a run scoreboard players operation #w main_score *= #-1 const
#execute if entity @s[tag=phys_veh.have_on_board_clutch_lock] unless score #phys_veh.control.brake main_score matches 0 if score #phys_veh.control.brake_d main_score matches 1.. if score #wheel_axle_id main_score = @s phys_veh.track_brake.d run scoreboard players operation #w main_score *= #-1 const
execute if entity @s[tag=!phys_veh.locking_center_differential] run scoreboard players operation @s phys_veh.transmission_w += #w main_score
scoreboard players operation #w main_score *= #100 const
execute if entity @e[type=#physical_vehicles:refers_to_vehicle/track,tag=phys_veh.track,tag=phys_veh.track_broken,tag=phys_veh.selected_part,predicate=physical_vehicles:same_scores/wheel_axle_id,limit=1,x=0] run return run function physical_vehicles:zzz/vehicle/locking_differential/track_broken_spins
scoreboard players operation @e[type=minecraft:item_display,tag=phys_veh.wheel,tag=phys_veh.selected_part,scores={phys_veh.id.wheel_axle=0..},predicate=physical_vehicles:same_scores/wheel_axle_id,x=0] phys_veh.spin.x = #w main_score
execute if entity @s[tag=phys_veh.tracked_control] run scoreboard players operation @e[type=minecraft:item_display,tag=phys_veh.track,tag=phys_veh.selected_part,scores={phys_veh.id.wheel_axle=0..},predicate=physical_vehicles:same_scores/wheel_axle_id,x=0] phys_veh.spin.x = #w main_score
