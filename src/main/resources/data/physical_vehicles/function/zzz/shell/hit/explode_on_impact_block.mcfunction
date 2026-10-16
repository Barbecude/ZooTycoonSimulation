#function physical_vehicles:zzz/shell/hit/explode_on_impact_block
$summon minecraft:tnt ~ ~ ~ {explosion_power:$(explosion_power),fuse:$(explosion_fuse)s,Tags:["physical_explosions.explode_anyway"],NoGravity:true}
particle minecraft:explosion ~ ~ ~ 0 0 0 0 1 force
