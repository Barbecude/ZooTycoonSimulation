data modify storage midwut:main Motion set from entity @s Motion
execute store result storage midwut:main Motion[0] double 0.000003 run data get storage midwut:main Motion[0] 1000000
execute store result storage midwut:main Motion[1] double 0.000003 run data get storage midwut:main Motion[1] 1000000
execute store result storage midwut:main Motion[2] double 0.000003 run data get storage midwut:main Motion[2] 1000000

tp ~1000000 ~ ~
data modify entity @s Motion set from storage midwut:main Motion
#data remove entity @s Air
tp ~ ~ ~
data modify entity @s Motion set from storage midwut:main Motion