execute if entity @p[tag=rf,tag=r] run fill ~ 1 ~ ~ ~-1 ~ stone
execute if entity @p[tag=rf2,tag=r] run fill ~ ~-2 ~ ~ ~-1 ~ stone_bricks
execute if entity @p[tag=rf3,tag=r] run fill ~ ~-1 ~ ~ ~-1 ~ stone_bricks replace air
execute if entity @p[tag=rf4,tag=r] run fill ~ ~-2 ~ ~ ~-1 ~ stone_bricks replace air
execute if entity @p[tag=rf5,tag=r] run fill ~ 1 ~ ~ ~-1 ~ stone replace air
setblock ~ ~ ~ black_concrete_powder