execute align xyz run summon frog ~ ~ ~0.99999 {NoAI:1b,Silent:1b,Team:"no_coll",Rotation:[-90f,0f],Tags:["midwut.frog_mc_life"],variant:"warm"}
execute align xyz run summon frog ~0.5 ~ ~0.5 {NoAI:1b,Silent:1b,Team:"no_coll",Rotation:[-90f,0f],Tags:["midwut.frog_mc_life","main"]}
execute align xyz run summon frog ~ ~ ~ {NoAI:1b,Silent:1b,Team:"no_coll",Rotation:[-90f,0f],Tags:["midwut.frog_mc_life"],variant:"cold"}


schedule function midwut:test/frogs/tick 5s
