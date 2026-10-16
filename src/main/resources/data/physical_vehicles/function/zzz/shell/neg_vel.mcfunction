execute unless score #temp main_score matches 260000..271000 run return run scoreboard players set @s phys_veh.velocity.z 1
execute facing ^ ^ ^-1 run rotate @s ~ ~
scoreboard players operation @s phys_veh.velocity.z *= #-1 const
