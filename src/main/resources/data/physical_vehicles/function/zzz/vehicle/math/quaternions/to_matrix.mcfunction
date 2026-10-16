
scoreboard players operation #x22 main_score = @s phys_veh.quaternion.x
scoreboard players operation #x22 main_score *= @s phys_veh.quaternion.x
scoreboard players operation #x22 main_score /= #5000 const
scoreboard players operation #y22 main_score = @s phys_veh.quaternion.y
scoreboard players operation #y22 main_score *= @s phys_veh.quaternion.y
scoreboard players operation #y22 main_score /= #5000 const
scoreboard players operation #z22 main_score = @s phys_veh.quaternion.z
scoreboard players operation #z22 main_score *= @s phys_veh.quaternion.z
scoreboard players operation #z22 main_score /= #5000 const

scoreboard players set #m00 main_score 10000
scoreboard players operation #m00 main_score -= #y22 main_score
scoreboard players operation #m00 main_score -= #z22 main_score
scoreboard players set #m11 main_score 10000
scoreboard players operation #m11 main_score -= #x22 main_score
scoreboard players operation #m11 main_score -= #z22 main_score
scoreboard players set #m22 main_score 10000
scoreboard players operation #m22 main_score -= #x22 main_score
scoreboard players operation #m22 main_score -= #y22 main_score

scoreboard players operation #m01 main_score = @s phys_veh.quaternion.x
scoreboard players operation #m01 main_score *= @s phys_veh.quaternion.y
scoreboard players operation #m12 main_score = @s phys_veh.quaternion.z
scoreboard players operation #m12 main_score *= @s phys_veh.quaternion.y
scoreboard players operation #m02 main_score = @s phys_veh.quaternion.z
scoreboard players operation #m02 main_score *= @s phys_veh.quaternion.x
scoreboard players operation #xw main_score = @s phys_veh.quaternion.x
scoreboard players operation #xw main_score *= @s phys_veh.quaternion.w
scoreboard players operation #yw main_score = @s phys_veh.quaternion.y
scoreboard players operation #yw main_score *= @s phys_veh.quaternion.w
scoreboard players operation #zw main_score = @s phys_veh.quaternion.z
scoreboard players operation #zw main_score *= @s phys_veh.quaternion.w

scoreboard players operation #m10 main_score = #m01 main_score
scoreboard players operation #m10 main_score += #zw main_score
scoreboard players operation #m10 main_score /= #5000 const
scoreboard players operation #m01 main_score -= #zw main_score
scoreboard players operation #m01 main_score /= #5000 const
scoreboard players operation #m20 main_score = #m02 main_score
scoreboard players operation #m20 main_score -= #yw main_score
scoreboard players operation #m20 main_score /= #5000 const
scoreboard players operation #m02 main_score += #yw main_score
scoreboard players operation #m02 main_score /= #5000 const
scoreboard players operation #m21 main_score = #m12 main_score
scoreboard players operation #m21 main_score += #xw main_score
scoreboard players operation #m21 main_score /= #5000 const
scoreboard players operation #m12 main_score -= #xw main_score
scoreboard players operation #m12 main_score /= #5000 const

scoreboard players operation #m00_3 main_score = #m00 main_score
scoreboard players operation #m01_3 main_score = #m01 main_score
scoreboard players operation #m02_3 main_score = #m02 main_score
scoreboard players operation #m10_3 main_score = #m10 main_score
scoreboard players operation #m11_3 main_score = #m11 main_score
scoreboard players operation #m12_3 main_score = #m12 main_score
scoreboard players operation #m20_3 main_score = #m20 main_score
scoreboard players operation #m21_3 main_score = #m21 main_score
scoreboard players operation #m22_3 main_score = #m22 main_score

scoreboard players operation #m00_3 main_score /= #10 const
scoreboard players operation #m01_3 main_score /= #10 const
scoreboard players operation #m02_3 main_score /= #10 const
scoreboard players operation #m10_3 main_score /= #10 const
scoreboard players operation #m11_3 main_score /= #10 const
scoreboard players operation #m12_3 main_score /= #10 const
scoreboard players operation #m20_3 main_score /= #10 const
scoreboard players operation #m21_3 main_score /= #10 const
scoreboard players operation #m22_3 main_score /= #10 const

scoreboard players operation @s phys_veh.m00 = #m00 main_score
scoreboard players operation @s phys_veh.m01 = #m01 main_score
scoreboard players operation @s phys_veh.m02 = #m02 main_score
scoreboard players operation @s phys_veh.m10 = #m10 main_score
scoreboard players operation @s phys_veh.m11 = #m11 main_score
scoreboard players operation @s phys_veh.m12 = #m12 main_score
scoreboard players operation @s phys_veh.m20 = #m20 main_score
scoreboard players operation @s phys_veh.m21 = #m21 main_score
scoreboard players operation @s phys_veh.m22 = #m22 main_score

scoreboard players operation @s phys_veh.m00_3 = #m00_3 main_score
scoreboard players operation @s phys_veh.m01_3 = #m01_3 main_score
scoreboard players operation @s phys_veh.m02_3 = #m02_3 main_score
scoreboard players operation @s phys_veh.m10_3 = #m10_3 main_score
scoreboard players operation @s phys_veh.m11_3 = #m11_3 main_score
scoreboard players operation @s phys_veh.m12_3 = #m12_3 main_score
scoreboard players operation @s phys_veh.m20_3 = #m20_3 main_score
scoreboard players operation @s phys_veh.m21_3 = #m21_3 main_score
scoreboard players operation @s phys_veh.m22_3 = #m22_3 main_score