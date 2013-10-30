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
     * TeamNameSettingを取得する
     * @param name チーム名、チーム表示名でも可
     * @return TeamNameSetting
     */
    public TeamNameSetting getTeamNameSetting(String name) {
        
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
        
        if ( !colorteaming.getCTConfig().isAllowPlayerJoinAny() ) {
            return false;
        }
        
        TeamNameSetting teamName = getTeamNameSetting(name);
        if ( teamName == null ) {
            return false;
        }
        
        ColorTeamingAPI api = colorteaming.getAPI();
        
        return (api.addPlayerTeam(player, teamName) != null);
    }
    
    /**
     * 指定されたプレイヤーを、人数の少ないチームへ参加させる。
     * @param player プレイヤー
     * @return チーム設定を実行したかどうか
     */
    public boolean addPlayerToRestTeam(Player player) {
        
        if ( !colorteaming.getCTConfig().isAllowPlayerJoinRandom() ) {
            return false;
        }
        
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
        
        if ( !colorteaming.getCTConfig().isAllowPlayerLeave() ) {
            return false;
        }
        
        ColorTeamingAPI api = colorteaming.getAPI();
        if ( api.getPlayerTeam(player) == null ) {
            return false;
        }
        api.leavePlayerTeam(player, Reason.SELF);
        return true;
    }
}
