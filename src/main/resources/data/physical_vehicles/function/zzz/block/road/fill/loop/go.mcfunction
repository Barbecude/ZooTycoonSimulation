#function physical_vehicles:zzz/block/road/fill/loop/go {block:"",up:1,down:0,north:0,south:0,west:0,east:0}
execute store result score #temp main_score run gamerule minecraft:max_command_sequence_length
execute if score #temp main_score matches 10000000.. run return fail
$data modify storage midwut:main macros.temp set value $(block)
$scoreboard players set #up main_score $(up)
$scoreboard players set #down main_score $(down)
$scoreboard players set #north main_score $(north)
$scoreboard players set #south main_score $(south)
$scoreboard players set #west main_score $(west)
$scoreboard players set #east main_score $(east)
#gamerule max_block_modifications 100000
function physical_vehicles:zzz/block/road/fill/loop/loop with storage midwut:main macros
