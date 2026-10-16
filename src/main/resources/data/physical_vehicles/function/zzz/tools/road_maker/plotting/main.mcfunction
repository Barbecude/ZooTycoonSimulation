# P(t) = (2t³ - 3t² + 1) * P0 + (t³ - 2t² + t) * M0 + (-2t³ + 3t²) * P1 + (t³ - t²) * M1

scoreboard players operation #a main_score = #t main_score
scoreboard players operation #a main_score *= #t main_score
scoreboard players operation #a main_score /= #10000 const
scoreboard players operation #a main_score *= #t main_score
scoreboard players operation #a main_score /= #50000 const

scoreboard players operation #b main_score = #t main_score
scoreboard players operation #b main_score *= #t main_score
scoreboard players operation #b main_score /= #100000 const
scoreboard players operation #b main_score *= #3 const

scoreboard players operation #a main_score -= #b main_score
scoreboard players add #a main_score 1000

scoreboard players operation #p.x main_score = #p0.x main_score
scoreboard players operation #p.y main_score = #p0.y main_score
scoreboard players operation #p.z main_score = #p0.z main_score
scoreboard players operation #p.x main_score *= #a main_score
scoreboard players operation #p.y main_score *= #a main_score
scoreboard players operation #p.z main_score *= #a main_score
scoreboard players operation #p.x main_score /= #1000 const
scoreboard players operation #p.y main_score /= #1000 const
scoreboard players operation #p.z main_score /= #1000 const



scoreboard players operation #a main_score = #t main_score
scoreboard players operation #a main_score *= #t main_score
scoreboard players operation #a main_score /= #10000 const
scoreboard players operation #a main_score *= #t main_score
scoreboard players operation #a main_score /= #10000 const

scoreboard players operation #b main_score = #t main_score
scoreboard players operation #b main_score *= #t main_score
scoreboard players operation #b main_score /= #5000 const

scoreboard players operation #a main_score -= #b main_score
scoreboard players operation #a main_score += #t main_score
scoreboard players operation #a main_score /= #10 const

scoreboard players operation #t.x main_score = #m0.x main_score
scoreboard players operation #t.y main_score = #m0.y main_score
scoreboard players operation #t.z main_score = #m0.z main_score
scoreboard players operation #t.x main_score *= #a main_score
scoreboard players operation #t.y main_score *= #a main_score
scoreboard players operation #t.z main_score *= #a main_score
scoreboard players operation #t.x main_score /= #1000 const
scoreboard players operation #t.y main_score /= #1000 const
scoreboard players operation #t.z main_score /= #1000 const

scoreboard players operation #p.x main_score += #t.x main_score
scoreboard players operation #p.y main_score += #t.y main_score
scoreboard players operation #p.z main_score += #t.z main_score



scoreboard players operation #a main_score = #t main_score
scoreboard players operation #a main_score *= #t main_score
scoreboard players operation #a main_score /= #100000 const
scoreboard players operation #a main_score *= #3 const

scoreboard players operation #b main_score = #t main_score
scoreboard players operation #b main_score *= #t main_score
scoreboard players operation #b main_score /= #10000 const
scoreboard players operation #b main_score *= #t main_score
scoreboard players operation #b main_score /= #50000 const

scoreboard players operation #a main_score -= #b main_score

scoreboard players operation #t.x main_score = #p1.x main_score
scoreboard players operation #t.y main_score = #p1.y main_score
scoreboard players operation #t.z main_score = #p1.z main_score
scoreboard players operation #t.x main_score *= #a main_score
scoreboard players operation #t.y main_score *= #a main_score
scoreboard players operation #t.z main_score *= #a main_score
scoreboard players operation #t.x main_score /= #1000 const
scoreboard players operation #t.y main_score /= #1000 const
scoreboard players operation #t.z main_score /= #1000 const

scoreboard players operation #p.x main_score += #t.x main_score
scoreboard players operation #p.y main_score += #t.y main_score
scoreboard players operation #p.z main_score += #t.z main_score



scoreboard players operation #a main_score = #t main_score
scoreboard players operation #a main_score *= #t main_score
scoreboard players operation #a main_score /= #10000 const
scoreboard players operation #a main_score *= #t main_score
scoreboard players operation #a main_score /= #100000 const

scoreboard players operation #b main_score = #t main_score
scoreboard players operation #b main_score *= #t main_score
scoreboard players operation #b main_score /= #100000 const

scoreboard players operation #a main_score -= #b main_score

scoreboard players operation #t.x main_score = #m1.x main_score
scoreboard players operation #t.y main_score = #m1.y main_score
scoreboard players operation #t.z main_score = #m1.z main_score
scoreboard players operation #t.x main_score *= #a main_score
scoreboard players operation #t.y main_score *= #a main_score
scoreboard players operation #t.z main_score *= #a main_score
scoreboard players operation #t.x main_score /= #1000 const
scoreboard players operation #t.y main_score /= #1000 const
scoreboard players operation #t.z main_score /= #1000 const

execute store result storage midwut:main macros.x float 0.01 run scoreboard players operation #p.x main_score += #t.x main_score
execute store result storage midwut:main macros.y float 0.01 run scoreboard players operation #p.y main_score += #t.y main_score
execute store result storage midwut:main macros.z float 0.01 run scoreboard players operation #p.z main_score += #t.z main_score



# P'(t) = (6t² - 6t) * P0 + (3t² - 4t + 1) * M0 + (-6t² + 6t) * P1 + (3t² - 2t) * M1


