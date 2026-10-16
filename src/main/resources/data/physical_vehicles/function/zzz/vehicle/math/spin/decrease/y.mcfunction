execute if score @s phys_veh.spin.y matches -400..-201 run return run scoreboard players add @s phys_veh.spin.y 2
execute if score @s phys_veh.spin.y matches -200..-1 run return run scoreboard players add @s phys_veh.spin.y 1
execute if score @s phys_veh.spin.y matches 1..200 run return run scoreboard players remove @s phys_veh.spin.y 1

scoreboard players operation @s phys_veh.spin.y *= #995 const
scoreboard players operation @s phys_veh.spin.y /= #1000 const
execute if score @s phys_veh.spin.y matches ..-1 run scoreboard players add @s phys_veh.spin.y 1