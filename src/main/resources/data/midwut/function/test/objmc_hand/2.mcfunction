execute store result score #y main_score run data get entity @s Rotation[0]
execute store result score #x main_score run data get entity @s Rotation[1]
#scoreboard players set #x main_score 0
#scoreboard players set #y main_score 90
#scoreboard players set #z main_score 0


scoreboard players operation #x main_score *= #708334 const
scoreboard players operation #y main_score *= #708334 const
#scoreboard players operation #z main_score *= #708334 const
scoreboard players operation #x main_score /= #1000000 const
scoreboard players operation #y main_score /= #1000000 const
#scoreboard players operation #z main_score /= #1000000 const

## и тут по формуле
scoreboard players operation #x main_score *= #16-bytes const
scoreboard players operation #y main_score *= #8-bytes const

scoreboard players operation #y main_score += #x main_score

data modify storage midwut:main Inventory set value []
data modify storage midwut:main Inventory append from entity @s Inventory[{Slot:-106b}]
#execute store result storage midwut:main Inventory[-1].Slot byte 1 run data get entity @s SelectedItemSlot
execute store result storage midwut:main Inventory[-1].tag.CustomPotionColor int 1 run scoreboard players get #y main_score
function midwut:help/inventory_modifier/add_merge

#execute store result entity @s ArmorItems[3].tag.CustomPotionColor int 1 run scoreboard players get #z main_score
