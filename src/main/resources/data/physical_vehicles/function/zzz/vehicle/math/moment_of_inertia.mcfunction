
scoreboard players operation #n00 main_score = #m00 main_score
scoreboard players operation #n01 main_score = #m01 main_score
scoreboard players operation #n02 main_score = #m02 main_score
scoreboard players operation #n10 main_score = #m10 main_score
scoreboard players operation #n11 main_score = #m11 main_score
scoreboard players operation #n12 main_score = #m12 main_score
scoreboard players operation #n20 main_score = #m20 main_score
scoreboard players operation #n21 main_score = #m21 main_score
scoreboard players operation #n22 main_score = #m22 main_score

scoreboard players operation #n00 main_score *= #1000 const
scoreboard players operation #n01 main_score *= #1000 const
scoreboard players operation #n02 main_score *= #1000 const
scoreboard players operation #n10 main_score *= #1000 const
scoreboard players operation #n11 main_score *= #1000 const
scoreboard players operation #n12 main_score *= #1000 const
scoreboard players operation #n20 main_score *= #1000 const
scoreboard players operation #n21 main_score *= #1000 const
scoreboard players operation #n22 main_score *= #1000 const
scoreboard players operation #n00 main_score /= @s phys_veh.moment_of_inertia.x
scoreboard players operation #n01 main_score /= @s phys_veh.moment_of_inertia.y
scoreboard players operation #n02 main_score /= @s phys_veh.moment_of_inertia.z
scoreboard players operation #n10 main_score /= @s phys_veh.moment_of_inertia.x
scoreboard players operation #n11 main_score /= @s phys_veh.moment_of_inertia.y
scoreboard players operation #n12 main_score /= @s phys_veh.moment_of_inertia.z
scoreboard players operation #n20 main_score /= @s phys_veh.moment_of_inertia.x
scoreboard players operation #n21 main_score /= @s phys_veh.moment_of_inertia.y
scoreboard players operation #n22 main_score /= @s phys_veh.moment_of_inertia.z


scoreboard players operation #i00 main_score = #n00 main_score
scoreboard players operation #i00 main_score *= #m00_3 main_score
scoreboard players operation #temp main_score = #n01 main_score
scoreboard players operation #temp main_score *= #m01_3 main_score
scoreboard players operation #i00 main_score += #temp main_score
scoreboard players operation #temp main_score = #n02 main_score
scoreboard players operation #temp main_score *= #m02_3 main_score
scoreboard players operation #i00 main_score += #temp main_score
scoreboard players operation #i00 main_score /= #1000 const

scoreboard players operation #i01 main_score = #n00 main_score
scoreboard players operation #i01 main_score *= #m10_3 main_score
scoreboard players operation #temp main_score = #n01 main_score
scoreboard players operation #temp main_score *= #m11_3 main_score
scoreboard players operation #i01 main_score += #temp main_score
scoreboard players operation #temp main_score = #n02 main_score
scoreboard players operation #temp main_score *= #m12_3 main_score
scoreboard players operation #i01 main_score += #temp main_score
scoreboard players operation #i01 main_score /= #1000 const

scoreboard players operation #i02 main_score = #n00 main_score
scoreboard players operation #i02 main_score *= #m20_3 main_score
scoreboard players operation #temp main_score = #n01 main_score
scoreboard players operation #temp main_score *= #m21_3 main_score
scoreboard players operation #i02 main_score += #temp main_score
scoreboard players operation #temp main_score = #n02 main_score
scoreboard players operation #temp main_score *= #m22_3 main_score
scoreboard players operation #i02 main_score += #temp main_score
scoreboard players operation #i02 main_score /= #1000 const

scoreboard players operation #i10 main_score = #n10 main_score
scoreboard players operation #i10 main_score *= #m00_3 main_score
scoreboard players operation #temp main_score = #n11 main_score
scoreboard players operation #temp main_score *= #m01_3 main_score
scoreboard players operation #i10 main_score += #temp main_score
scoreboard players operation #temp main_score = #n12 main_score
scoreboard players operation #temp main_score *= #m02_3 main_score
scoreboard players operation #i10 main_score += #temp main_score
scoreboard players operation #i10 main_score /= #1000 const

scoreboard players operation #i11 main_score = #n10 main_score
scoreboard players operation #i11 main_score *= #m10_3 main_score
scoreboard players operation #temp main_score = #n11 main_score
scoreboard players operation #temp main_score *= #m11_3 main_score
scoreboard players operation #i11 main_score += #temp main_score
scoreboard players operation #temp main_score = #n12 main_score
scoreboard players operation #temp main_score *= #m12_3 main_score
scoreboard players operation #i11 main_score += #temp main_score
scoreboard players operation #i11 main_score /= #1000 const

