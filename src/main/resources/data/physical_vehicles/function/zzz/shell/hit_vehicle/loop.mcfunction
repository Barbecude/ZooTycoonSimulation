execute store result score #nx main_score run data get storage midwut:main boxes[-1].box[0] 1
execute store result score #ny main_score run data get storage midwut:main boxes[-1].box[1] 1
execute store result score #nz main_score run data get storage midwut:main boxes[-1].box[2] 1
execute store result score #px main_score run data get storage midwut:main boxes[-1].box[3] 1
execute store result score #py main_score run data get storage midwut:main boxes[-1].box[4] 1
execute store result score #pz main_score run data get storage midwut:main boxes[-1].box[5] 1
execute if score #x2 main_score >= #nx main_score if score #x2 main_score <= #px main_score if score #y2 main_score >= #ny main_score if score #y2 main_score <= #py main_score if score #z2 main_score >= #nz main_score if score #z2 main_score <= #pz main_score if function physical_vehicles:zzz/shell/hit_vehicle/rotated_box run return 1

data remove storage midwut:main boxes[-1]
execute if data storage midwut:main boxes[0] run function physical_vehicles:zzz/shell/hit_vehicle/loop