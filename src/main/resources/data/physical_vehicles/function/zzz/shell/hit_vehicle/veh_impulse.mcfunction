
execute store result score #mass main_score run data get storage midwut:main DataShell.data.phys_veh.shell.mass 10
#tellraw @a [{score:{name:"#impulse",objective:"main_score"}},"  ",{score:{name:"#mass",objective:"main_score"}}]
execute if data storage midwut:main DataShell.data.phys_veh.shell.explode_on_entity_impact run function physical_vehicles:zzz/shell/hit_vehicle/veh_impulse_expl
scoreboard players operation #impulse main_score *= #mass main_score
execute in midwut:main positioned 0.0 0.0 0.0 positioned ^ ^ ^1 run tp 0000051d-0000-0fb7-0000-001500000014 ~ ~ ~
execute as 0000051d-0000-0fb7-0000-001500000014 run function physical_vehicles:zzz/shell/hit_vehicle/get_direction

#tellraw @a [{score:{name:"#impulse",objective:"main_score"}},"  ",{score:{name:"#impx",objective:"main_score"}},"  ",{score:{name:"#impy",objective:"main_score"}},"  ",{score:{name:"#impz",objective:"main_score"}}]
scoreboard players operation #impx main_score *= #impulse main_score
scoreboard players operation #impy main_score *= #impulse main_score
scoreboard players operation #impz main_score *= #impulse main_score
scoreboard players operation #impx main_score /= #10 const
scoreboard players operation #impy main_score /= #10 const
scoreboard players operation #impz main_score /= #10 const
scoreboard players operation #impx main_score /= @s phys_veh.mass
scoreboard players operation #impy main_score /= @s phys_veh.mass
scoreboard players operation #impz main_score /= @s phys_veh.mass

scoreboard players operation @s phys_veh.velocity.x += #impx main_score
scoreboard players operation @s phys_veh.velocity.y += #impy main_score
scoreboard players operation @s phys_veh.velocity.z += #impz main_score


#scoreboard players operation #impx main_score /= #10 const
#scoreboard players operation #impy main_score /= #10 const
#scoreboard players operation #impz main_score /= #10 const

scoreboard players operation #x main_score = #vy main_score
scoreboard players operation #x main_score *= #impz main_score
scoreboard players operation #temp main_score = #vz main_score
scoreboard players operation #temp main_score *= #impy main_score
scoreboard players operation #x main_score -= #temp main_score
scoreboard players operation #x main_score /= #1000 const

scoreboard players operation #y main_score = #vx main_score
scoreboard players operation #y main_score *= #impz main_score
scoreboard players operation #temp main_score = #vz main_score
scoreboard players operation #temp main_score *= #impx main_score
scoreboard players operation #y main_score -= #temp main_score
scoreboard players operation #y main_score /= #-1000 const

scoreboard players operation #z main_score = #vx main_score
scoreboard players operation #z main_score *= #impy main_score
scoreboard players operation #temp main_score = #vy main_score
scoreboard players operation #temp main_score *= #impx main_score
scoreboard players operation #z main_score -= #temp main_score
scoreboard players operation #z main_score /= #1000 const



scoreboard players operation #x2 main_score = @s phys_veh.m00_3
scoreboard players operation #x2 main_score *= #x main_score
scoreboard players operation #tx2 main_score = @s phys_veh.m10_3
scoreboard players operation #tx2 main_score *= #y main_score
scoreboard players operation #tx3 main_score = @s phys_veh.m20_3
scoreboard players operation #tx3 main_score *= #z main_score

scoreboard players operation #y2 main_score = @s phys_veh.m01_3
scoreboard players operation #y2 main_score *= #x main_score
scoreboard players operation #ty2 main_score = @s phys_veh.m11_3
scoreboard players operation #ty2 main_score *= #y main_score
scoreboard players operation #ty3 main_score = @s phys_veh.m21_3
scoreboard players operation #ty3 main_score *= #z main_score

scoreboard players operation #z2 main_score = @s phys_veh.m02_3
scoreboard players operation #z2 main_score *= #x main_score
scoreboard players operation #tz2 main_score = @s phys_veh.m12_3
scoreboard players operation #tz2 main_score *= #y main_score
scoreboard players operation #tz3 main_score = @s phys_veh.m22_3
scoreboard players operation #tz3 main_score *= #z main_score

scoreboard players operation #x2 main_score += #tx2 main_score
scoreboard players operation #x2 main_score += #tx3 main_score

scoreboard players operation #y2 main_score += #ty2 main_score
scoreboard players operation #y2 main_score += #ty3 main_score

scoreboard players operation #z2 main_score += #tz2 main_score
scoreboard players operation #z2 main_score += #tz3 main_score


scoreboard players operation #x2 main_score /= @s phys_veh.moment_of_inertia_no_mass.x
scoreboard players operation #y2 main_score /= @s phys_veh.moment_of_inertia_no_mass.y
scoreboard players operation #z2 main_score /= @s phys_veh.moment_of_inertia_no_mass.z



scoreboard players operation @s phys_veh.spin.x += #x2 main_score
scoreboard players operation @s phys_veh.spin.y += #y2 main_score
scoreboard players operation @s phys_veh.spin.z += #z2 main_score