scoreboard players operation #i12 main_score = #n10 main_score
scoreboard players operation #i12 main_score *= #m20_3 main_score
scoreboard players operation #temp main_score = #n11 main_score
scoreboard players operation #temp main_score *= #m21_3 main_score
scoreboard players operation #i12 main_score += #temp main_score
scoreboard players operation #temp main_score = #n12 main_score
scoreboard players operation #temp main_score *= #m22_3 main_score
scoreboard players operation #i12 main_score += #temp main_score
scoreboard players operation #i12 main_score /= #1000 const

scoreboard players operation #i20 main_score = #n20 main_score
scoreboard players operation #i20 main_score *= #m00_3 main_score
scoreboard players operation #temp main_score = #n21 main_score
scoreboard players operation #temp main_score *= #m01_3 main_score
scoreboard players operation #i20 main_score += #temp main_score
scoreboard players operation #temp main_score = #n22 main_score
scoreboard players operation #temp main_score *= #m02_3 main_score
scoreboard players operation #i20 main_score += #temp main_score
scoreboard players operation #i20 main_score /= #1000 const

scoreboard players operation #i21 main_score = #n20 main_score
scoreboard players operation #i21 main_score *= #m10_3 main_score
scoreboard players operation #temp main_score = #n21 main_score
scoreboard players operation #temp main_score *= #m11_3 main_score
scoreboard players operation #i21 main_score += #temp main_score
scoreboard players operation #temp main_score = #n22 main_score
scoreboard players operation #temp main_score *= #m12_3 main_score
scoreboard players operation #i21 main_score += #temp main_score
scoreboard players operation #i21 main_score /= #1000 const

scoreboard players operation #i22 main_score = #n20 main_score
scoreboard players operation #i22 main_score *= #m20_3 main_score
scoreboard players operation #temp main_score = #n21 main_score
scoreboard players operation #temp main_score *= #m21_3 main_score
scoreboard players operation #i22 main_score += #temp main_score
scoreboard players operation #temp main_score = #n22 main_score
scoreboard players operation #temp main_score *= #m22_3 main_score
scoreboard players operation #i22 main_score += #temp main_score
scoreboard players operation #i22 main_score /= #1000 const

execute if score #i00 main_score matches ..-1 run scoreboard players set #i00 main_score 0
execute if score #i01 main_score matches ..-1 run scoreboard players set #i01 main_score 0
execute if score #i02 main_score matches ..-1 run scoreboard players set #i02 main_score 0
execute if score #i10 main_score matches ..-1 run scoreboard players set #i10 main_score 0
execute if score #i11 main_score matches ..-1 run scoreboard players set #i11 main_score 0
execute if score #i12 main_score matches ..-1 run scoreboard players set #i12 main_score 0
execute if score #i20 main_score matches ..-1 run scoreboard players set #i20 main_score 0
execute if score #i21 main_score matches ..-1 run scoreboard players set #i21 main_score 0
execute if score #i22 main_score matches ..-1 run scoreboard players set #i22 main_score 0

scoreboard players operation @s phys_veh.i00 = #i00 main_score
scoreboard players operation @s phys_veh.i01 = #i01 main_score
scoreboard players operation @s phys_veh.i02 = #i02 main_score
scoreboard players operation @s phys_veh.i10 = #i10 main_score
scoreboard players operation @s phys_veh.i11 = #i11 main_score
scoreboard players operation @s phys_veh.i12 = #i12 main_score
scoreboard players operation @s phys_veh.i20 = #i20 main_score
scoreboard players operation @s phys_veh.i21 = #i21 main_score
scoreboard players operation @s phys_veh.i22 = #i22 main_score

#tellraw @a [{score:{name:"#i00",objective:"main_score"}},"  ",{score:{name:"#i01",objective:"main_score"}},"  ",{score:{name:"#i02",objective:"main_score"}},"  ",{score:{name:"#i10",objective:"main_score"}},"  ",{score:{name:"#i11",objective:"main_score"}},"  ",{score:{name:"#i12",objective:"main_score"}},"  ",{score:{name:"#i20",objective:"main_score"}},"  ",{score:{name:"#i21",objective:"main_score"}},"  ",{score:{name:"#i22",objective:"main_score"}}]