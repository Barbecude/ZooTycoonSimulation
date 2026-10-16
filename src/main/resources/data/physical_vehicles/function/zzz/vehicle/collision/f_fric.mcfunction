
scoreboard players operation #vx.y main_score /= #fr.x main_score
scoreboard players operation #vx.z main_score /= #fr.x main_score
scoreboard players operation #vy.x main_score /= #fr.y main_score
scoreboard players operation #vy.z main_score /= #fr.y main_score
scoreboard players operation #vz.x main_score /= #fr.z main_score
scoreboard players operation #vz.y main_score /= #fr.z main_score

scoreboard players operation @s phys_veh.add_velocity.y += #vx.y main_score
scoreboard players operation @s phys_veh.add_velocity.z += #vx.z main_score
scoreboard players operation @s phys_veh.add_velocity.x += #vy.x main_score
scoreboard players operation @s phys_veh.add_velocity.z += #vy.z main_score
scoreboard players operation @s phys_veh.add_velocity.x += #vz.x main_score
scoreboard players operation @s phys_veh.add_velocity.y += #vz.y main_score



# 1.000
scoreboard players operation #wx.x main_score /= #fr.x main_score
scoreboard players operation #wx.y main_score /= #fr.x main_score
scoreboard players operation #wx.z main_score /= #fr.x main_score
scoreboard players operation #wy.x main_score /= #fr.y main_score
scoreboard players operation #wy.y main_score /= #fr.y main_score
scoreboard players operation #wy.z main_score /= #fr.y main_score
scoreboard players operation #wz.x main_score /= #fr.z main_score
scoreboard players operation #wz.y main_score /= #fr.z main_score
scoreboard players operation #wz.z main_score /= #fr.z main_score

scoreboard players operation #wx.x main_score += #wy.x main_score
scoreboard players operation #wx.y main_score += #wy.y main_score
scoreboard players operation #wx.z main_score += #wy.z main_score
scoreboard players operation #wx.x main_score += #wz.x main_score
scoreboard players operation #wx.y main_score += #wz.y main_score
scoreboard players operation #wx.z main_score += #wz.z main_score


scoreboard players operation #offset.x main_score = #wx.x main_score
scoreboard players operation #offset.y main_score = #wx.y main_score
scoreboard players operation #offset.z main_score = #wx.z main_score

scoreboard players operation #x3 main_score = #m00_3 main_score
scoreboard players operation #x3 main_score *= #offset.x main_score
scoreboard players operation #tx2 main_score = #m10_3 main_score
scoreboard players operation #tx2 main_score *= #offset.y main_score
scoreboard players operation #tx3 main_score = #m20_3 main_score
scoreboard players operation #tx3 main_score *= #offset.z main_score

scoreboard players operation #y3 main_score = #m01_3 main_score
scoreboard players operation #y3 main_score *= #offset.x main_score
scoreboard players operation #ty2 main_score = #m11_3 main_score
scoreboard players operation #ty2 main_score *= #offset.y main_score
scoreboard players operation #ty3 main_score = #m21_3 main_score
scoreboard players operation #ty3 main_score *= #offset.z main_score

scoreboard players operation #z3 main_score = #m02_3 main_score
scoreboard players operation #z3 main_score *= #offset.x main_score
scoreboard players operation #tz2 main_score = #m12_3 main_score
scoreboard players operation #tz2 main_score *= #offset.y main_score
scoreboard players operation #tz3 main_score = #m22_3 main_score
scoreboard players operation #tz3 main_score *= #offset.z main_score

scoreboard players operation #x3 main_score += #tx2 main_score
scoreboard players operation #x3 main_score += #tx3 main_score

scoreboard players operation #y3 main_score += #ty2 main_score
scoreboard players operation #y3 main_score += #ty3 main_score

scoreboard players operation #z3 main_score += #tz2 main_score
scoreboard players operation #z3 main_score += #tz3 main_score


scoreboard players operation #x3 main_score /= @s phys_veh.moment_of_inertia_no_mass.x
scoreboard players operation #y3 main_score /= @s phys_veh.moment_of_inertia_no_mass.y
scoreboard players operation #z3 main_score /= @s phys_veh.moment_of_inertia_no_mass.z


scoreboard players operation @s phys_veh.add_spin.x += #x3 main_score
scoreboard players operation @s phys_veh.add_spin.y += #y3 main_score
scoreboard players operation @s phys_veh.add_spin.z += #z3 main_score

