execute store result score #blockf main_score run data get storage midwut:main macros.step 1
execute store result score #blockd main_score run data get storage midwut:main macros.step 1000
scoreboard players operation #blockf main_score *= #1000 const

data modify storage midwut:main temp set value {pixel:8,material:"smooth_stone",colors:[]}
execute store result storage midwut:main temp.pixel int 0.016 run scoreboard players operation #blockd main_score -= #blockf main_score

execute if data storage midwut:main temp{pixel:0} run return fail

function midwut:test/gen_krivaya/rampa0/setblock_pos with storage midwut:main macros