scoreboard players operation #a main_score = #t main_score
scoreboard players operation #a main_score *= #t main_score
scoreboard players operation #a main_score /= #100000 const

scoreboard players operation #b main_score = #t main_score
scoreboard players operation #b main_score /= #10 const

scoreboard players operation #a main_score -= #b main_score
scoreboard players operation #a main_score *= #6 const

scoreboard players operation #p.x main_score = #p0.x main_score
scoreboard players operation #p.y main_score = #p0.y main_score
scoreboard players operation #p.z main_score = #p0.z main_score
scoreboard players operation #p.x main_score *= #a main_score
scoreboard players operation #p.y main_score *= #a main_score
scoreboard players operation #p.z main_score *= #a main_score
scoreboard players operation #p.x main_score /= #1000 const
scoreboard players operation #p.y main_score /= #1000 const
scoreboard players operation #p.z main_score /= #1000 const



scoreboard players operation #a main_score = #t main_score
scoreboard players operation #a main_score *= #t main_score
scoreboard players operation #a main_score /= #100000 const
scoreboard players operation #a main_score *= #3 const

scoreboard players operation #b main_score = #t main_score
scoreboard players operation #b main_score /= #10 const
scoreboard players operation #b main_score *= #4 const

scoreboard players operation #a main_score -= #b main_score
scoreboard players add #a main_score 1000

scoreboard players operation #t.x main_score = #m0.x main_score
scoreboard players operation #t.y main_score = #m0.y main_score
scoreboard players operation #t.z main_score = #m0.z main_score
scoreboard players operation #t.x main_score *= #a main_score
scoreboard players operation #t.y main_score *= #a main_score
scoreboard players operation #t.z main_score *= #a main_score
scoreboard players operation #t.x main_score /= #1000 const
scoreboard players operation #t.y main_score /= #1000 const
scoreboard players operation #t.z main_score /= #1000 const

scoreboard players operation #p.x main_score += #t.x main_score
scoreboard players operation #p.y main_score += #t.y main_score
scoreboard players operation #p.z main_score += #t.z main_score



scoreboard players operation #a main_score = #t main_score
scoreboard players operation #a main_score /= #10 const

scoreboard players operation #b main_score = #t main_score
scoreboard players operation #b main_score *= #t main_score
scoreboard players operation #b main_score /= #100000 const

scoreboard players operation #a main_score -= #b main_score
scoreboard players operation #a main_score *= #6 const

scoreboard players operation #t.x main_score = #p1.x main_score
scoreboard players operation #t.y main_score = #p1.y main_score
scoreboard players operation #t.z main_score = #p1.z main_score
scoreboard players operation #t.x main_score *= #a main_score
scoreboard players operation #t.y main_score *= #a main_score
scoreboard players operation #t.z main_score *= #a main_score
scoreboard players operation #t.x main_score /= #1000 const
scoreboard players operation #t.y main_score /= #1000 const
scoreboard players operation #t.z main_score /= #1000 const

scoreboard players operation #p.x main_score += #t.x main_score
scoreboard players operation #p.y main_score += #t.y main_score
scoreboard players operation #p.z main_score += #t.z main_score



scoreboard players operation #a main_score = #t main_score
scoreboard players operation #a main_score *= #t main_score
scoreboard players operation #a main_score /= #100000 const
scoreboard players operation #a main_score *= #3 const

scoreboard players operation #b main_score = #t main_score
scoreboard players operation #b main_score /= #5 const

scoreboard players operation #a main_score -= #b main_score

scoreboard players operation #t.x main_score = #m1.x main_score
scoreboard players operation #t.y main_score = #m1.y main_score
scoreboard players operation #t.z main_score = #m1.z main_score
scoreboard players operation #t.x main_score *= #a main_score
scoreboard players operation #t.y main_score *= #a main_score
scoreboard players operation #t.z main_score *= #a main_score
scoreboard players operation #t.x main_score /= #1000 const
scoreboard players operation #t.y main_score /= #1000 const
scoreboard players operation #t.z main_score /= #1000 const

execute store result storage midwut:main macros.vx float 0.01 run scoreboard players operation #p.x main_score += #t.x main_score
execute store result storage midwut:main macros.vy float 0.01 run scoreboard players operation #p.y main_score += #t.y main_score
execute store result storage midwut:main macros.vz float 0.01 run scoreboard players operation #p.z main_score += #t.z main_score


scoreboard players operation #t2 main_score = #t main_score
scoreboard players operation #t2 main_score /= #10 const
scoreboard players operation #a main_score = #angle1 main_score
scoreboard players operation #a main_score *= #t2 main_score
#scoreboard players operation #a main_score /= #1000 const

scoreboard players remove #t2 main_score 1000
scoreboard players operation #b main_score = #angle0 main_score
scoreboard players operation #b main_score *= #t2 main_score
#scoreboard players operation #b main_score /= #1000 const

execute store result storage midwut:main macros.angle float 0.000001 run scoreboard players operation #a main_score -= #b main_score


execute if score #make main_score matches 0 run function physical_vehicles:zzz/tools/road_maker/plotting/summon_pre
execute if score #make main_score matches 1.. run function physical_vehicles:zzz/tools/road_maker/plotting/block_pos with storage midwut:main macros
execute if score #make main_score > @s phys_veh.afk_timer run return run scoreboard players set #make main_score 1

scoreboard players add #t main_score 1
execute unless score #t main_score matches 10001.. run function physical_vehicles:zzz/tools/road_maker/plotting/main