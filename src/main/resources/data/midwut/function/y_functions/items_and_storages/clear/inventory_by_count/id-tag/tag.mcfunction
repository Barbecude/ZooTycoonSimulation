execute unless data storage midwut:main temp.tag unless data storage midwut:main Items2[-1].tag run return 0
execute if data storage midwut:main temp.tag unless data storage midwut:main Items2[-1].tag run return 1
execute unless data storage midwut:main temp.tag if data storage midwut:main Items2[-1].tag run return 1
$execute store result score #test main_score unless data storage midwut:main temp.tag$(tag)