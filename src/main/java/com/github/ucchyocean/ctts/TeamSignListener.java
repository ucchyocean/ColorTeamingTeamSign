/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.ctts;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * リスナークラス
 * @author ucchy
 */
public class TeamSignListener implements Listener {

    private static final String ACTIVE = ChatColor.GREEN + "[ACTIVE]";
    private static final String INACTIVE = ChatColor.RED + "[INACTIVE]";
    private static final String FIRST_LINE_JOIN = "[CJoin]";
    private static final String FIRST_LINE_LEAVE = "[CLeave]";
    
    private static final String PERMISSION_PRE = "ctteamsign.";
    private static final String PERMISSION_USER_USE = PERMISSION_PRE + "user.use";
    private static final String PERMISSION_ADMIN_TOGGLE = PERMISSION_PRE + "admin.toggle";
    private static final String PERMISSION_ADMIN_PLACE = PERMISSION_PRE + "admin.place";
    private static final String PERMISSION_ADMIN_BREAK = PERMISSION_PRE + "admin.break";
    
    private static final String MSG_PRE = "[CTTS]";
    private static final String MSG_PRE_ERR = ChatColor.RED + MSG_PRE;
    private static final String MSG_NOT_EXIST_CLASS = 
            MSG_PRE_ERR + "指定されたチームが存在しません。";
    private static final String MSG_NOT_HAVE_PERMISSION_PLACE = 
            MSG_PRE_ERR + "権限が無いためチームサインを設置できません。";
    private static final String MSG_NOT_HAVE_PERMISSION_BREAK = 
            MSG_PRE_ERR + "権限が無いためチームサインを除去できません。";
    
    private static final String MSG_DISABLE_CONFIG_JOIN = 
            MSG_PRE_ERR + "現在チームへの参加は許可されていません。";
    private static final String MSG_DISABLE_CONFIG_LEAVE = 
            MSG_PRE_ERR + "現在チームからの離脱は許可されていません。";
    
    private ColorTeamingBridge bridge;
    
    /**
     * コンストラクタ
     * @param bridge 
     */
    public TeamSignListener(ColorTeamingBridge bridge) {
        this.bridge = bridge;
    }
    
    /**
     * カンバンをクリックしたときのイベント
     * @param event 
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        
        // クリックされたのがカンバンでないなら無視する
        if (event.getClickedBlock() == null ||
                !(event.getClickedBlock().getState() instanceof Sign)) {
            return;
        }
        
        Sign sign = (Sign)event.getClickedBlock().getState();
        
        // 関係のないカンバンなら無視する
        if (!sign.getLine(0).equals(FIRST_LINE_JOIN) 
                || !sign.getLine(0).equals(FIRST_LINE_LEAVE)) {
            return;
        }
        
        // joinのカンバンかどうかを調べる
        boolean isJoin = false;
        if ( sign.getLine(0).equals(FIRST_LINE_JOIN) ) {
            isJoin = true;
        }
        
        Player player = event.getPlayer();
        if ( event.getAction() == Action.LEFT_CLICK_BLOCK 
                || (event.getPlayer().getGameMode() == GameMode.ADVENTURE 
                    && event.getAction() == Action.RIGHT_CLICK_BLOCK ) ) {
            
            if ( !player.hasPermission(PERMISSION_USER_USE) ) {
                // 権限がない
                return;
            }
            
            if ( sign.getLine(3).equals(INACTIVE) ) {
                // カンバンが無効状態
                return;
            }
            
            String tname = sign.getLine(1);
            if ( isJoin && !tname.equals("") && !bridge.isExistTeam(tname) ) {
                // 指定されたクラスが既に存在しない
                player.sendMessage(MSG_NOT_EXIST_CLASS);
                return;
            }
            
            
            if ( isJoin && tname.equals("") ) {
                // 人数の少ないチームを設定する
                boolean result = bridge.addPlayerToRestTeam(player);
                if ( !result ) {
                    player.sendMessage(MSG_DISABLE_CONFIG_JOIN);
                    return;
                }
                
            } else if ( isJoin ) {
                // 指定されたチームを設定する
                boolean result = bridge.addPlayerToTeam(player, tname);
                if ( !result ) {
                    player.sendMessage(MSG_DISABLE_CONFIG_JOIN);
                    return;
                }
                
            } else {
                // チームから離脱させる
                boolean result = bridge.leavePlayerFromTeam(player);
                if ( !result ) {
                    player.sendMessage(MSG_DISABLE_CONFIG_LEAVE);
                    return;
                }
            }
            
            return;
            
        } else if ( event.getAction() == Action.RIGHT_CLICK_BLOCK ) {
            
            if ( !player.hasPermission(PERMISSION_ADMIN_TOGGLE) ) {
                // 権限が無い
                return;
            }
            
            // 有効状態と無効状態を切り替えする
            if ( sign.getLine(3).equals(ACTIVE) ) {
                sign.setLine(3, INACTIVE);
            } else if ( sign.getLine(3).equals(INACTIVE) ) {
                sign.setLine(3, ACTIVE);
            }
            sign.update();
            
            return;
        }
    }
    

    /**
     * カンバンを設置したときのイベント
     * @param event 
     */
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        
        // 関係のないカンバンなら無視する
        if (!event.getLine(0).equals(FIRST_LINE_JOIN) 
                || !event.getLine(0).equals(FIRST_LINE_LEAVE)) {
            return;
        }
        
        // joinのカンバンかどうかを調べる
        boolean isJoin = false;
        if ( event.getLine(0).equals(FIRST_LINE_JOIN) ) {
            isJoin = true;
        }
        
        Player player = event.getPlayer();
        if ( !player.hasPermission(PERMISSION_ADMIN_PLACE) ) {
            // 権限が無い
            player.sendMessage(MSG_NOT_HAVE_PERMISSION_PLACE);
            event.setLine(0, "");
            return;
        }

        String tname = event.getLine(1);
        if ( isJoin && !tname.equals("") && !bridge.isExistTeam(tname) ) {
            // 指定されたクラスが存在しない
            player.sendMessage(MSG_NOT_EXIST_CLASS);
            event.setLine(0, "");
            return;
        }
        
        // クラスサインを有効状態に変更する
        event.setLine(3, ACTIVE);
    }
    
    /**
     * ブロックが壊されたときのイベント
     * @param event 
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        BlockState block = event.getBlock().getState();
        if ( !(block instanceof Sign) ) {
            // 壊されたブロックがカンバンでないなら無視する
            return;
        }
        
        Sign sign = (Sign) block;
        if (!sign.getLine(0).equals(FIRST_LINE_JOIN) 
                || !sign.getLine(0).equals(FIRST_LINE_LEAVE)) {
            // 関係のないカンバンなら無視する
            return;
        }

        Player player = event.getPlayer();
        if ( !player.hasPermission(PERMISSION_ADMIN_BREAK) ) {
            // 権限が無い
            player.sendMessage(MSG_NOT_HAVE_PERMISSION_BREAK);
            event.setCancelled(true);
            return;
        }
    }
}
