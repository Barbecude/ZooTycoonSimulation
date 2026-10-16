scoreboard players set #count_wheels.total main_score 3

#execute if score #wn0.x main_score = #wn1.x main_score if score #wn0.z main_score = #wn1.z main_score run scoreboard players remove #count_wheels.total main_score 1
#execute if score #wn0.x main_score = #wn2.x main_score if score #wn0.z main_score = #wn2.z main_score run scoreboard players remove #count_wheels.total main_score 1
#execute if score #wn1.x main_score = #wn2.x main_score if score #wn1.z main_score = #wn2.z main_score run scoreboard players remove #count_wheels.total main_score 1

scoreboard players operation #wn0_.x main_score = #wn0.x main_score
scoreboard players operation #wn0_.y main_score = #wn0.y main_score
scoreboard players operation #wn0_.z main_score = #wn0.z main_score
scoreboard players operation #wn2_.x main_score = #wn2.x main_score
scoreboard players operation #wn2_.y main_score = #wn2.y main_score
scoreboard players operation #wn2_.z main_score = #wn2.z main_score

scoreboard players operation #wn0.x main_score -= #wn1.x main_score
scoreboard players operation #wn0.y main_score -= #wn1.y main_score
#scoreboard players operation #wn0.z main_score -= #wn1.z main_score
#scoreboard players operation #wn2.x main_score -= #wn3.x main_score
scoreboard players operation #wn2.y main_score -= #wn3.y main_score
scoreboard players operation #wn2.z main_score -= #wn3.z main_score

scoreboard players operation #wn0.y main_score *= #-1 const
#scoreboard players operation #wn2.y main_score *= #-1 const
scoreboard players set #wn0.z main_score 0
scoreboard players set #wn2.x main_score 0

execute if score #wn0.x main_score matches -200..200 run scoreboard players remove #count_wheels.total main_score 1
execute if score #wn2.z main_score matches -200..200 run scoreboard players remove #count_wheels.total main_score 1
execute if score #count_wheels.total main_score matches 1 run return run function physical_vehicles:zzz/vehicle/at_parts/wheel/add_vel/norm/0
execute if score #count_wheels.total main_score matches 2 run return run function physical_vehicles:zzz/vehicle/at_parts/wheel/add_vel/norm/1

#tellraw @a [{score:{name:"#wn0.x",objective:"main_score"}},"  ",{score:{name:"#wn0.y",objective:"main_score"}},"  ",{score:{name:"#wn2.y",objective:"main_score"}},"  ",{score:{name:"#wn2.z",objective:"main_score"}}]



scoreboard players operation #IN:arctan2.X main_score = #wn0.z main_score
scoreboard players operation #IN:arctan2.Y main_score = #wn0.x main_score
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

scoreboard players operation #IN:arctan2.Y main_score = #wn0.y main_score
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

scoreboard players operation #wn0.x main_score *= #1000 const
scoreboard players operation #wn0.y main_score *= #1000 const
scoreboard players operation #wn0.z main_score *= #1000 const
scoreboard players operation #wn0.x main_score /= #IN:arctan2.X main_score
scoreboard players operation #wn0.y main_score /= #IN:arctan2.X main_score
scoreboard players operation #wn0.z main_score /= #IN:arctan2.X main_score






scoreboard players operation #IN:arctan2.X main_score = #wn2.z main_score
scoreboard players operation #IN:arctan2.Y main_score = #wn2.x main_score
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

scoreboard players operation #IN:arctan2.Y main_score = #wn2.y main_score
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

scoreboard players operation #wn2.x main_score *= #1000 const
scoreboard players operation #wn2.y main_score *= #1000 const
scoreboard players operation #wn2.z main_score *= #1000 const
scoreboard players operation #wn2.x main_score /= #IN:arctan2.X main_score
scoreboard players operation #wn2.y main_score /= #IN:arctan2.X main_score
scoreboard players operation #wn2.z main_score /= #IN:arctan2.X main_score



scoreboard players operation #rs.x main_score = #m00 main_score
scoreboard players operation #rs.x main_score *= #wn0.x main_score
scoreboard players operation #tx2 main_score = #m01 main_score
scoreboard players operation #tx2 main_score *= #wn0.y main_score
scoreboard players operation #tx3 main_score = #m02 main_score
scoreboard players operation #tx3 main_score *= #wn0.z main_score

scoreboard players operation #rs.y main_score = #m10 main_score
scoreboard players operation #rs.y main_score *= #wn0.x main_score
scoreboard players operation #ty2 main_score = #m11 main_score
scoreboard players operation #ty2 main_score *= #wn0.y main_score
scoreboard players operation #ty3 main_score = #m12 main_score
scoreboard players operation #ty3 main_score *= #wn0.z main_score

scoreboard players operation #rs.z main_score = #m20 main_score
scoreboard players operation #rs.z main_score *= #wn0.x main_score
scoreboard players operation #tz2 main_score = #m21 main_score
scoreboard players operation #tz2 main_score *= #wn0.y main_score
scoreboard players operation #tz3 main_score = #m22 main_score
scoreboard players operation #tz3 main_score *= #wn0.z main_score

scoreboard players operation #rs.x main_score += #tx2 main_score
scoreboard players operation #rs.x main_score += #tx3 main_score

