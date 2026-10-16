clear @s *[minecraft:custom_data~{midwut.next_page:true}]
execute store result storage midwut:main midwut_crafting_table.page int -1 run data get storage midwut:main midwut_crafting_table.page -1.0000000001
execute if data storage midwut:main midwut_crafting_table{page:0} run data modify storage midwut:main midwut_crafting_table.page set value 1
playsound minecraft:ui.button.click ui @s ~ ~ ~ 0.5 1