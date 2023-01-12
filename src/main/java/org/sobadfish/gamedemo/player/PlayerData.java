package org.sobadfish.gamedemo.player;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.TextFormat;
import org.sobadfish.gamedemo.event.PlayerGetExpEvent;
import org.sobadfish.gamedemo.event.PlayerLevelChangeEvent;
import org.sobadfish.gamedemo.manager.FunctionManager;
import org.sobadfish.gamedemo.manager.TotalManager;
import org.sobadfish.gamedemo.tools.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * 玩家数据
 *
 * @author Sobadfish
 * */
public class PlayerData {

    //玩家名称
    private String name = "";

    //玩家经验
    private int exp;

    //玩家等级
    private int level;

    public PlayerData(String name){
        this.name = name;
    }

    public PlayerData(){}


    public List<RoomData> roomData = new ArrayList<>();

    public int getFinalData(DataType dataType){
        int c = 0;
        for(RoomData data: roomData){
            c += data.getInt(dataType);
        }
        return c;
    }

    public int getExp() {
        if(exp < 0){
            exp = 0;
        }
        return exp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void addExp(int exp, String cause){
        addExp(exp,cause,true);
    }


    public void addExp(int exp,String cause,boolean event){
        if(event){
            PlayerGetExpEvent expEvent = new PlayerGetExpEvent(name, exp,this.exp + exp,cause);
            Server.getInstance().getPluginManager().callEvent(expEvent);
            if(!expEvent.isCancelled()){
                exp = expEvent.getExp();
                displayExpMessage(expEvent);
            }else{
                return;
            }

        }

        if(this.exp < 0){
            this.exp = 0;
        }
        this.exp += exp;
        if(this.exp >= getNextLevelExp()){
            this.exp -= getNextLevelExp();
            PlayerLevelChangeEvent event1 = new PlayerLevelChangeEvent(name,level,1);
            Server.getInstance().getPluginManager().callEvent(event1);
            if(event1.isCancelled()){
                return;
            }
            level += event1.getNewLevel();

            int nExp = this.exp - getNextLevelExp();
            if(nExp > 0){
                this.exp -= getNextLevelExp();
                addExp(nExp,null,false);
            }
        }

    }

    /**
     * 当玩家获得经验时展示的信息
     * */
    private void displayExpMessage(PlayerGetExpEvent expEvent){
        Player player = Server.getInstance().getPlayer(name);
        if(player != null){
            player.sendMessage(TextFormat.colorize('&',TotalManager.getLanguage().getLanguage("player-getting-level-exp","&b[1] 经验([2])",expEvent.getExp()+"",expEvent.getCause())));
            PlayerInfo info = TotalManager.getRoomManager().getPlayerInfo(player);
            PlayerData data = TotalManager.getDataManager().getData(name);

            if(info == null || info.getGameRoom() == null){

                TotalManager.sendTipMessageToObject("&l&m"+ Utils.writeLine(5,"&a▁▁▁"),player);
                TotalManager.sendTipMessageToObject("&l"+Utils.writeLine(9,"&a﹉﹉"),player);
                String line = String.format("%20s","");
                player.sendMessage(line);
                String inputTitle = TotalManager.getLanguage().getLanguage("player-getting-level-exp-title","&b&l小游戏经验")+"\n";
                TotalManager.sendTipMessageToObject(FunctionManager.getCentontString(inputTitle,30),player);
                TotalManager.sendTipMessageToObject(FunctionManager.getCentontString(TotalManager.getLanguage().getLanguage("level-title","&b等级 ")+data.getLevel()+String.format("%"+inputTitle.length()+"s","")+TotalManager.language.getLanguage("level-title","&b等级 ")+(data.getLevel() + 1)+"\n",30),player);

                TotalManager.sendTipMessageToObject("&7["+data.getExpLine(20)+"&7]\n",player);

                String d = String.format("%.1f",data.getExpPercent() * 100.0);
                TotalManager.sendTipMessageToObject(FunctionManager.getCentontString("&b"+data.getExpString(data.getExp())+" &7/ &a"+data.getExpString(data.getNextLevelExp())+" &7("+d+"％)",40)+"\n",player);
                TotalManager.sendTipMessageToObject("&l&m"+Utils.writeLine(5,"&a▁▁▁"),player);
                TotalManager.sendTipMessageToObject("&l"+Utils.writeLine(9,"&a﹉﹉"),player);

            }
        }
    }

    /**
     * 获取等级百分比
     * */
    public double getExpPercent(){
        double r = 0;
        if(this.exp > 0){
            r = (double) this.exp / (double) getNextLevelExp();
        }
        return r;
    }

    /**
     * 获取等级条
     * */
    public String getExpLine(int size){
        return FunctionManager.drawLine((float) getExpPercent(),size,"&b■","&7■");
    }

    /**
     * 根据等级获取颜色
     * */
    public String getColorByLevel(int level){
        String[] color = new String[]{"&7","&f","&6","&b","&2","&3","&4","&d","&6","&e"};
        if(level < 100){
            return color[0];
        }else{
            return color[(level / 100) % 10];
        }

    }

    public String getLevelString(){
        String str = "✫";
        if(level > 1000){
            str = "✪";
        }
        return getColorByLevel(level)+level+str;
    }

    public String getExpString(int exp){
        double e = exp;
        e /= 1000;
        if(e < 10 && e >= 1){
            return String.format("%.1f",e)+"k";
        }else if(e > 10){
            e /= 10;
            if(e < 1000){
                return String.format("%.1f",e)+"w";
            }else{
                return String.format("%.1f",e)+"bill";
            }
        }else{
            return String.format("%.1f",(double)exp);
        }
    }

    public int getNextLevelExp(){
        double l = level;
         l+= 1;
        if(l > 100){
            l = l / 100.0;
            l = l - (int) l;
            l *= 100;
            if(l <= 0){
                l = 1;
            }
        }
       return (int)l * TotalManager.getUpExp();
    }



    public static class RoomData{

        public String roomName = "";

        public LinkedHashMap<DataType,Integer> sourceData = new LinkedHashMap<>();

        public void addSource(DataType dataType, int source) {
            if(sourceData.containsKey(dataType)){
                sourceData.put(dataType,sourceData.get(dataType) + source);
            }
            sourceData.put(dataType, source);
        }



        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RoomData roomData = (RoomData) o;
            return Objects.equals(roomName, roomData.roomName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(roomName);
        }

        public int getInt(DataType type){
            int c = 0;
            if(sourceData.containsKey(type)){
                c += sourceData.get(type);
            }
            return c;


        }
    }

    public RoomData getRoomData(String room){
        RoomData roomData = new RoomData();
        roomData.roomName = room;
        if(!this.roomData.contains(roomData)){
            this.roomData.add(roomData);
        }else{
            roomData = this.roomData.get(this.roomData.indexOf(roomData));
        }
        return roomData;
    }

    /**
     * 将缓存数据存储到PlayerData中
     * @param info PlayerInfo数据信息
     * */
    public void setInfo(PlayerInfo info){
        RoomData data = getRoomData(info.getGameRoom().getRoomConfig().name);
        for (DataType entry : info.statistics.keySet()) {
            data.addSource(DataType.DEATH,info.getData(entry));
        }
    }

    @Override
    public boolean equals(Object o) {
       if(o instanceof PlayerData){
           return ((PlayerData) o).name.equalsIgnoreCase(name);
       }
       return false;
    }

    public enum DataType{
        /**
         * 击杀
         * */
        KILL(TotalManager.getLanguage().getLanguage("type-kill","击杀")),
        /**
         * 死亡
         * */
        DEATH(TotalManager.getLanguage().getLanguage("type-death","死亡")),
        /**
         * 胜利
         * */
        VICTORY(TotalManager.getLanguage().getLanguage("type-victory","胜利")),
        /**
         * 失败
         * */
        DEFEAT(TotalManager.getLanguage().getLanguage("type-defeat","失败")),
        /**
         * 助攻
         * */
        ASSISTS(TotalManager.getLanguage().getLanguage("type-assists","助攻")),

        /**
         * 游戏次数
         * */
        GAME(TotalManager.getLanguage().getLanguage("type-game","游戏次数"));

        private final String name;

        DataType(String name){
            this.name = name;
        }


        public String getName() {
            return name;
        }

        public static DataType byName(String name){
            for(DataType type: values()){
                if(type.getName().equalsIgnoreCase(name)){
                    return type;
                }
            }
            return null;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
