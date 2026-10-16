scoreboard players set #wheel_axle_id main_score 0
scoreboard players set #wc main_score 0
scoreboard players set #mc main_score 0
function physical_vehicles:zzz/vehicle/locking_differential/center_axle_loop
execute if score #wc main_score matches ..-1 run scoreboard players operation #mc main_score *= #-1 const
scoreboard players operation #wc main_score /= #mc main_score
execute if score #mc main_score matches ..-1 run scoreboard players operation #wc main_score *= #-1 const
scoreboard players operation #ww main_score = #wc main_score
scoreboard players operation #ww main_score *= #100 const
scoreboard players operation @s phys_veh.transmission_w = #wc main_score
scoreboard players operation @s phys_veh.transmission_w /= #10 const
#tellraw @a [{score:{name:"#ww",objective:"main_score"}}]
execute if entity @s[tag=phys_veh.locking_inter_wheel_differential] run return run function physical_vehicles:zzz/vehicle/locking_differential/center_set
scoreboard players set #wheel_axle_id main_score 0
function physical_vehicles:zzz/vehicle/locking_differential/center_axle_loop2
