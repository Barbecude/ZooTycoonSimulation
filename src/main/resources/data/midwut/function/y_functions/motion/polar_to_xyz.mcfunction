$tp ^ ^ ^$(temp)
data modify storage midwut:main Pos set from entity @s Pos
execute store result score #midwut.motion.x main_score run data get storage midwut:main Pos[0]
execute store result score #midwut.motion.y main_score run data get storage midwut:main Pos[1]
execute store result score #midwut.motion.z main_score run data get storage midwut:main Pos[2]
function midwut:return_marker