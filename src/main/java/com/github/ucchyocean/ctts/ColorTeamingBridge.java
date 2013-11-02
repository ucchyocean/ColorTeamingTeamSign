/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.ctts;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.github.ucchyocean.ct.ColorTeaming;
import com.github.ucchyocean.ct.ColorTeamingAPI;
import com.github.ucchyocean.ct.config.TeamNameConfig;
import com.github.ucchyocean.ct.config.TeamNameSetting;
import com.github.ucchyocean.ct.event.ColorTeamingPlayerLeaveEvent.Reason;

/**
 * カラーチーミング 連携クラス
 * @author ucchy
 */
public class ColorTeamingBridge {

    private ColorTeaming colorteaming;
    
    /**
     * コンストラクタ
     * @param colorteaming 
     */
    public ColorTeamingBridge(Plugin colorteaming) {
        this.colorteaming = (ColorTeaming)colorteaming;
    }
    
    /**
     * 指定されたチームが存在するかどうかを確認する
     * @param name チーム名、チーム表示名でも可
     * @return 存在するかどうか
     */
    public boolean isExistTeam(String name) {
        
        TeamNameConfig config = colorteaming.getAPI().getTeamNameConfig();
        if ( config.containsID(name) ) {
            return true;
        }
        
        for ( TeamNameSetting tns : config.getTeamNames() ) {
            if ( tns.getName().equals(name) ) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 指定されたプレイヤーが、既にチームに所属しているかどうかを確認する
     * @param player プレイヤー
     * @return 所属しているかどうか
     */
    public boolean isPlayerInTeam(Player player) {
        return (colorteaming.getAPI().getPlayerTeam(player) != null);
    }
    
    /**
     * TeamNameSettingを取得する
     * @param name チーム名、チーム表示名でも可
     * @return TeamNameSetting
     */
    private TeamNameSetting getTeamNameSetting(String name) {
        
        TeamNameConfig config = colorteaming.getAPI().getTeamNameConfig();
        if ( config.containsID(name) ) {
            return config.getTeamNameFromID(name);
        }
        
        for ( TeamNameSetting tns : config.getTeamNames() ) {
            if ( tns.getName().equals(name) ) {
                return tns;
            }
        }
        
        return null;
    }
    
    /**
     * 指定されたプレイヤーに指定されたチームを設定する
     * @param player プレイヤー
     * @param name チーム名
     * @return チーム設定を実行したかどうか
     */
    public boolean addPlayerToTeam(Player player, String name) {
        
        TeamNameSetting teamName = getTeamNameSetting(name);
        if ( teamName == null ) {
            return false;
        }
        
        ColorTeamingAPI api = colorteaming.getAPI();
        
        boolean result = (api.addPlayerTeam(player, teamName) != null);
        
        if ( !result ) {
            return false;
        }
        
        // サイドバー更新
        api.makeSidebarScore();
        
        return true;
    }
    
    /**
     * 指定されたプレイヤーを、人数の少ないチームへ参加させる。
     * @param player プレイヤー
     * @return チーム設定を実行したかどうか
     */
    public boolean addPlayerToRestTeam(Player player) {
        
        ColorTeamingAPI api = colorteaming.getAPI();
        ArrayList<Player> players = new ArrayList<Player>();
        players.add(player);
        return api.addPlayerToColorTeamsWithOrderSelection(players);
    }
    
    /**
     * 指定されたプレイヤーをチームから離脱させる。
     * @param player プレイヤー
     * @return チーム離脱を実行したかどうか
     */
    public boolean leavePlayerFromTeam(Player player) {
        
        ColorTeamingAPI api = colorteaming.getAPI();
        if ( api.getPlayerTeam(player) == null ) {
            return false;
        }
        
        api.leavePlayerTeam(player, Reason.SELF);
        
        // サイドバー更新
        api.makeSidebarScore();
        
        return true;
    }
    
    /**
     * @return isAllowPlayerJoinAny 設定をかえす
     */
    public boolean isAllowPlayerJoinAny() {
        return colorteaming.getCTConfig().isAllowPlayerJoinAny();
    }
    
    /**
     * @return isAllowPlayerJoinRandom 設定をかえす
     */
    public boolean isAllowPlayerJoinRandom() {
        return colorteaming.getCTConfig().isAllowPlayerJoinRandom();
    }
    
    /**
     * @return isAllowPlayerLeave 設定をかえす
     */
    public boolean isAllowPlayerLeave() {
        return colorteaming.getCTConfig().isAllowPlayerLeave();
    }
}
