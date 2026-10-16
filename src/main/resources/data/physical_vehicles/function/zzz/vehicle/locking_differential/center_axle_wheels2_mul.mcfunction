execute if score #w main_score matches 1.. if score #mul main_score > #w main_score run return run scoreboard players set #temp main_score 1
execute if score #w main_score matches ..-1 if score #mul main_score < #w main_score run return run scoreboard players set #temp main_score -1
scoreboard players set #temp main_score 0