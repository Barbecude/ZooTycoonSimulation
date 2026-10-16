scoreboard players set #c main_score 0
scoreboard players set #w main_score 0
scoreboard players set #m main_score 0
execute as @e[type=minecraft:item_display,tag=phys_veh.wheel,tag=phys_veh.selected_part,scores={phys_veh.id.wheel_axle=0..},predicate=physical_vehicles:same_scores/wheel_axle_id,x=0] run function physical_vehicles:zzz/vehicle/locking_differential/center_axle_wheel
scoreboard players operation #w main_score /= #c main_score
#tellraw @a [{score:{name:"#w",objective:"main_score"}}]
scoreboard players operation #w main_score /= #100 const
scoreboard players operation #m main_score /= #c main_score
scoreboard players operation #w main_score *= #m main_score
execute if entity @s[tag=phys_veh.have_on_board_clutch_lock] if score #phys_veh.control.brake_a main_score matches 1.. if score #wheel_axle_id main_score = @s phys_veh.track_brake.a run scoreboard players operation #w main_score *= #-1 const
execute if entity @s[tag=phys_veh.have_on_board_clutch_lock] if score #phys_veh.control.brake_d main_score matches 1.. if score #wheel_axle_id main_score = @s phys_veh.track_brake.d run scoreboard players operation #w main_score *= #-1 const
scoreboard players operation #wc main_score += #w main_score
scoreboard players operation #mc main_score += #m main_score
#scoreboard players operation #w main_score /= #100 const
