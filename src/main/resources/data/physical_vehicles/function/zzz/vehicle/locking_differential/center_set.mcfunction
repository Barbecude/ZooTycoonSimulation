scoreboard players operation @e[type=minecraft:item_display,tag=phys_veh.wheel,tag=phys_veh.selected_part,scores={phys_veh.id.wheel_axle=0..},x=0] phys_veh.spin.x = #ww main_score
execute if entity @s[tag=!phys_veh.have_on_board_clutch_lock] run return 1
scoreboard players operation #wheel_axle_id main_score = @s phys_veh.track_brake.a
execute if score #phys_veh.control.brake_d main_score > #phys_veh.control.brake_a main_score run scoreboard players operation #wheel_axle_id main_score = @s phys_veh.track_brake.d
scoreboard players operation @e[type=minecraft:item_display,tag=phys_veh.wheel,tag=phys_veh.selected_part,scores={phys_veh.id.wheel_axle=0..},predicate=physical_vehicles:same_scores/wheel_axle_id,x=0] phys_veh.spin.x *= #-1 const
