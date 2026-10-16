scoreboard players operation @s phys_veh.coll_velocity.x /= @s phys_veh.coll_count
scoreboard players operation @s phys_veh.coll_velocity.y /= @s phys_veh.coll_count
scoreboard players operation @s phys_veh.coll_velocity.z /= @s phys_veh.coll_count
scoreboard players operation @s phys_veh.coll_spin.x /= @s phys_veh.coll_count
scoreboard players operation @s phys_veh.coll_spin.y /= @s phys_veh.coll_count
scoreboard players operation @s phys_veh.coll_spin.z /= @s phys_veh.coll_count

scoreboard players operation @s phys_veh.velocity.x += @s phys_veh.coll_velocity.x
scoreboard players operation @s phys_veh.velocity.y += @s phys_veh.coll_velocity.y
scoreboard players operation @s phys_veh.velocity.z += @s phys_veh.coll_velocity.z
scoreboard players operation @s phys_veh.spin.x += @s phys_veh.coll_spin.x
scoreboard players operation @s phys_veh.spin.y += @s phys_veh.coll_spin.y
scoreboard players operation @s phys_veh.spin.z += @s phys_veh.coll_spin.z
scoreboard players set @s phys_veh.coll_velocity.x 0
scoreboard players set @s phys_veh.coll_velocity.y 0
scoreboard players set @s phys_veh.coll_velocity.z 0
scoreboard players set @s phys_veh.coll_spin.x 0
scoreboard players set @s phys_veh.coll_spin.y 0
scoreboard players set @s phys_veh.coll_spin.z 0
scoreboard players set @s phys_veh.coll_count 0