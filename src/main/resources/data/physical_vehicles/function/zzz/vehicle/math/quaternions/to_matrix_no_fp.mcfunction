
scoreboard players operation #x22 main_score = @s phys_veh.quaternion.x
scoreboard players operation #x22 main_score *= @s phys_veh.quaternion.x
scoreboard players operation #x22 main_score /= #5000 const
scoreboard players operation #y22 main_score = @s phys_veh.quaternion.y
scoreboard players operation #y22 main_score *= @s phys_veh.quaternion.y
scoreboard players operation #y22 main_score /= #5000 const
scoreboard players operation #z22 main_score = @s phys_veh.quaternion.z
scoreboard players operation #z22 main_score *= @s phys_veh.quaternion.z
scoreboard players operation #z22 main_score /= #5000 const

scoreboard players set @s phys_veh.m00 10000
scoreboard players operation @s phys_veh.m00 -= #y22 main_score
scoreboard players operation @s phys_veh.m00 -= #z22 main_score
scoreboard players set @s phys_veh.m11 10000
scoreboard players operation @s phys_veh.m11 -= #x22 main_score
scoreboard players operation @s phys_veh.m11 -= #z22 main_score
scoreboard players set @s phys_veh.m22 10000
scoreboard players operation @s phys_veh.m22 -= #x22 main_score
scoreboard players operation @s phys_veh.m22 -= #y22 main_score

scoreboard players operation @s phys_veh.m01 = @s phys_veh.quaternion.x
scoreboard players operation @s phys_veh.m01 *= @s phys_veh.quaternion.y
scoreboard players operation @s phys_veh.m12 = @s phys_veh.quaternion.z
scoreboard players operation @s phys_veh.m12 *= @s phys_veh.quaternion.y
scoreboard players operation @s phys_veh.m02 = @s phys_veh.quaternion.z
scoreboard players operation @s phys_veh.m02 *= @s phys_veh.quaternion.x
scoreboard players operation #xw main_score = @s phys_veh.quaternion.x
scoreboard players operation #xw main_score *= @s phys_veh.quaternion.w
scoreboard players operation #yw main_score = @s phys_veh.quaternion.y
scoreboard players operation #yw main_score *= @s phys_veh.quaternion.w
scoreboard players operation #zw main_score = @s phys_veh.quaternion.z
scoreboard players operation #zw main_score *= @s phys_veh.quaternion.w

scoreboard players operation @s phys_veh.m10 = @s phys_veh.m01
scoreboard players operation @s phys_veh.m10 += #zw main_score
scoreboard players operation @s phys_veh.m10 /= #5000 const
scoreboard players operation @s phys_veh.m01 -= #zw main_score
scoreboard players operation @s phys_veh.m01 /= #5000 const
scoreboard players operation @s phys_veh.m20 = @s phys_veh.m02
scoreboard players operation @s phys_veh.m20 -= #yw main_score
scoreboard players operation @s phys_veh.m20 /= #5000 const
scoreboard players operation @s phys_veh.m02 += #yw main_score
scoreboard players operation @s phys_veh.m02 /= #5000 const
scoreboard players operation @s phys_veh.m21 = @s phys_veh.m12
scoreboard players operation @s phys_veh.m21 += #xw main_score
scoreboard players operation @s phys_veh.m21 /= #5000 const
scoreboard players operation @s phys_veh.m12 -= #xw main_score
scoreboard players operation @s phys_veh.m12 /= #5000 const

scoreboard players operation @s phys_veh.m00_3 = @s phys_veh.m00
scoreboard players operation @s phys_veh.m01_3 = @s phys_veh.m01
scoreboard players operation @s phys_veh.m02_3 = @s phys_veh.m02
scoreboard players operation @s phys_veh.m10_3 = @s phys_veh.m10
scoreboard players operation @s phys_veh.m11_3 = @s phys_veh.m11
scoreboard players operation @s phys_veh.m12_3 = @s phys_veh.m12
scoreboard players operation @s phys_veh.m20_3 = @s phys_veh.m20
scoreboard players operation @s phys_veh.m21_3 = @s phys_veh.m21
scoreboard players operation @s phys_veh.m22_3 = @s phys_veh.m22

scoreboard players operation @s phys_veh.m00_3 /= #10 const
scoreboard players operation @s phys_veh.m01_3 /= #10 const
scoreboard players operation @s phys_veh.m02_3 /= #10 const
scoreboard players operation @s phys_veh.m10_3 /= #10 const
scoreboard players operation @s phys_veh.m11_3 /= #10 const
scoreboard players operation @s phys_veh.m12_3 /= #10 const
scoreboard players operation @s phys_veh.m20_3 /= #10 const
scoreboard players operation @s phys_veh.m21_3 /= #10 const
scoreboard players operation @s phys_veh.m22_3 /= #10 const
