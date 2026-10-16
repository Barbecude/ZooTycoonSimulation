#scoreboard players operation #wheel_axle_id main_score = @s phys_veh.id.wheel_axle
#execute store result score #drive_axle_count main_score if entity @e[type=minecraft:item_display,tag=phys_veh.wheel.driving,tag=phys_veh.selected_part,predicate=physical_vehicles:same_scores/wheel_axle_id,x=0]
scoreboard players operation @s phys_veh.add_spin.y /= @s phys_veh.count_wheels
scoreboard players operation @s phys_veh.add_spin.x /= @s phys_veh.count_wheels


#tellraw @a [{score:{name:"@s",objective:"phys_veh.add_spin.x"}},"  ",{score:{name:"@s",objective:"phys_veh.add_spin.y"}}]
scoreboard players operation @s phys_veh.add_spin.y /= @s phys_veh.mass
scoreboard players operation @s phys_veh.add_spin.x /= #10 const
scoreboard players operation @s phys_veh.add_spin.x *= @s phys_veh.add_spin.y
scoreboard players operation @s phys_veh.add_spin.x /= #tick_rate main_score
scoreboard players operation @s phys_veh.add_spin.x /= @s phys_veh.moment_of_inertia_no_mass.x

#scoreboard players operation @s phys_veh.add_spin.x /= #drive_axle_count main_score
scoreboard players operation @s phys_veh.spin.x -= @s phys_veh.add_spin.x
scoreboard players set @s phys_veh.count_wheels 0
scoreboard players set @s phys_veh.add_spin.x 0
scoreboard players set @s phys_veh.add_spin.y 0