execute if score @s phys_veh.velocity.x matches -2000..-1001 run return run scoreboard players add @s phys_veh.velocity.x 2
execute if score @s phys_veh.velocity.x matches -1000..-1 run return run scoreboard players add @s phys_veh.velocity.x 1
execute if score @s phys_veh.velocity.x matches 1..1000 run return run scoreboard players remove @s phys_veh.velocity.x 1

scoreboard players operation @s phys_veh.velocity.x *= #999 const
scoreboard players operation @s phys_veh.velocity.x /= #1000 const
execute if score @s phys_veh.velocity.x matches ..-1 run scoreboard players add @s phys_veh.velocity.x 1