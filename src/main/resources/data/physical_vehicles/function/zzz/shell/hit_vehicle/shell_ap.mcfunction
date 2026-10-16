execute store result score #penetration main_score run data get storage midwut:main DataShell.data.phys_veh.shell.penetration 10
execute store result score #start_velocity main_score run data get storage midwut:main DataShell.data.phys_veh.shell.start_velocity 0.5
scoreboard players operation #hit_velocity main_score = #velocity main_score
scoreboard players operation #velocity main_score *= #1000 const
scoreboard players operation #velocity main_score /= #start_velocity main_score
scoreboard players operation #velocity main_score *= #velocity main_score
scoreboard players operation #velocity main_score /= #1000 const
scoreboard players operation #penetration main_score *= #velocity main_score
scoreboard players operation #penetration main_score /= #1000 const

#tellraw @a [{score:{name:"#thickness",objective:"main_score"}}]


scoreboard players operation #n2.x main_score = #m00 main_score
scoreboard players operation #n2.x main_score *= #n.x main_score
scoreboard players operation #tx2 main_score = #m01 main_score
scoreboard players operation #tx2 main_score *= #n.y main_score
scoreboard players operation #tx3 main_score = #m02 main_score
scoreboard players operation #tx3 main_score *= #n.z main_score

scoreboard players operation #n2.y main_score = #m10 main_score
scoreboard players operation #n2.y main_score *= #n.x main_score
scoreboard players operation #ty2 main_score = #m11 main_score
scoreboard players operation #ty2 main_score *= #n.y main_score
scoreboard players operation #ty3 main_score = #m12 main_score
scoreboard players operation #ty3 main_score *= #n.z main_score

scoreboard players operation #n2.z main_score = #m20 main_score
scoreboard players operation #n2.z main_score *= #n.x main_score
scoreboard players operation #tz2 main_score = #m21 main_score
scoreboard players operation #tz2 main_score *= #n.y main_score
scoreboard players operation #tz3 main_score = #m22 main_score
scoreboard players operation #tz3 main_score *= #n.z main_score

scoreboard players operation #n2.x main_score += #tx2 main_score
scoreboard players operation #n2.x main_score += #tx3 main_score

scoreboard players operation #n2.y main_score += #ty2 main_score
scoreboard players operation #n2.y main_score += #ty3 main_score

scoreboard players operation #n2.z main_score += #tz2 main_score
scoreboard players operation #n2.z main_score += #tz3 main_score

scoreboard players operation #n2.x main_score /= #10000 const
scoreboard players operation #n2.y main_score /= #10000 const
scoreboard players operation #n2.z main_score /= #10000 const

#tellraw @a [{score:{name:"#n2.x",objective:"main_score"}},"  ",{score:{name:"#n2.y",objective:"main_score"}},"  ",{score:{name:"#n2.z",objective:"main_score"}}]
execute in midwut:main positioned 0.0 0.0 0.0 positioned ^ ^ ^1 run tp 0000051d-0000-0fb7-0000-001500000014 ~ ~ ~
execute as 0000051d-0000-0fb7-0000-001500000014 run function physical_vehicles:zzz/shell/hit_vehicle/get_direction

#tellraw @a [{score:{name:"#impx",objective:"main_score"}},"  ",{score:{name:"#impy",objective:"main_score"}},"  ",{score:{name:"#impz",objective:"main_score"}}]
scoreboard players operation #impx main_score *= #n2.x main_score
scoreboard players operation #impy main_score *= #n2.y main_score
scoreboard players operation #impz main_score *= #n2.z main_score
scoreboard players operation #impx main_score += #impy main_score
scoreboard players operation #impx main_score += #impz main_score
scoreboard players operation #impx main_score /= #-1000 const
#tellraw @a [{score:{name:"#impx",objective:"main_score"}}]
execute if score #impx main_score matches ..0 run return fail

scoreboard players operation #thickness main_score *= #1000 const
scoreboard players operation #thickness main_score /= #impx main_score
#tellraw @a [{score:{name:"#thickness",objective:"main_score"}}]