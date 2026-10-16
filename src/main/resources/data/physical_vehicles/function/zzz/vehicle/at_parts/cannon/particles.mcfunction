$execute positioned ^ ^ ^$(barrel_length) run particle minecraft:flash{color:$(color)} ~ ~ ~ 0 0 0 0 1 force
$execute positioned ^ ^ ^$(barrel_length) run particle minecraft:explosion ~ ~ ~ $(delta) $(delta) $(delta) 0 $(count) force
$execute positioned ^ ^ ^$(barrel_length) run particle minecraft:large_smoke ~ ~ ~ $(delta) $(delta) $(delta) $(speed) $(count) force
kill @s[type=!minecraft:player]