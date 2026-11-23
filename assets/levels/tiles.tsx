<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.10.2" name="tiles" tilewidth="16" tileheight="16" tilecount="7" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <tile id="0">
  <properties>
   <property name="blocksMovement" type="bool" value="false"/>
   <property name="blocksSpells" type="bool" value="false"/>
   <property name="tileType" value="grass"/>
  </properties>
  <image width="16" height="16" source="../tiles/spawn_grass.png"/>
 </tile>
 <tile id="1">
  <properties>
   <property name="blocksMovement" type="bool" value="false"/>
   <property name="blocksSpells" type="bool" value="false"/>
   <property name="tileType" value="floor"/>
  </properties>
  <image width="16" height="16" source="../tiles/spawn_floor.png"/>
 </tile>
 <tile id="2">
  <properties>
   <property name="blocksMovement" type="bool" value="true"/>
   <property name="blocksSpells" type="bool" value="false"/>
   <property name="tileType" value="water"/>
  </properties>
  <image width="16" height="16" source="../tiles/spawn_water.png"/>
 </tile>
 <tile id="3">
  <properties>
   <property name="blocksMovement" type="bool" value="true"/>
   <property name="blocksSpells" type="bool" value="true"/>
   <property name="tileType" value="hedge"/>
  </properties>
  <image width="16" height="16" source="../tiles/spawn_hedge.png"/>
 </tile>
 <tile id="4">
  <properties>
   <property name="blocksMovement" type="bool" value="true"/>
   <property name="blocksSpells" type="bool" value="true"/>
   <property name="tileType" value="wall"/>
  </properties>
  <image width="16" height="16" source="../tiles/spawn_wall.png"/>
 </tile>
 <tile id="5">
  <properties>
   <property name="blocksMovement" type="bool" value="true"/>
   <property name="blocksSpells" type="bool" value="true"/>
   <property name="tileType" value="wall_alt"/>
  </properties>
  <image width="16" height="16" source="../tiles/spawn_wall_alt.png"/>
 </tile>
 <tile id="6">
  <properties>
   <property name="blocksMovement" type="bool" value="true"/>
   <property name="blocksSpells" type="bool" value="true"/>
   <property name="tileType" value="void_tile"/>
  </properties>
  <image width="16" height="16" source="../tiles/void_tile.png"/>
 </tile>
</tileset>
