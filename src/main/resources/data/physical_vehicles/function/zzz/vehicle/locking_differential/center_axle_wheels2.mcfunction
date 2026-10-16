scoreboard players set #c main_score 0
scoreboard players set #c2 main_score 0
scoreboard players set #w main_score 0
execute as @e[type=minecraft:item_display,tag=phys_veh.wheel,tag=phys_veh.selected_part,scores={phys_veh.id.wheel_axle=0..},predicate=physical_vehicles:same_scores/wheel_axle_id] run function physical_vehicles:zzz/vehicle/locking_differential/center_axle_wheel2
execute if score #c main_score matches 0 run return run scoreboard players operation @e[type=minecraft:item_display,tag=phys_veh.wheel,tag=phys_veh.selected_part,scores={phys_veh.id.wheel_axle=0..},predicate=physical_vehicles:same_scores/wheel_axle_id] phys_veh.spin.x = #ww main_score
scoreboard players operation #w main_score /= #100 const
scoreboard players operation #w main_score /= #c2 main_score
scoreboard players operation #mul main_score = #ww main_score
scoreboard players operation #mul main_score %= #w main_score
scoreboard players operation #mul main_score *= #2 const
#tellraw @a [{score:{name:"#mul",objective:"main_score"}},"  ",{score:{name:"#w",objective:"main_score"}}]
function physical_vehicles:zzz/vehicle/locking_differential/center_axle_wheels2_mul
scoreboard players operation #mul main_score = #ww main_score
scoreboard players operation #mul main_score /= #w main_score
scoreboard players operation #mul main_score += #temp main_score
#execute if predicate ch scoreboard players add #mul main_score 1
execute as @e[type=minecraft:item_display,tag=phys_veh.wheel,tag=phys_veh.selected_part,scores={phys_veh.id.wheel_axle=0..},predicate=physical_vehicles:same_scores/wheel_axle_id] run function physical_vehicles:zzz/vehicle/locking_differential/center_axle_wheel3