scoreboard players operation #rs.y main_score += #ty2 main_score
scoreboard players operation #rs.y main_score += #ty3 main_score

scoreboard players operation #rs.z main_score += #tz2 main_score
scoreboard players operation #rs.z main_score += #tz3 main_score

scoreboard players operation #rs.x main_score /= #10000 const
scoreboard players operation #rs.y main_score /= #10000 const
scoreboard players operation #rs.z main_score /= #10000 const



scoreboard players operation #rs2.x main_score = #m00 main_score
scoreboard players operation #rs2.x main_score *= #wn2.x main_score
scoreboard players operation #tx2 main_score = #m01 main_score
scoreboard players operation #tx2 main_score *= #wn2.y main_score
scoreboard players operation #tx3 main_score = #m02 main_score
scoreboard players operation #tx3 main_score *= #wn2.z main_score

scoreboard players operation #rs2.y main_score = #m10 main_score
scoreboard players operation #rs2.y main_score *= #wn2.x main_score
scoreboard players operation #ty2 main_score = #m11 main_score
scoreboard players operation #ty2 main_score *= #wn2.y main_score
scoreboard players operation #ty3 main_score = #m12 main_score
scoreboard players operation #ty3 main_score *= #wn2.z main_score

scoreboard players operation #rs2.z main_score = #m20 main_score
scoreboard players operation #rs2.z main_score *= #wn2.x main_score
scoreboard players operation #tz2 main_score = #m21 main_score
scoreboard players operation #tz2 main_score *= #wn2.y main_score
scoreboard players operation #tz3 main_score = #m22 main_score
scoreboard players operation #tz3 main_score *= #wn2.z main_score

scoreboard players operation #rs2.x main_score += #tx2 main_score
scoreboard players operation #rs2.x main_score += #tx3 main_score

scoreboard players operation #rs2.y main_score += #ty2 main_score
scoreboard players operation #rs2.y main_score += #ty3 main_score

scoreboard players operation #rs2.z main_score += #tz2 main_score
scoreboard players operation #rs2.z main_score += #tz3 main_score

scoreboard players operation #rs2.x main_score /= #10000 const
scoreboard players operation #rs2.y main_score /= #10000 const
scoreboard players operation #rs2.z main_score /= #10000 const



scoreboard players operation #norm.z main_score = #rs.x main_score
scoreboard players operation #norm.x main_score = #rs.y main_score
scoreboard players operation #norm.y main_score = #rs.z main_score

scoreboard players operation #norm.x main_score *= #rs2.z main_score
scoreboard players operation #temp main_score = #norm.y main_score
scoreboard players operation #temp main_score *= #rs2.y main_score
scoreboard players operation #norm.x main_score -= #temp main_score
scoreboard players operation #norm.x main_score /= #-1000 const
execute if score #norm.x main_score matches ..-1 run scoreboard players add #norm.x main_score 1

scoreboard players operation #norm.y main_score *= #rs2.x main_score
scoreboard players operation #temp main_score = #norm.z main_score
scoreboard players operation #temp main_score *= #rs2.z main_score
scoreboard players operation #norm.y main_score -= #temp main_score
scoreboard players operation #norm.y main_score /= #-1000 const
execute if score #norm.y main_score matches ..-1 run scoreboard players add #norm.y main_score 1

scoreboard players operation #norm.z main_score *= #rs2.y main_score
scoreboard players operation #temp main_score = #rs.y main_score
scoreboard players operation #temp main_score *= #rs2.x main_score
scoreboard players operation #norm.z main_score -= #temp main_score
scoreboard players operation #norm.z main_score /= #-1000 const
execute if score #norm.z main_score matches ..-1 run scoreboard players add #norm.z main_score 1


scoreboard players operation #IN:arctan2.X main_score = #norm.z main_score
scoreboard players operation #IN:arctan2.Y main_score = #norm.x main_score
execute if score #IN:arctan2.X main_score matches ..-1 run scoreboard players operation #IN:arctan2.X main_score *= #-1 const
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

scoreboard players operation #IN:arctan2.Y main_score = #norm.y main_score
execute if score #IN:arctan2.Y main_score matches ..-1 run scoreboard players operation #IN:arctan2.Y main_score *= #-1 const
execute if score #IN:arctan2.Y main_score > #IN:arctan2.X main_score run scoreboard players operation #IN:arctan2.Y main_score >< #IN:arctan2.X main_score
scoreboard players operation #OUT:arctan2 main_score = #IN:arctan2.Y main_score
scoreboard players operation #midwut.temp main_score = #IN:arctan2.X main_score
execute store result storage midwut:main trigonometric.in int 1 run function midwut:y_functions/math/trigonometric/arctan2/1

function midwut:y_functions/math/trigonometric/cos with storage midwut:main trigonometric
scoreboard players operation #IN:arctan2.X main_score *= #10000 const
scoreboard players operation #IN:arctan2.X main_score /= #OUT:cos main_score

scoreboard players operation #norm.x main_score *= #1000 const
scoreboard players operation #norm.y main_score *= #1000 const
scoreboard players operation #norm.z main_score *= #1000 const
scoreboard players operation #norm.x main_score /= #IN:arctan2.X main_score
scoreboard players operation #norm.y main_score /= #IN:arctan2.X main_score
scoreboard players operation #norm.z main_score /= #IN:arctan2.X main_score


