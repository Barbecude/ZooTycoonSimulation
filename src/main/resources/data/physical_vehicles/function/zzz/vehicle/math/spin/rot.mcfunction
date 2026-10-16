
scoreboard players operation #main.rotated_spin.x main_score = #m00_3 main_score
scoreboard players operation #main.rotated_spin.x main_score *= @s phys_veh.spin.x
scoreboard players operation #tx2 main_score = #m01_3 main_score
scoreboard players operation #tx2 main_score *= @s phys_veh.spin.y
scoreboard players operation #tx3 main_score = #m02_3 main_score
scoreboard players operation #tx3 main_score *= @s phys_veh.spin.z

scoreboard players operation #main.rotated_spin.y main_score = #m10_3 main_score
scoreboard players operation #main.rotated_spin.y main_score *= @s phys_veh.spin.x
scoreboard players operation #ty2 main_score = #m11_3 main_score
scoreboard players operation #ty2 main_score *= @s phys_veh.spin.y
scoreboard players operation #ty3 main_score = #m12_3 main_score
scoreboard players operation #ty3 main_score *= @s phys_veh.spin.z

scoreboard players operation #main.rotated_spin.z main_score = #m20_3 main_score
scoreboard players operation #main.rotated_spin.z main_score *= @s phys_veh.spin.x
scoreboard players operation #tz2 main_score = #m21_3 main_score
scoreboard players operation #tz2 main_score *= @s phys_veh.spin.y
scoreboard players operation #tz3 main_score = #m22_3 main_score
scoreboard players operation #tz3 main_score *= @s phys_veh.spin.z

scoreboard players operation #main.rotated_spin.x main_score += #tx2 main_score
scoreboard players operation #main.rotated_spin.x main_score += #tx3 main_score

scoreboard players operation #main.rotated_spin.y main_score += #ty2 main_score
scoreboard players operation #main.rotated_spin.y main_score += #ty3 main_score

scoreboard players operation #main.rotated_spin.z main_score += #tz2 main_score
scoreboard players operation #main.rotated_spin.z main_score += #tz3 main_score

scoreboard players operation #main.rotated_spin.x main_score /= #1000 const
scoreboard players operation #main.rotated_spin.y main_score /= #1000 const
scoreboard players operation #main.rotated_spin.z main_score /= #1000 const
