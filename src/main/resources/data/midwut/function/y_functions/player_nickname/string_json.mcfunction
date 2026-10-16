loot replace block 0 0 0 container.0 loot midwut:player_head
data modify storage midwut:main PlayerName set value {string:"ERROR!",json:{"text":"ERROR!"}}
data modify storage midwut:main PlayerName.string set from block 0 0 0 Items[0].components."minecraft:profile".name
data modify storage midwut:main PlayerName.json.text set from storage midwut:main PlayerName.string
