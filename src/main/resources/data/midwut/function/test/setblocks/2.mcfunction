clone ~ ~ ~ ~ ~ ~ ~ 319 ~

gamerule block_drops false
setblock ~ ~ ~ air destroy
gamerule block_drops true

#clone ~ 319 ~ ~ 319 ~ ~ ~ ~
#setblock ~ 319 ~ air

#scoreboard players remove #setblocks main_score 1

#particle composter ~ ~ ~ 0.2 0.2 0.2 0 1 force

tag @s[scores={main_score=0}] remove midwut.setblocks