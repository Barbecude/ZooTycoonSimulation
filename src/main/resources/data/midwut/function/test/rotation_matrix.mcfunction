
# scoreboard players operation #offset.pos_z main_score = @s offset.x
# scoreboard players operation #offset.pos_y main_score = @s offset.y
# scoreboard players operation #offset.pos_x main_score = @s offset.z

## #cos_x main_score
## #sin_x main_score

## #cos_y main_score
## #sin_y main_score

## #cos_z main_score
## #sin_z main_score


## #offset_x_ main_score
## #offset_y_ main_score
## #offset_z_ main_score


###### X
## cos  sin 0   offset_z_
## -sin cos 0   offset_y_
## 0    0   1   offset_x_

###### Z
## offset_z_ = cos sin 0
scoreboard players operation #test main_score = #offset.pos_z main_score
scoreboard players operation #test main_score *= #cos_x main_score
scoreboard players operation #offset_z_ main_score = #test main_score
scoreboard players operation #test main_score = #offset.pos_y main_score
scoreboard players operation #test main_score *= #sin_x main_score
scoreboard players operation #offset_z_ main_score += #test main_score
scoreboard players operation #offset_z_ main_score /= #1000 const

## offset_y_ = -sin cos 0
scoreboard players operation #offset.pos_y main_score *= #cos_x main_score
scoreboard players operation #offset_y_ main_score = #offset.pos_y main_score
scoreboard players operation #offset.pos_z main_score *= #sin_x main_score
scoreboard players operation #offset_y_ main_score -= #offset.pos_z main_score
scoreboard players operation #offset_y_ main_score /= #1000 const

## offset_x_ = 0 0 1
scoreboard players operation #offset_x_ main_score = #offset.pos_x main_score


###### X
## 1  0  0      offset_z_
## 0 cos -sin   offset_y_
## 0 sin cos    offset_x_

scoreboard players operation #test2 main_score = #offset_y_ main_score
scoreboard players operation #test3 main_score = #offset_x_ main_score

## offset_y_ = 0 cos -sin
scoreboard players operation #test main_score = #test2 main_score
scoreboard players operation #test main_score *= #cos_z main_score
scoreboard players operation #offset_y_ main_score = #test main_score
scoreboard players operation #test main_score = #test3 main_score
scoreboard players operation #test main_score *= #sin_z main_score
scoreboard players operation #offset_y_ main_score -= #test main_score
scoreboard players operation #offset_y_ main_score /= #1000 const

## offset_x_ = 0 sin cos
scoreboard players operation #test3 main_score *= #cos_z main_score
scoreboard players operation #offset_x_ main_score = #test3 main_score
scoreboard players operation #test2 main_score *= #sin_z main_score
scoreboard players operation #offset_x_ main_score += #test2 main_score
scoreboard players operation #offset_x_ main_score /= #1000 const


###### Y
## cos  0 sin   offset_z_
## 0    1 0     offset_y_
## -sin 0 cos   offset_x_


scoreboard players operation #test2 main_score = #offset_z_ main_score
scoreboard players operation #test3 main_score = #offset_x_ main_score

## offset_z_ = cos 0 sin
scoreboard players operation #test main_score = #test2 main_score
scoreboard players operation #test main_score *= #cos_y main_score
scoreboard players operation #offset_z_ main_score = #test main_score
scoreboard players operation #test main_score = #test3 main_score
scoreboard players operation #test main_score *= #sin_y main_score
scoreboard players operation #offset_z_ main_score += #test main_score
scoreboard players operation #offset_z_ main_score /= #1000 const

## offset_x_ = -sin 0 cos
scoreboard players operation #test3 main_score *= #cos_y main_score
scoreboard players operation #offset_x_ main_score = #test3 main_score
scoreboard players operation #test2 main_score *= #sin_y main_score
scoreboard players operation #offset_x_ main_score -= #test2 main_score
scoreboard players operation #offset_x_ main_score /= #1000 const

