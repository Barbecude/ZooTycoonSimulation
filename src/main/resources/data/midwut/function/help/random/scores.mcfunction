#> midwut:help/random/scores

# §6 En
## §e Input data:
### scoreboard players get #random_min main_score - Minimum (including)
### scoreboard players get #random_max main_score - Maximum (including)
## §e Output data:
### scoreboard players get #random main_score - Random number

# §6 Ru
## §e Входные данные:
### scoreboard players get #random_min main_score - Минимум (включая)
### scoreboard players get #random_max main_score - Максимум (включая)
## §e Выходные данные:
### scoreboard players get #random main_score - Рандомное число

# 
scoreboard players operation #random.seed main_score += #seed const
scoreboard players operation #random.seed main_score += #gametime const

scoreboard players operation #random main_score = #random.seed main_score
scoreboard players operation #random main_score *= #random.seed main_score
scoreboard players operation #random main_score /= #10 const
scoreboard players operation #random main_score %= #100000000 const

scoreboard players add #random_max main_score 1
scoreboard players operation #random_max main_score -= #random_min main_score
scoreboard players operation #random.seed main_score = #random main_score
scoreboard players operation #random main_score %= #random_max main_score
scoreboard players operation #random main_score += #random_min main_score
scoreboard players operation #random_max main_score += #random_min main_score
scoreboard players remove #random_max main_score 1


