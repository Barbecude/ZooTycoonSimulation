scoreboard players remove @s phys_veh.velocity.z 1
scoreboard players add @s phys_veh.gravity.y 100
execute if score @s phys_veh.gravity.y matches ..-100 run function physical_vehicles:zzz/shell/gravity_neg_loop