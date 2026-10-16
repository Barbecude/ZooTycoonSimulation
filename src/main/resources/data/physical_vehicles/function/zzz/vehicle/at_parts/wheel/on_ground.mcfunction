scoreboard players add #count_wheels.total main_score 1
#scoreboard players operation @s phys_veh.coeff_friction = #coeff_friction main_score

scoreboard players operation @s phys_veh.wheel.suspension.force = #distance main_score
scoreboard players operation @s phys_veh.wheel.suspension.force *= @s phys_veh.strength
scoreboard players operation @s phys_veh.wheel.suspension.offset -= #distance main_score
scoreboard players operation @s phys_veh.wheel.suspension.offset *= @s phys_veh.damping
scoreboard players operation @s phys_veh.wheel.suspension.force -= @s phys_veh.wheel.suspension.offset
scoreboard players operation @s phys_veh.wheel.suspension.force > #0 const



scoreboard players operation @s phys_veh.wheel.offset_y += @s phys_veh.offset.y
scoreboard players operation @s phys_veh.wheel.offset_y -= @s phys_veh.radius

scoreboard players operation @s phys_veh.offset2.x = #m00 main_score
scoreboard players operation @s phys_veh.offset2.x *= @s phys_veh.offset.x
scoreboard players operation #tx2 main_score = #m01 main_score
scoreboard players operation #tx2 main_score *= @s phys_veh.wheel.offset_y
scoreboard players operation #tx3 main_score = #m02 main_score
scoreboard players operation #tx3 main_score *= @s phys_veh.offset.z

scoreboard players operation @s phys_veh.offset2.y = #m10 main_score
scoreboard players operation @s phys_veh.offset2.y *= @s phys_veh.offset.x
scoreboard players operation #ty2 main_score = #m11 main_score
scoreboard players operation #ty2 main_score *= @s phys_veh.wheel.offset_y
scoreboard players operation #ty3 main_score = #m12 main_score
scoreboard players operation #ty3 main_score *= @s phys_veh.offset.z

scoreboard players operation @s phys_veh.offset2.z = #m20 main_score
scoreboard players operation @s phys_veh.offset2.z *= @s phys_veh.offset.x
scoreboard players operation #tz2 main_score = #m21 main_score
scoreboard players operation #tz2 main_score *= @s phys_veh.wheel.offset_y
scoreboard players operation #tz3 main_score = #m22 main_score
scoreboard players operation #tz3 main_score *= @s phys_veh.offset.z

scoreboard players operation @s phys_veh.offset2.x += #tx2 main_score
scoreboard players operation @s phys_veh.offset2.x += #tx3 main_score

scoreboard players operation @s phys_veh.offset2.y += #ty2 main_score
scoreboard players operation @s phys_veh.offset2.y += #ty3 main_score

scoreboard players operation @s phys_veh.offset2.z += #tz2 main_score
scoreboard players operation @s phys_veh.offset2.z += #tz3 main_score

scoreboard players operation @s phys_veh.offset2.x /= #10000 const
scoreboard players operation @s phys_veh.offset2.y /= #10000 const
scoreboard players operation @s phys_veh.offset2.z /= #10000 const

execute if score @s phys_veh.offset.x > #wn0.x main_score run function physical_vehicles:zzz/vehicle/at_parts/wheel/norm_points/0
execute if score @s phys_veh.offset.x < #wn1.x main_score run function physical_vehicles:zzz/vehicle/at_parts/wheel/norm_points/1
execute if score @s phys_veh.offset.z > #wn2.z main_score run function physical_vehicles:zzz/vehicle/at_parts/wheel/norm_points/2
execute if score @s phys_veh.offset.z < #wn3.z main_score run function physical_vehicles:zzz/vehicle/at_parts/wheel/norm_points/3
