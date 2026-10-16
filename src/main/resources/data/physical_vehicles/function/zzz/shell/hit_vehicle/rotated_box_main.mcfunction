scoreboard players operation #id_test main_score = @s phys_veh.id.vehicle
execute store result score #object_id main_score run data get storage midwut:main boxes[-1].rotated.object_id
execute as @e[type=#physical_vehicles:refers_to_vehicle/all,tag=physical_vehicle,predicate=physical_vehicles:same_scores/vehicle_id,predicate=physical_vehicles:same_scores/object_id,limit=1,x=0] run function physical_vehicles:zzz/shell/hit_vehicle/rotated_box_matrix

execute store result score #ofx main_score run data get storage midwut:main Pos2[0] 1000
execute store result score #ofy main_score run data get storage midwut:main Pos2[1] 1000
execute store result score #ofz main_score run data get storage midwut:main Pos2[2] 1000

execute store result score #temp main_score run data get storage midwut:main Pos[0] 1000
#execute unless score #te main_score matches 1 run tellraw @a [{score:{name:"#temp",objective:"main_score"}},"  ",{score:{name:"#ofx",objective:"main_score"}}]
scoreboard players operation #ofx main_score -= #temp main_score
execute store result score #temp main_score run data get storage midwut:main Pos[1] 1000
scoreboard players operation #ofy main_score -= #temp main_score
execute store result score #temp main_score run data get storage midwut:main Pos[2] 1000
scoreboard players operation #ofz main_score -= #temp main_score

#execute unless score #te main_score matches 1 run tellraw @a [{score:{name:"#ofx",objective:"main_score"}},"  ",{score:{name:"#ofy",objective:"main_score"}},"  ",{score:{name:"#ofz",objective:"main_score"}}]


scoreboard players operation #tx main_score = #vx main_score
scoreboard players operation #ty main_score = #vy main_score
scoreboard players operation #tz main_score = #vz main_score
#execute unless score #te main_score matches 1 run tellraw @a [{score:{name:"#tx",objective:"main_score"}},"  ",{score:{name:"#ty",objective:"main_score"}},"  ",{score:{name:"#tz",objective:"main_score"}}]
#execute unless score #te main_score matches 1 run tellraw @a [{score:{name:"#ofx",objective:"main_score"}},"  ",{score:{name:"#ofy",objective:"main_score"}},"  ",{score:{name:"#ofz",objective:"main_score"}}]
scoreboard players operation #tx main_score -= #ofx main_score
scoreboard players operation #ty main_score -= #ofy main_score
scoreboard players operation #tz main_score -= #ofz main_score
#execute unless score #te main_score matches 1 run tellraw @a [{score:{name:"#tx",objective:"main_score"}},"  ",{score:{name:"#ty",objective:"main_score"}},"  ",{score:{name:"#tz",objective:"main_score"}}]

scoreboard players operation #x3 main_score = #m00 main_score
scoreboard players operation #x3 main_score *= #tx main_score
scoreboard players operation #tx2 main_score = #m10 main_score
scoreboard players operation #tx2 main_score *= #ty main_score
scoreboard players operation #tx3 main_score = #m20 main_score
scoreboard players operation #tx3 main_score *= #tz main_score

scoreboard players operation #y3 main_score = #m01 main_score
scoreboard players operation #y3 main_score *= #tx main_score
scoreboard players operation #ty2 main_score = #m11 main_score
scoreboard players operation #ty2 main_score *= #ty main_score
scoreboard players operation #ty3 main_score = #m21 main_score
scoreboard players operation #ty3 main_score *= #tz main_score

scoreboard players operation #z3 main_score = #m02 main_score
scoreboard players operation #z3 main_score *= #tx main_score
scoreboard players operation #tz2 main_score = #m12 main_score
scoreboard players operation #tz2 main_score *= #ty main_score
scoreboard players operation #tz3 main_score = #m22 main_score
scoreboard players operation #tz3 main_score *= #tz main_score

scoreboard players operation #x3 main_score += #tx2 main_score
scoreboard players operation #x3 main_score += #tx3 main_score

scoreboard players operation #y3 main_score += #ty2 main_score
scoreboard players operation #y3 main_score += #ty3 main_score

scoreboard players operation #z3 main_score += #tz2 main_score
scoreboard players operation #z3 main_score += #tz3 main_score

scoreboard players operation #x3 main_score /= #10000 const
scoreboard players operation #y3 main_score /= #10000 const
scoreboard players operation #z3 main_score /= #10000 const

execute store result score #nx main_score run data get storage midwut:main boxes[-1].rotated.box[0] 1000
execute store result score #ny main_score run data get storage midwut:main boxes[-1].rotated.box[1] 1000
execute store result score #nz main_score run data get storage midwut:main boxes[-1].rotated.box[2] 1000
execute store result score #px main_score run data get storage midwut:main boxes[-1].rotated.box[3] 1000
execute store result score #py main_score run data get storage midwut:main boxes[-1].rotated.box[4] 1000
execute store result score #pz main_score run data get storage midwut:main boxes[-1].rotated.box[5] 1000
#execute unless score #te main_score matches 1 run tellraw @a [{score:{name:"#x3",objective:"main_score"}},"  ",{score:{name:"#y3",objective:"main_score"}},"  ",{score:{name:"#z3",objective:"main_score"}}]
#execute unless score #te main_score matches 1 run tellraw @a [{score:{name:"#nx",objective:"main_score"}},"  ",{score:{name:"#ny",objective:"main_score"}},"  ",{score:{name:"#nz",objective:"main_score"}}]
#execute unless score #te main_score matches 1 run tellraw @a [{score:{name:"#px",objective:"main_score"}},"  ",{score:{name:"#py",objective:"main_score"}},"  ",{score:{name:"#pz",objective:"main_score"}}]
#scoreboard players set #te main_score 1
return run execute if score #x3 main_score >= #nx main_score if score #x3 main_score <= #px main_score if score #y3 main_score >= #ny main_score if score #y3 main_score <= #py main_score if score #z3 main_score >= #nz main_score if score #z3 main_score <= #pz main_score run function physical_vehicles:zzz/shell/hit_vehicle/rotated_data