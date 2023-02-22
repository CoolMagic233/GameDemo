package org.sobadfish.gamedemo.event;

import cn.nukkit.plugin.Plugin;
import org.sobadfish.gamedemo.player.PlayerInfo;
import org.sobadfish.gamedemo.room.GameRoom;


/**
 * 玩家在游戏中死亡的事件
 * @author SoBadFish
 * 2022/1/15
 */
public class PlayerGameDeathEvent extends PlayerRoomInfoEvent{

    private PlayerInfo damager;
    public PlayerGameDeathEvent(PlayerInfo playerInfo, GameRoom room, Plugin plugin) {
        super(playerInfo,room, plugin);
    }

    public void setDamager(PlayerInfo damager) {
        this.damager = damager;
    }

    public PlayerInfo getDamager() {
        return damager;
    }
}
